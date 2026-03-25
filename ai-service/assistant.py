from flask import Flask, request, jsonify, render_template
from flask_cors import CORS
from werkzeug.utils import secure_filename
import dashscope
import pymysql
import json
import os
import uuid
import re
import time
import random
from typing import Dict, List, Any, Optional
import mimetypes
import base64
from io import BytesIO
from datetime import datetime
from PIL import Image

BASE_DIR = os.path.dirname(os.path.abspath(__file__))
PROJECT_ROOT = os.path.dirname(BASE_DIR)
DEFAULT_TEMPLATE_DIR = os.path.join(PROJECT_ROOT, "templates")
FALLBACK_TEMPLATE_DIR = os.path.join(BASE_DIR, "templates")
TEMPLATE_DIR = DEFAULT_TEMPLATE_DIR if os.path.isdir(DEFAULT_TEMPLATE_DIR) else FALLBACK_TEMPLATE_DIR

# 文件上传配置
UPLOAD_FOLDER = os.path.join(PROJECT_ROOT, 'storage', 'ai_uploads')
ALLOWED_EXTENSIONS = {'pdf', 'doc', 'docx', 'xls', 'xlsx', 'jpg', 'jpeg', 'png', 'txt'}
MAX_FILE_SIZE = 10 * 1024 * 1024  # 10MB

# 确保上传目录存在
if not os.path.exists(UPLOAD_FOLDER):
    os.makedirs(UPLOAD_FOLDER)

app = Flask(__name__, template_folder=TEMPLATE_DIR)
app.config['UPLOAD_FOLDER'] = UPLOAD_FOLDER
app.config['MAX_CONTENT_LENGTH'] = MAX_FILE_SIZE
CORS(app)

# 配置
API_KEY = os.getenv("DASHSCOPE_API_KEY", "")
MODEL_NAME = "qwen-plus"

# 数据库配置
DB_CONFIG = {
    'host': '120.26.212.210',
    'port': 3306,
    'user': 'javaee',
    'password': '@Yali123456',
    'database': 'javaee',
    'charset': 'utf8mb4'
}

# 会话历史存储（内存中，用于当前会话）
conversation_history = {}
db_schema_cache: Optional[str] = None
last_db_context: Dict[str, Dict[str, Any]] = {}

SENSITIVE_KEYWORDS = [
    '手机号', '手机', '电话', '密码', '身份证', '身份证号', '住址', '地址', '银行卡', '联系', '联系方式', 'email', '邮箱'
]

ROLE_PERMISSION_NOTE = {
    'student': '仅限本人相关数据（选课、作业、成绩等）',
    'teacher': '仅限本人教授课程及相关学生数据',
    'admin': '管理员可查看全局数据'
}

# ========== 主观题批改功能 ==========
GRADING_SYSTEM_PROMPT = (
    "你是一名严谨的教师助教，专门负责主观题阅卷。"
    "请始终用中文回复，并且严格按照要求输出 JSON。"
    "JSON 必须包含 score、percentage、comment、strengths、improvements 五个字段，"
    "不得包含 Markdown 代码块或多余文本。"
)

GRADING_USER_TEMPLATE = """请根据以下作业信息给出评分：

【题目】
{question}

【标准答案】
{standard_answer}

【学生答案】
{student_answer}

【题目总分】{total_score}分

评分参考：
1. 内容完整性（40%）
2. 准确性（30%）
3. 逻辑性（20%）
4. 深度理解（10%）

请结合上述规则，返回如下 JSON（不要添加额外文字）：
{{
  "score": 0-{total_score} 的整数,
  "percentage": 得分率 0-100 的整数,
  "comment": "50-150 字的点评",
  "strengths": ["优点1", "优点2"],
  "improvements": ["改进建议1", "改进建议2"]
}}
"""

# ========== 批改相关辅助函数 ==========
def clamp(value: int, min_value: int, max_value: int) -> int:
    return max(min_value, min(value, max_value))


def log_grading_debug(message: str) -> None:
    """简单写入评分调试日志，方便追踪 DashScope 原始响应。"""
    try:
        debug_path = os.path.join(BASE_DIR, 'grading_debug.log')
        with open(debug_path, 'a', encoding='utf-8') as log_file:
            log_file.write(f"[{datetime.now().isoformat()}] {message}\n")
    except Exception:
        # 调试日志失败时不影响主流程
        pass


def safe_serialize(obj: Any, limit: int = 2000) -> str:
    """尽量把对象序列化成可读文本，避免日志写入失败。"""
    try:
        if isinstance(obj, (dict, list)):
            text = json.dumps(obj, ensure_ascii=False, default=str)
        elif hasattr(obj, '__dict__'):
            text = json.dumps(obj.__dict__, ensure_ascii=False, default=str)
        else:
            text = str(obj)
    except Exception:
        text = repr(obj)
    if len(text) > limit:
        return text[:limit] + '...'
    return text

def allowed_file(filename):
    """检查文件类型是否允许"""
    return '.' in filename and filename.rsplit('.', 1)[1].lower() in ALLOWED_EXTENSIONS

def get_file_type(filename):
    """根据文件后缀返回文件类型"""
    ext = filename.rsplit('.', 1)[1].lower() if '.' in filename else ''
    if ext in ['doc', 'docx']:
        return 'word'
    elif ext == 'pdf':
        return 'pdf'
    elif ext in ['xls', 'xlsx']:
        return 'excel'
    elif ext in ['jpg', 'jpeg', 'png']:
        return 'image'
    elif ext == 'txt':
        return 'txt'
    return 'unknown'

def extract_text_from_file(file_path, file_type):
    """从文件中提取文本内容（简单实现）"""
    try:
        if file_type == 'txt':
            with open(file_path, 'r', encoding='utf-8') as f:
                return f.read()[:1000]  # 最多读取1000字符
        elif file_type == 'image':
            return f"[图片文件: {os.path.basename(file_path)}]"
        elif file_type in ['word', 'pdf', 'excel']:
            return f"[文档文件: {os.path.basename(file_path)}]"
        return ""
    except Exception as e:
        print(f"提取文件内容失败: {e}")
        return ""

def has_sensitive_request(message: str) -> Optional[str]:
    lower_msg = message.lower()
    for kw in SENSITIVE_KEYWORDS:
        if kw.lower() in lower_msg:
            return kw
    return None

def check_permission(message: str, role: str, user_id: str) -> (bool, Optional[str]):
    violation_kw = has_sensitive_request(message)
    if violation_kw and role != 'admin':
        print(f"用户身份 / 角色：{role} | 用户 ID：{user_id} | 权限核验结果：无权限 | 违规类型：查询私密信息（{violation_kw}） | 处理结果：拒绝查询")
        return False, violation_kw
    return True, None

def is_common_question(message: str) -> bool:
    text = message.lower()
    domain_words = ['课程', '成绩', '作业', '考试', '选课', '教师', '老师', '学生', '班级', '教务']
    general_words = ['是什么', '怎么', '如何', '为什么', 'meaning', 'define', '定义']
    return any(g in text for g in general_words) and not any(d in text for d in domain_words)

def extract_primary_table(sql: str) -> Optional[str]:
    if not sql:
        return None
    match = re.search(r'from\s+([a-zA-Z0-9_]+)', sql, re.IGNORECASE)
    if match:
        return match.group(1)
    return None

def get_table_columns_info(table: str) -> (str, str):
    if not table:
        return '', ''
    if not re.match(r'^[a-zA-Z_][a-zA-Z0-9_]*$', table):
        return '', ''
    schema_sql = f"SHOW FULL COLUMNS FROM `{table}`"
    try:
        conn = get_db_connection()
        cursor = conn.cursor(pymysql.cursors.DictCursor)
        cursor.execute(schema_sql)
        rows = cursor.fetchall()
        cursor.close()
        conn.close()
        fields = [row.get('Field') or row.get('FIELD') for row in rows if row.get('Field') or row.get('FIELD')]
        return schema_sql, ', '.join(fields) if fields else ''
    except Exception as exc:
        print(f"获取表字段失败: {exc}")
        return schema_sql, ''

def sanitize_user_id(raw_user_id: str) -> str:
    """确保 user_id 可安全用于 SQL（仅保留数字部分）。"""
    try:
        return str(int(raw_user_id))
    except (ValueError, TypeError):
        return raw_user_id.strip()[:32]


def resolve_user_id(raw_user_id: Optional[str], username: Optional[str] = None) -> str:
    """优先使用传入的 userId；若缺失则尝试按用户名查询 tb_user.id。默认返回 '1' 以保证不为空。"""
    if raw_user_id:
        return sanitize_user_id(raw_user_id)
    if username:
        try:
            conn = get_db_connection()
            cursor = conn.cursor(pymysql.cursors.DictCursor)
            cursor.execute("SELECT id FROM tb_user WHERE username = %s LIMIT 1", (username,))
            row = cursor.fetchone()
            cursor.close()
            conn.close()
            if row and row.get('id') is not None:
                return sanitize_user_id(row['id'])
        except Exception as exc:
            print(f"按用户名查询 user_id 失败: {exc}")
    return '1'

def get_db_connection():
    """获取数据库连接"""
    return pymysql.connect(**DB_CONFIG)

def save_message_to_db(user_id, role, message, is_user, session_id, session_title, first_message=None, file_path=None, file_type=None):
    """保存消息到数据库，按会话维度做滚动留存。"""
    try:
        conn = get_db_connection()
        cursor = conn.cursor()

        # 插入新消息
        sql = """INSERT INTO tb_ai_chat_history 
                 (user_id, role, message, is_user, session_id, session_title, first_message, file_path, file_type)
                 VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s)"""
        cursor.execute(sql, (user_id, role, message, is_user, session_id, session_title, first_message, file_path, file_type))

        # 每个用户+角色+会话保留最近50条
        delete_sql = """
            DELETE FROM tb_ai_chat_history
            WHERE user_id = %s AND role = %s AND session_id = %s
            AND id NOT IN (
                SELECT id FROM (
                    SELECT id FROM tb_ai_chat_history
                    WHERE user_id = %s AND role = %s AND session_id = %s
                    ORDER BY created_at DESC LIMIT 50
                ) tmp
            )
        """
        cursor.execute(delete_sql, (user_id, role, session_id, user_id, role, session_id))

        conn.commit()
        cursor.close()
        conn.close()
    except Exception as e:
        print(f"保存消息失败: {str(e)}")

def load_history_from_db(user_id, role, session_id):
    """从数据库加载某个会话的历史记录"""
    try:
        conn = get_db_connection()
        cursor = conn.cursor(pymysql.cursors.DictCursor)

        sql = """SELECT message, is_user, created_at
                 FROM tb_ai_chat_history
                 WHERE user_id = %s AND role = %s AND session_id = %s
                 ORDER BY created_at ASC
                 LIMIT 100"""
        cursor.execute(sql, (user_id, role, session_id))

        results = cursor.fetchall()
        cursor.close()
        conn.close()

        history = []
        for row in results:
            if not row['message']:
                continue
            history.append({
                'role': 'user' if row['is_user'] else 'assistant',
                'content': row['message']
            })

        return history
    except Exception as e:
        print(f"加载历史失败: {str(e)}")
        return []

def list_sessions_from_db(user_id, role):
    """列出用户的会话列表，按最近时间排序。"""
    try:
        conn = get_db_connection()
        cursor = conn.cursor(pymysql.cursors.DictCursor)

        sql = """
            SELECT session_id, session_title, 
                   MAX(created_at) AS last_time,
                   (SELECT first_message FROM tb_ai_chat_history 
                    WHERE session_id = h.session_id AND first_message IS NOT NULL 
                    LIMIT 1) AS first_message
            FROM tb_ai_chat_history h
            WHERE user_id = %s AND role = %s
            GROUP BY session_id, session_title
            ORDER BY last_time DESC
            LIMIT 50
        """
        cursor.execute(sql, (user_id, role))
        rows = cursor.fetchall()
        cursor.close()
        conn.close()
        return rows
    except Exception as e:
        print(f"加载会话列表失败: {str(e)}")
        return []

def create_session(user_id: str, role: str, title: Optional[str] = None) -> Dict[str, str]:
    """创建新会话，返回 session_id 与标题。"""
    session_id = str(uuid.uuid4())
    session_title = title or "新对话"
    try:
        conn = get_db_connection()
        cursor = conn.cursor()
        # 插入一条占位记录，便于前端立刻看见该会话
        sql = """INSERT INTO tb_ai_chat_history (user_id, role, message, is_user, session_id, session_title)
                 VALUES (%s, %s, %s, 0, %s, %s)"""
        cursor.execute(sql, (user_id, role, "", session_id, session_title))
        conn.commit()
        cursor.close()
        conn.close()
    except Exception as e:
        print(f"创建会话失败: {str(e)}")
    return {"session_id": session_id, "session_title": session_title}

def get_all_table_names() -> List[str]:
    """获取数据库中所有tb_开头的表名"""
    try:
        conn = get_db_connection()
        cursor = conn.cursor()
        cursor.execute(
            "SELECT table_name FROM information_schema.tables "
            "WHERE table_schema = %s AND table_name LIKE 'tb_%%' "
            "ORDER BY table_name",
            (DB_CONFIG['database'],)
        )
        tables = [row[0] for row in cursor.fetchall()]
        cursor.close()
        conn.close()
        return tables
    except Exception as exc:
        print(f"获取表名失败: {exc}")
        return []

def get_tables_structure(table_names: List[str]) -> Dict[str, str]:
    """批量查询多个表的结构信息"""
    result = {}
    conn = get_db_connection()
    cursor = conn.cursor(pymysql.cursors.DictCursor)
    
    for table in table_names:
        if not re.match(r'^[a-zA-Z_][a-zA-Z0-9_]*$', table):
            continue
        try:
            cursor.execute(f"SHOW FULL COLUMNS FROM `{table}`")
            rows = cursor.fetchall()
            fields_info = []
            for row in rows:
                field = row.get('Field') or row.get('FIELD')
                field_type = row.get('Type') or row.get('TYPE')
                comment = row.get('Comment') or row.get('COMMENT') or ''
                if comment:
                    fields_info.append(f"{field}({field_type}, {comment})")
                else:
                    fields_info.append(f"{field}({field_type})")
            result[table] = ', '.join(fields_info)
        except Exception as exc:
            print(f"查询表{table}结构失败: {exc}")
            result[table] = f"查询失败: {exc}"
    
    cursor.close()
    conn.close()
    return result

def get_db_schema_description(refresh: bool = False) -> str:
    """读取数据库元数据，生成简要结构描述。"""
    global db_schema_cache
    if db_schema_cache is not None and not refresh:
        return db_schema_cache

    try:
        conn = get_db_connection()
        cursor = conn.cursor(pymysql.cursors.DictCursor)
        schema_sql = """
            SELECT table_name, column_name, column_comment
            FROM information_schema.columns
            WHERE table_schema = %s AND table_name LIKE 'tb_%%'
            ORDER BY table_name, ordinal_position
        """
        cursor.execute(schema_sql, (DB_CONFIG['database'],))
        rows = cursor.fetchall()
        cursor.close()
        conn.close()
    except Exception as exc:
        print(f"读取数据库结构失败: {exc}")
        return ""

    table_columns: Dict[str, List[str]] = {}
    for row in rows:
        table = row.get('table_name') or row.get('TABLE_NAME') or ''
        comment = row.get('column_comment') or row.get('COLUMN_COMMENT') or ''
        col_desc = row.get('column_name') or row.get('COLUMN_NAME') or ''
        if comment:
            col_desc += f"({comment})"
        if not table:
            continue
        table_columns.setdefault(table, []).append(col_desc)

    lines: List[str] = []
    for table, cols in list(table_columns.items())[:20]:
        preview = ', '.join(cols[:8])
        if len(cols) > 8:
            preview += ', ...'
        lines.append(f"{table}: {preview}")

    description = "\n".join(lines)
    db_schema_cache = description
    return description

def extract_json_block(text: str) -> Dict[str, Any]:
    """从模型输出中提取 JSON。"""
    if not text:
        return {}
    cleaned = text.strip()
    if cleaned.startswith('```'):
        cleaned = cleaned.strip('`')
        if '\n' in cleaned:
            cleaned = cleaned.split('\n', 1)[-1]
    start = cleaned.find('{')
    end = cleaned.rfind('}')
    if start == -1 or end == -1 or end <= start:
        return {}
    try:
        return json.loads(cleaned[start:end+1])
    except json.JSONDecodeError:
        try:
            # 处理可能的单引号或尾随注释
            normalized = cleaned[start:end+1].replace("'", '"')
            return json.loads(normalized)
        except Exception:
            return {}

def is_safe_select(sql: str) -> bool:
    if not sql:
        return False
    lowered = sql.strip().lower()
    if not lowered.startswith('select'):
        return False
    forbidden = ['insert', 'update', 'delete', 'drop', 'alter', 'truncate']
    return not any(keyword in lowered for keyword in forbidden)

def execute_select(sql: str) -> List[Dict[str, Any]]:
    conn = get_db_connection()
    cursor = conn.cursor(pymysql.cursors.DictCursor)
    print(f"执行SQL: {sql}")
    cursor.execute(sql)
    results = cursor.fetchall()
    print(f"SQL返回{len(results)}条记录")
    cursor.close()
    conn.close()
    return results

def summarize_rows(rows: List[Dict[str, Any]], limit: int = 5) -> str:
    if not rows:
        return "未查询到相关记录。"
    preview = rows[:limit]
    summary_lines = []
    for idx, row in enumerate(preview, start=1):
        kv_pairs = ', '.join(f"{k}={row[k]}" for k in list(row.keys())[:6])
        summary_lines.append(f"记录{idx}: {kv_pairs}")
    if len(rows) > limit:
        summary_lines.append(f"... 共 {len(rows)} 条记录")
    return "\n".join(summary_lines)


def is_followup_course_detail(message: str) -> bool:
    """粗略判断是否在追问上一条课程统计的具体课程明细。"""
    text = message.lower()
    keywords = ['这几门课', '这些课', '哪几门', '分别是什么', '是什么课', '是哪几门', '具体课程', '具体有哪些课']
    return any(k in text for k in keywords)

def keyword_sql_plan(message: str, role: str, user_id: str) -> Optional[Dict[str, Any]]:
    """基于关键词规则匹配，生成常见问题的SQL查询（如"我是谁"）"""
    sanitized_user_id = sanitize_user_id(user_id)
    msg_lower = message.lower().strip()
    
    # ==================== 特定问题查询真实数据，让AI组织语言 ====================
    # 教师查询课程中的学生数量
    if role == 'teacher' and any(keyword in msg_lower for keyword in ['课程中有多少学生', '我的课程中有多少学生', '有多少学生', '学生人数', '选课人数', '多少人选']):
        try:
            conn = get_db_connection()
            cursor = conn.cursor(pymysql.cursors.DictCursor)
            cursor.execute("""
                SELECT c.name AS 课程名称, c.enrolled AS 已选人数, c.capacity AS 容量
                FROM tb_course c
                WHERE c.teacher_id = %s
            """, (sanitized_user_id,))
            rows = cursor.fetchall()
            cursor.close()
            conn.close()
            
            if not rows:
                summary = "您目前没有开设任何课程。"
            else:
                summary = "根据系统数据，您的课程学生情况如下：\n"
                for i, row in enumerate(rows, 1):
                    summary += f"{i}. 《{row['课程名称']}》：{row['已选人数']} 名学生（容量：{row['容量']}人）\n"
            
            return {
                'need_db': True,
                'reason': '查询教师课程中的学生数量',
                'sql': f"SELECT c.name AS 课程名称, c.enrolled AS 已选人数 FROM tb_course c WHERE c.teacher_id = {sanitized_user_id}",
                'summary': summary
            }
        except Exception as e:
            print(f"查询教师课程学生数失败: {e}")
            return None
    
    # 学生查询自己的课程
    elif role == 'student' and any(keyword in msg_lower for keyword in ['我有哪些课程', '我的课程', '选了什么课', '我选的课', '哪些课']):
        try:
            conn = get_db_connection()
            cursor = conn.cursor(pymysql.cursors.DictCursor)
            cursor.execute("""
                SELECT c.name AS 课程名称, c.course_code AS 课程代码, c.credit AS 学分, c.semester AS 学期
                FROM tb_course_selection cs
                JOIN tb_course c ON cs.course_id = c.id
                WHERE cs.student_id = %s AND cs.status = 'selected'
            """, (sanitized_user_id,))
            rows = cursor.fetchall()
            cursor.close()
            conn.close()
            
            if not rows:
                summary = "您目前没有选择任何课程。"
            else:
                summary = "根据系统记录，您当前选修的课程如下：\n"
                for i, row in enumerate(rows, 1):
                    summary += f"{i}. 《{row['课程名称']}》(课程代码: {row['课程代码']}) - {row['学分']}学分，{row['学期']}学期\n"
            
            return {
                'need_db': True,
                'reason': '查询学生选修的课程',
                'sql': f"SELECT c.name AS 课程名称 FROM tb_course_selection cs JOIN tb_course c ON cs.course_id = c.id WHERE cs.student_id = {sanitized_user_id}",
                'summary': summary
            }
        except Exception as e:
            print(f"查询学生课程失败: {e}")
            return None
    
    # 查询用户个人信息
    elif '我是谁' in msg_lower:
        try:
            conn = get_db_connection()
            cursor = conn.cursor(pymysql.cursors.DictCursor)
            cursor.execute("""
                SELECT id, username, real_name, role, email
                FROM tb_user
                WHERE id = %s
            """, (sanitized_user_id,))
            row = cursor.fetchone()
            cursor.close()
            conn.close()
            
            if not row:
                summary = "无法获取您的用户信息。"
            else:
                summary = f"您好！您是{row['role']} {row['real_name']}({row['username']})"
            
            return {
                'need_db': True,
                'reason': '查询用户个人信息',
                'sql': f"SELECT id, username, real_name, role FROM tb_user WHERE id = {sanitized_user_id}",
                'summary': summary
            }
        except Exception as e:
            print(f"查询用户个人信息失败: {e}")
            return None
    
    # 查询系统课程总数
    elif any(keyword in msg_lower for keyword in ['系统有多少课程', '课程总数', '共有多少课程']):
        try:
            conn = get_db_connection()
            cursor = conn.cursor(pymysql.cursors.DictCursor)
            cursor.execute("SELECT COUNT(*) AS total FROM tb_course")
            row = cursor.fetchone()
            cursor.close()
            conn.close()
            
            total = row['total'] if row else 0
            summary = f"系统中共有 {total} 门课程。"
            
            return {
                'need_db': True,
                'reason': '查询系统课程总数',
                'sql': "SELECT COUNT(*) AS total FROM tb_course",
                'summary': summary
            }
        except Exception as e:
            print(f"查询系统课程总数失败: {e}")
            return None
    
    # 查询系统学生总数
    elif any(keyword in msg_lower for keyword in ['系统有多少学生', '学生总数', '共有多少学生']):
        try:
            conn = get_db_connection()
            cursor = conn.cursor(pymysql.cursors.DictCursor)
            cursor.execute("SELECT COUNT(*) AS total FROM tb_user WHERE role = 'student'")
            row = cursor.fetchone()
            cursor.close()
            conn.close()
            
            total = row['total'] if row else 0
            summary = f"系统中共有 {total} 名学生。"
            
            return {
                'need_db': True,
                'reason': '查询系统学生总数',
                'sql': "SELECT COUNT(*) AS total FROM tb_user WHERE role = 'student'",
                'summary': summary
            }
        except Exception as e:
            print(f"查询系统学生总数失败: {e}")
            return None
    
    # 学生查询作业完成情况
    elif role == 'student' and any(keyword in msg_lower for keyword in ['我的作业完成情况', '作业完成情况', '我完成了多少作业', '作业进度', '作业提交情况', '我的作业提交']):
        try:
            conn = get_db_connection()
            cursor = conn.cursor(pymysql.cursors.DictCursor)
            cursor.execute("""
                SELECT 
                    COUNT(hs.id) AS total_submitted,
                    (SELECT COUNT(*) FROM tb_homework WHERE course_id IN (
                        SELECT course_id FROM tb_course_selection WHERE student_id = %s
                    )) AS total_assignments
                FROM tb_homework_submission hs
                WHERE hs.student_id = %s
            """, (sanitized_user_id, sanitized_user_id))
            row = cursor.fetchone()
            cursor.close()
            conn.close()
            
            if not row:
                summary = "无法获取您的作业完成情况。"
            else:
                submitted = row['total_submitted'] or 0
                total = row['total_assignments'] or 0
                pending = total - submitted
                summary = f"根据查询结果：您总共有 {total} 个作业，已完成 {submitted} 个，待完成 {pending} 个。"
            
            return {
                'need_db': True,
                'reason': '查询学生作业完成情况',
                'sql': f"SELECT COUNT(hs.id) AS total_submitted FROM tb_homework_submission hs WHERE hs.student_id = {sanitized_user_id}",
                'summary': summary
            }
        except Exception as e:
            print(f"查询学生作业完成情况失败: {e}")
            return None
    
    # ==================== 原有的关键词匹配逻辑 ====================
    # 匹配"我是谁"类问题
    if any(kw in msg_lower for kw in ['我的信息', '个人信息', '我的资料', '查看我的']):
        if role == 'teacher':
            sql = f"""SELECT u.id AS id, u.username, u.real_name, u.role, u.email,
                              t.id AS teacher_id, t.teacher_number, t.department, t.title, t.research_field
                       FROM tb_user u
                       LEFT JOIN tb_teacher t ON t.user_id = u.id
                       WHERE u.id = {sanitized_user_id}
                       LIMIT 1"""
        elif role == 'student':
            sql = f"""SELECT u.id AS id, u.username, u.real_name, u.role, u.email,
                              s.id AS student_id, s.student_number, s.major, s.class_name, s.enrollment_year, s.grade
                       FROM tb_user u
                       LEFT JOIN tb_student s ON s.user_id = u.id
                       WHERE u.id = {sanitized_user_id}
                       LIMIT 1"""
        else:
            sql = f"SELECT id, username, real_name, role, email FROM tb_user WHERE id = {sanitized_user_id} LIMIT 1"
        return {
            'need_db': True,
            'reason': f'查询当前{role}用户的基本信息',
            'sql': sql
        }
    
    # 学生查询自己的课程（非关键词）
    if role == 'student' and any(kw in msg_lower for kw in ['课程信息']):
        sql = f"""SELECT cs.course_id, cs.course_name, cs.select_time, cs.status, c.teacher_name, c.credit, c.semester
                  FROM tb_course_selection cs
                  JOIN tb_course c ON cs.course_id = c.id
                  WHERE cs.student_id = {sanitized_user_id}
                  LIMIT 20"""
        return {
            'need_db': True,
            'reason': '查询学生的选课信息',
            'sql': sql
        }
    
    # 学生查询自己的作业（非关键词）
    if role == 'student' and any(kw in msg_lower for kw in ['作业情况', '作业提交']):
        sql = f"""SELECT h.title, h.status, h.score, h.submit_time 
                  FROM tb_homework h 
                  WHERE h.student_id = {sanitized_user_id}
                  ORDER BY h.submit_time DESC LIMIT 20"""
        return {
            'need_db': True,
            'reason': '查询学生的作业提交情况',
            'sql': sql
        }
    
    # 教师查询自己教授的课程（非关键词）
    if role == 'teacher' and any(kw in msg_lower for kw in ['我教的课', '教授课程', '我有哪些课程', '哪些课', '课程信息']):
        sql = f"""SELECT c.id, c.course_code, c.name, c.description, c.status, c.semester, c.credit, c.capacity, c.enrolled
                  FROM tb_course c
                  WHERE c.teacher_id = {sanitized_user_id}
                  LIMIT 20"""
        return {
            'need_db': True,
            'reason': '查询教师教授的课程',
            'sql': sql
        }
    
    return None

def maybe_query_database(message: str, role: str, user_id: str) -> Dict[str, Any]:
    """多轮AI分析查询流程：
    第一轮：基于所有表名，让AI分析可能相关的表
    第二轮：查询相关表结构，让AI生成具体SQL
    第三轮：执行SQL并返回结果
    """
    # 权限核验
    allowed, violation_kw = check_permission(message, role, user_id)
    if not allowed:
        return {'error': '无查询权限，涉及敏感信息'}

    # 判定常识性问题
    if is_common_question(message):
        print(f"用户身份 / 角色：{role} | 用户 ID：{user_id} | 判定结果：常识性问题，无需查库 | 核心问题：{message}")
        return {'reason': '常识性问题，无需查库'}

    # 尝试规则匹配SQL（最高优先级）
    keyword_plan = keyword_sql_plan(message, role, user_id)
    if keyword_plan:
        # 检查是否是硬编码答案
        if 'hardcoded_answer' in keyword_plan:
            print(f"[硬编码答案] 问题: {message}")
            return {
                'hardcoded': True,
                'answer': keyword_plan['hardcoded_answer'],
                'reason': '快速问题-硬编码答案'
            }
        
        # 原有的SQL执行逻辑
        raw_sql = keyword_plan.get('sql', '')
        if raw_sql and is_safe_select(raw_sql):
            try:
                rows = execute_select(raw_sql)
                summary = summarize_rows(rows)
                print(f"[规则匹配] SQL: {raw_sql} | 结果: {len(rows)}条记录")
                return {
                    'sql': raw_sql,
                    'rows': rows,
                    'summary': summary,
                    'reason': keyword_plan.get('reason', '')
                }
            except Exception as exc:
                print(f"[规则匹配] SQL执行失败: {exc}")
                return {'sql': raw_sql, 'error': str(exc)}

    sanitized_user_id = sanitize_user_id(user_id)
    role_desc = {
        'student': '学生角色，只能查询自己的数据（选课、作业、成绩等），必须用user_id限制',
        'teacher': '教师角色，可查询自己教授的课程及相关学生数据，必须用teacher_id限制',
        'admin': '管理员角色，可查询全局统计数据'
    }

    # ========== 第一轮：分析可能相关的表 ==========
    all_tables = get_all_table_names()
    if not all_tables:
        return {'reason': '无法获取数据库表信息'}

    round1_system_prompt = (
        "你是数据库分析专家。根据用户问题，从提供的表名列表中选出可能相关的表。\n"
        f"用户角色：{role_desc.get(role, '普通用户')}\n\n"
        "请返回JSON格式：{\"need_db\": true/false, \"tables\": [表名列表], \"reason\": \"分析说明\"}\n"
        "注意：表名必须从下面列表中选择，不能自己编造！\n\n"
        f"可用表名列表：{', '.join(all_tables)}"
    )
    round1_user_prompt = (
        f"用户角色: {role}\n"
        f"用户ID: {sanitized_user_id}\n"
        f"用户问题: {message}\n\n"
        "请分析需要查询哪些表。"
    )

    try:
        round1_resp = dashscope.Generation.call(
            model=MODEL_NAME,
            api_key=API_KEY,
            messages=[
                {'role': 'system', 'content': round1_system_prompt},
                {'role': 'user', 'content': round1_user_prompt}
            ]
        )
        round1_text = round1_resp.get('output', {}).get('text', '') if round1_resp else ''
        round1_result = extract_json_block(round1_text)
        print(f"[第一轮AI分析] {round1_result}")
    except Exception as exc:
        print(f"第一轮AI分析失败: {exc}")
        return {'reason': f'AI分析失败: {exc}'}

    if not round1_result.get('need_db'):
        return {'reason': round1_result.get('reason', '无需查询数据库')}

    relevant_tables = round1_result.get('tables', [])
    if not relevant_tables:
        return {'reason': '未找到相关表'}

    # ========== 第二轮：查询表结构，生成SQL ==========
    tables_structure = get_tables_structure(relevant_tables)
    if not tables_structure:
        return {'reason': '无法获取表结构'}

    structure_desc = '\n'.join([f"{table}: {info}" for table, info in tables_structure.items()])
    
    round2_system_prompt = (
        "你是SQL生成专家。根据表结构和用户问题生成SELECT查询语句。\n"
        f"用户角色：{role_desc.get(role, '普通用户')}\n\n"
        "**严格要求：**\n"
        "1. 只能使用下面提供的表名和字段名，字段名必须完全匹配\n"
        "2. 只生成SELECT语句，禁止INSERT/UPDATE/DELETE/DROP\n"
        "3. 必须添加LIMIT 20限制返回数量\n"
        "4. student角色：tb_course_selection/tb_homework等表的student_id字段直接存储的是tb_user.id\n"
        "   - 直接使用WHERE student_id = {sanitized_user_id}即可\n"
        "   - 不需要JOIN tb_student表（除非需要学生的专业、班级等扩展信息）\n"
        "   - 正确示例：SELECT * FROM tb_course_selection WHERE student_id = {sanitized_user_id}\n"
        "5. teacher角色：tb_course表的teacher_id字段直接存储的是tb_user.id\n"
        "   - 直接使用WHERE teacher_id = {sanitized_user_id}即可\n"
        "   - 不需要JOIN tb_teacher表（除非需要教师的职称、研究方向等扩展信息）\n"
        "   - 正确示例：SELECT * FROM tb_course WHERE teacher_id = {sanitized_user_id}\n"
        "6. admin角色：可以查询全局数据，但仍建议关联用户表确认权限\n"
        "7. 如需JOIN其他表获取额外字段，必须确保字段名正确\n\n"
        "请返回JSON格式：{\"sql\": \"SELECT语句\", \"reason\": \"查询说明\"}\n\n"
        f"相关表结构：\n{structure_desc}"
    )
    round2_user_prompt = (
        f"用户角色: {role}\n"
        f"用户ID: {sanitized_user_id}\n"
        f"用户问题: {message}\n\n"
        "请生成符合权限要求的SQL查询语句。"
    )

    try:
        round2_resp = dashscope.Generation.call(
            model=MODEL_NAME,
            api_key=API_KEY,
            messages=[
                {'role': 'system', 'content': round2_system_prompt},
                {'role': 'user', 'content': round2_user_prompt}
            ]
        )
        round2_text = round2_resp.get('output', {}).get('text', '') if round2_resp else ''
        round2_result = extract_json_block(round2_text)
        print(f"[第二轮AI生成SQL] {round2_result}")
    except Exception as exc:
        print(f"第二轮AI生成SQL失败: {exc}")
        return {'reason': f'SQL生成失败: {exc}'}

    raw_sql = round2_result.get('sql', '')
    if not raw_sql:
        return {'reason': '未生成SQL语句'}
    
    if not is_safe_select(raw_sql):
        return {'reason': '生成的语句不安全，已拒绝执行', 'sql': raw_sql}

    # ========== 第三轮：执行SQL ==========
    permission_note = ROLE_PERMISSION_NOTE.get(role, '普通权限')
    try:
        rows = execute_select(raw_sql)
        summary = summarize_rows(rows)
        print(
            f"[SQL执行成功] 角色：{role} | SQL: {raw_sql} | "
            f"权限：{permission_note} | 结果：{len(rows)}条记录"
        )
        return {
            'sql': raw_sql,
            'rows': rows,
            'summary': summary,
            'reason': round2_result.get('reason', '')
        }
    except Exception as exc:
        print(
            f"[SQL执行失败] 角色：{role} | SQL: {raw_sql} | "
            f"权限：{permission_note} | 错误：{exc}"
        )
        return {'sql': raw_sql, 'error': str(exc), 'reason': round2_result.get('reason', '')}

def build_system_prompt(role):
    """构建角色提示词"""
    prompts = {
        'student': (
            "你是智能助学帮手，专门为学生提供学习支持和课程辅导。"
            "你支持文字和图片理解，可以帮助学生识别题目、分析图片内容。"
            "你可以帮助学生查询课程信息、作业情况、考试安排和学习进度。"
            "请始终优先依据系统返回的数据回答，用自然语言表达，不要提及数据库、表名、字段名等技术细节。"
            "如查询不到数据，请友好地建议用户：1) 换个方式描述问题；2) 提供更具体的信息；3) 或联系管理员核实账户信息。"
            "语气友好、鼓励，注重激发学生的学习兴趣。绝对不要编造数据。"
        ),
        'teacher': (
            "你是智能助教，专门为教师提供教学支持和课程管理帮助。"
            "你支持文字和图片理解，可以帮助教师识别教学材料、分析图片内容。"
            "你可以帮助教师管理课程、查看学生表现、分析作业和考试数据。"
            "请始终优先依据系统返回的数据回答，用自然语言表达，不要提及数据库、表名、字段名等技术细节。"
            "如查询不到数据，请友好地建议用户：1) 换个方式描述问题；2) 提供更具体的信息；3) 或联系管理员核实账户权限。"
            "回答应专业、实用，提供可执行的教学建议。绝对不要编造数据。"
        ),
        'admin': (
            "你是智能管理助手，专门为管理员提供系统管理和数据分析支持。"
            "你支持文字和图片理解，可以帮助分析各类数据和材料。"
            "你可以帮助管理员查看系统统计、用户管理、课程运营和平台数据。"
            "请始终优先依据系统返回的数据回答，用自然语言表达，不要过多暴露数据库表名、字段名等底层技术细节。"
            "如查询不到数据，请友好地建议用户：1) 换个方式描述问题；2) 提供更具体的筛选条件；3) 或检查数据是否已录入系统。"
            "回答需包含数据结论与管理建议，清晰准确。绝对不要编造数据。"
        )
    }
    return prompts.get(role, '你是智能教学助手，支持文字和图片理解。请提供准确的教学相关信息。不要暴露技术细节，不要编造数据。')

@app.route('/')
def index():
    return render_template('myhtml.html')

@app.route('/sessions', methods=['GET'])
def list_sessions():
    """获取当前用户的会话列表"""
    try:
        raw_user_id = request.args.get('userId')
        username = request.args.get('username')
        role = request.args.get('role', 'student')
        user_id = resolve_user_id(raw_user_id, username)
        sessions = list_sessions_from_db(user_id, role)
        return jsonify({'success': True, 'sessions': sessions})
    except Exception as e:
        print(f"加载会话列表失败: {str(e)}")
        return jsonify({'success': False, 'error': str(e)}), 500
@app.route('/sessions', methods=['POST'])
def new_session():
    """创建新会话"""
    try:
        data = request.json or {}
        raw_user_id = data.get('userId')
        username = data.get('username')
        role = data.get('role', 'student')
        user_id = resolve_user_id(raw_user_id, username)
        title = data.get('title')
        session = create_session(user_id, role, title)
        return jsonify({'success': True, **session})
    except Exception as e:
        print(f"创建会话失败: {str(e)}")
        return jsonify({'success': False, 'error': str(e)}), 500

@app.route('/history', methods=['GET'])
def get_history():
    """获取某个会话的历史对话记录"""
    try:
        raw_user_id = request.args.get('userId')
        username = request.args.get('username')
        role = request.args.get('role', 'student')
        user_id = resolve_user_id(raw_user_id, username)
        session_id = request.args.get('sessionId', 'default')

        history = load_history_from_db(user_id, role, session_id)

        messages = []
        for msg in history:
            messages.append({
                'role': msg['role'],
                'content': msg['content'],
                'time': ''
            })

        return jsonify({
            'success': True,
            'messages': messages
        })
    except Exception as e:
        print(f"获取历史失败: {str(e)}")
        return jsonify({
            'success': False,
            'error': str(e)
        }), 500

@app.route('/chat', methods=['POST'])
def chat():
    try:
        data = request.json or {}
        message = data.get('message', '').strip()
        role = data.get('role', 'student')
        raw_user_id = data.get('userId')
        username = data.get('username')
        user_id = resolve_user_id(raw_user_id, username)
        session_id = data.get('sessionId') or 'default'
        session_title = data.get('sessionTitle') or '默认会话'
        images = data.get('images', [])  # 获取图片列表（base64格式）

        if not message and not images:
            return jsonify({'success': False, 'error': '消息和图片不能同时为空'}), 400

        cache_key = f"{role}_{user_id}_{session_id}"
        if cache_key not in conversation_history:
            conversation_history[cache_key] = load_history_from_db(user_id, role, session_id)
        
        # 判断是否为会话的第一条消息
        is_first_message = len(conversation_history[cache_key]) == 0
        first_message_text = message[:100] if is_first_message else None

        messages = [{'role': 'system', 'content': build_system_prompt(role)}]
        for msg in conversation_history[cache_key]:
            messages.append(msg)
        
        # 如果有图片，使用视觉模型
        if images:
            # 构建多模态内容
            user_content = []
            # 如果没有文字,添加默认提示让AI描述图片
            if message:
                user_content.append({'text': message})
            else:
                user_content.append({'text': '请详细描述这张图片的内容。'})
            
            # 添加图片，dashscope API需要直接使用base64字符串
            for img_obj in images:
                # img_obj 可以是字典或字符串
                # 字典格式: {'data': 'data:image/jpeg;base64,...'}
                # 字符串格式: 'data:image/jpeg;base64,...'
                if isinstance(img_obj, dict):
                    img_data = img_obj.get('data', img_obj.get('url', ''))
                elif isinstance(img_obj, str):
                    img_data = img_obj
                elif isinstance(img_obj, list):
                    # 如果是ArrayList被错误地传递过来，取第一个元素
                    img_data = img_obj[0] if len(img_obj) > 0 else ''
                else:
                    print(f"Warning: 未知的图片数据格式: {type(img_obj)}")
                    img_data = str(img_obj)
                
                # 如果是完整的data URL，需要提取base64部分
                if isinstance(img_data, str) and img_data.startswith('data:image'):
                    # 格式: data:image/png;base64,iVBORw0KG...
                    img_data = img_data.split(',', 1)[1] if ',' in img_data else img_data
                
                # 压缩图片到合理大小
                try:
                    # 解码base64
                    img_bytes = base64.b64decode(img_data)
                    img = Image.open(BytesIO(img_bytes))
                    
                    original_size = len(img_data)
                    
                    # 如果图片过大,压缩到最大1920x1080
                    max_size = (1920, 1080)
                    if img.width > max_size[0] or img.height > max_size[1]:
                        img.thumbnail(max_size, Image.Resampling.LANCZOS)
                    
                    # 转换为RGB（如果是RGBA）
                    if img.mode == 'RGBA':
                        img = img.convert('RGB')
                    
                    # 重新编码为JPEG，质量85%
                    buffer = BytesIO()
                    img.save(buffer, format='JPEG', quality=85, optimize=True)
                    compressed_data = base64.b64encode(buffer.getvalue()).decode('utf-8')
                    
                    compression_rate = (1 - len(compressed_data) / original_size) * 100
                    print(f"图片压缩: 原始 {original_size} -> 压缩后 {len(compressed_data)} (压缩率: {compression_rate:.2f}%)")
                    user_content.append({'image': f"data:image/jpeg;base64,{compressed_data}"})
                except Exception as e:
                    print(f"图片压缩失败: {e}, 使用原始图片")
                    if isinstance(img_data, str):
                        user_content.append({'image': f"data:image/jpeg;base64,{img_data}"})
            
            messages.append({'role': 'user', 'content': user_content})
        else:
            messages.append({'role': 'user', 'content': message})

        db_context = {}
        # 只有非图片请求才查询数据库
        if not images:
            try:
                db_context = maybe_query_database(message, role, user_id) or {}
            except Exception as exc:
                print(f"数据库查询辅助出错: {exc}")
                db_context = {'error': str(exc)}
        
        # 检查是否是硬编码答案
        if db_context.get('hardcoded'):
            bot_reply = db_context['answer']
            print(f"[硬编码答案] 准备返回: {bot_reply[:50]}...")
            
            # 添加2-3秒随机延迟，模拟AI思考过程（避免前端超时）
            delay = random.uniform(2.0, 3.0)
            print(f"[硬编码答案] 模拟AI思考延迟: {delay:.2f}秒")
            time.sleep(delay)
            
            # 保存到对话历史
            conversation_history[cache_key].append({'role': 'user', 'content': message})
            conversation_history[cache_key].append({'role': 'assistant', 'content': bot_reply})
            save_message_to_db(user_id, role, message, is_user=True, session_id=session_id,
                             session_title=session_title, first_message=first_message_text)
            save_message_to_db(user_id, role, bot_reply, is_user=False, session_id=session_id,
                             session_title=session_title)
            
            return jsonify({'success': True, 'data': bot_reply, 'sessionId': session_id})
        
        # 若本次查询有结果，缓存下来用于后续上下文追问
        if db_context.get('summary'):
            last_db_context[cache_key] = db_context
            context_text = (
                f"系统查询结果：\n{db_context['summary']}\n"
                "请基于以上真实数据回答用户，用自然语言表达，不要提及SQL、表名、字段名等技术细节。"
            )
            messages.append({'role': 'system', 'content': context_text})
        elif db_context.get('error'):
            messages.append({'role': 'system', 'content': '系统暂时无法获取相关数据。请友好地建议用户：1) 换个方式提问；2) 提供更明确的描述；3) 或稍后再试。不要提及技术错误细节。'})
        else:
            # 没有新结果时，尝试复用上一条上下文（如“这几门课是什么”）
            prev_ctx = last_db_context.get(cache_key)
            if prev_ctx and prev_ctx.get('summary') and is_followup_course_detail(message):
                context_text = (
                    f"沿用上一轮查询结果：\n{prev_ctx['summary']}\n"
                    "请基于这些数据回答用户的追问，用自然语言表达，不要暴露技术细节。"
                )
                messages.append({'role': 'system', 'content': context_text})

        # 根据是否有图片选择不同的API
        if images:
            print(f"调用qwen-vl-max视觉模型，图片数量: {len(images)}")
            print(f"消息内容: {messages}")
            response = dashscope.MultiModalConversation.call(
                model='qwen-vl-max',
                api_key=API_KEY,
                messages=messages
            )
        else:
            response = dashscope.Generation.call(
                model=MODEL_NAME,
                api_key=API_KEY,
                messages=messages
            )

        print(f"AI响应类型: {type(response)}")
        print(f"AI响应: {response}")
        
        # 处理视觉模型的响应格式
        bot_reply = None
        if images:
            # qwen-vl-max 视觉模型的响应格式
            if hasattr(response, 'output') and hasattr(response.output, 'choices'):
                if response.output.choices and len(response.output.choices) > 0:
                    content = response.output.choices[0].message.content
                    # 确保content是字符串而不是对象
                    if isinstance(content, str):
                        bot_reply = content
                    elif isinstance(content, list) and len(content) > 0:
                        # 如果是列表，尝试提取第一个元素或合并
                        if isinstance(content[0], dict) and 'text' in content[0]:
                            bot_reply = content[0]['text']
                        elif isinstance(content[0], str):
                            bot_reply = ' '.join(content)
                        else:
                            bot_reply = str(content[0])
                    elif isinstance(content, dict):
                        # 如果是字典，尝试提取text字段
                        bot_reply = content.get('text', str(content))
                    else:
                        bot_reply = str(content)
            elif isinstance(response, dict):
                if 'output' in response and 'choices' in response['output']:
                    if response['output']['choices'] and len(response['output']['choices']) > 0:
                        content = response['output']['choices'][0]['message']['content']
                        # 同样处理字典返回的content
                        if isinstance(content, str):
                            bot_reply = content
                        elif isinstance(content, dict):
                            bot_reply = content.get('text', str(content))
                        else:
                            bot_reply = str(content)
        else:
            # qwen-plus 文本模型的响应格式
            if response and 'output' in response and 'text' in response['output']:
                bot_reply = response['output']['text']
        
        if bot_reply:
            user_msg_display = message or '【图片】'
            conversation_history[cache_key].append({'role': 'user', 'content': user_msg_display})
            conversation_history[cache_key].append({'role': 'assistant', 'content': bot_reply})
            save_message_to_db(user_id, role, user_msg_display, 1, session_id, session_title, first_message_text)
            save_message_to_db(user_id, role, bot_reply, 0, session_id, session_title, first_message_text)
            if len(conversation_history[cache_key]) > 20:
                conversation_history[cache_key] = conversation_history[cache_key][-20:]
        else:
            bot_reply = "抱歉，我现在无法回答，请稍后再试。"
            print(f"无法提取响应内容,响应结构: {response}")

        role_prompt = build_system_prompt(role)
        pre_result = '图片识别' if images else (db_context.get('summary') or db_context.get('error') or '无数据库结果')
        print(
            f"用户原始问题：{message or '【图片】'} | 用户角色：{role} | 角色化 Prompt 模板：{role_prompt} | "
            f"润色前结果：{pre_result} | 最终反馈给用户的润色结果：{bot_reply}"
        )

        payload = {
            'success': True,
            'response': bot_reply,
            'sessionId': session_id,
            'sessionTitle': session_title
        }
        if db_context:
            payload['dataContext'] = {
                'sql': db_context.get('sql'),
                'summary': db_context.get('summary'),
                'error': db_context.get('error')
            }
        return jsonify(payload)

    except Exception as e:
        print(f"错误: {str(e)}")
        return jsonify({
            'success': False,
            'error': str(e)
        }), 500

@app.route('/upload', methods=['POST'])
def upload_file():
    """文件上传接口"""
    try:
        if 'file' not in request.files:
            return jsonify({'success': False, 'error': '未找到文件'}), 400
        
        file = request.files['file']
        if file.filename == '':
            return jsonify({'success': False, 'error': '未选择文件'}), 400
        
        if not allowed_file(file.filename):
            return jsonify({'success': False, 'error': '不支持的文件类型'}), 400
        
        # 保存文件
        filename = secure_filename(file.filename)
        unique_filename = f"{uuid.uuid4()}_{filename}"
        file_path = os.path.join(app.config['UPLOAD_FOLDER'], unique_filename)
        file.save(file_path)
        
        file_type = get_file_type(filename)
        file_content = extract_text_from_file(file_path, file_type)
        
        return jsonify({
            'success': True,
            'file_path': file_path,
            'file_type': file_type,
            'file_name': filename,
            'file_content': file_content
        })
    except Exception as e:
        print(f"文件上传失败: {str(e)}")
        return jsonify({'success': False, 'error': str(e)}), 500

# ========== 主观题批改路由 ==========
@app.route('/api/ai/grade-subjective', methods=['POST'])
def grade_subjective():
    """主观题AI评分接口：复用对话模型，根据提示词返回严格JSON"""
    data = request.json or {}
    question = (data.get('questionContent') or '').strip()
    standard_answer = (data.get('standardAnswer') or '').strip() or '未提供标准答案'
    student_answer = (data.get('studentAnswer') or '').strip()
    total_score = data.get('totalScore', 100)

    try:
        total_score = int(total_score)
        if total_score <= 0:
            total_score = 100
    except (TypeError, ValueError):
        total_score = 100

    if not question or not student_answer:
        return jsonify({
            'success': False,
            'error': '题目内容和学生答案不能为空'
        }), 400

    user_prompt = GRADING_USER_TEMPLATE.format(
        question=question,
        standard_answer=standard_answer,
        student_answer=student_answer,
        total_score=total_score
    )

    try:
        response = dashscope.Generation.call(
            model=MODEL_NAME,
            api_key=API_KEY,
            messages=[
                {'role': 'system', 'content': GRADING_SYSTEM_PROMPT},
                {'role': 'user', 'content': user_prompt}
            ],
            result_format='message'
        )

        response_dict: Optional[Dict[str, Any]] = response if isinstance(response, dict) else None
        if response_dict is None and hasattr(response, 'to_dict'):
            try:
                response_dict = response.to_dict()
            except Exception:
                response_dict = None
        if response_dict is None and hasattr(response, '__dict__'):
            response_dict = response.__dict__

        log_grading_debug(
            f"DashScope响应: status={getattr(response, 'status_code', None)}, code={getattr(response, 'code', None)}, "
            f"message={getattr(response, 'message', None)}"
        )
        log_grading_debug(f"DashScope原始内容: {safe_serialize(response_dict or response)}")

        print(f"AI评分响应对象: {response}")
        ai_text = ''

        output_text_value = None
        if isinstance(response, dict):
            output_text_value = response.get('output_text')
        elif hasattr(response, 'get'):
            try:
                output_text_value = response.get('output_text')
            except Exception:
                output_text_value = None
        if output_text_value is None and isinstance(response_dict, dict):
            output_text_value = response_dict.get('output_text')

        if isinstance(output_text_value, str) and output_text_value.strip():
            ai_text = output_text_value

        if not ai_text and isinstance(response_dict, dict):
            output_block = response_dict.get('output') if isinstance(response_dict, dict) else None
            if isinstance(output_block, dict):
                ai_text = output_block.get('text', '')
                if not ai_text and output_block.get('choices'):
                    first_choice = output_block['choices'][0]
                    message = first_choice.get('message') if isinstance(first_choice, dict) else None
                    if message:
                        content = message.get('content')
                        if isinstance(content, str):
                            ai_text = content
                        elif isinstance(content, list):
                            parts = []
                            for item in content:
                                if isinstance(item, dict):
                                    parts.append(item.get('text', ''))
                                else:
                                    parts.append(str(item))
                            ai_text = ''.join(parts)
                        elif content is not None:
                            ai_text = str(content)

        if not ai_text and hasattr(response, 'output'):
            output = response.output
            if hasattr(output, 'text') and output.text:
                ai_text = output.text
            elif hasattr(output, 'choices') and output.choices:
                first_choice = output.choices[0]
                message = None
                if isinstance(first_choice, dict):
                    message = first_choice.get('message')
                else:
                    message = getattr(first_choice, 'message', None)

                if message:
                    content = None
                    if isinstance(message, dict):
                        content = message.get('content')
                    else:
                        content = getattr(message, 'content', None)

                    if isinstance(content, str):
                        ai_text = content
                    elif isinstance(content, list):
                        parts = []
                        for item in content:
                            if isinstance(item, dict):
                                parts.append(item.get('text', ''))
                            else:
                                parts.append(str(item))
                        ai_text = ''.join(parts)
                    elif content is not None:
                        ai_text = str(content)

        if ai_text is None:
            ai_text = ''
        elif not isinstance(ai_text, str):
            if isinstance(ai_text, (dict, list)):
                ai_text = json.dumps(ai_text, ensure_ascii=False)
            else:
                ai_text = str(ai_text)

        print(f"AI评分提取文本: {ai_text[:200]}...")

        if not ai_text.strip():
            log_grading_debug("AI未返回文本，response_dict=" + safe_serialize(response_dict))
            raise ValueError('AI未返回有效内容')

        result = extract_json_block(ai_text)
        if not result:
            result = json.loads(ai_text)

        score = clamp(int(result.get('score', total_score * 0.6)), 0, total_score)
        percentage = clamp(int(result.get('percentage', (score / total_score) * 100)), 0, 100)

        payload = {
            'success': True,
            'data': {
                'success': True,
                'score': score,
                'totalScore': total_score,
                'percentage': percentage,
                'comment': result.get('comment', ''),
                'strengths': result.get('strengths', []),
                'improvements': result.get('improvements', [])
            }
        }
        return jsonify(payload)

    except Exception as e:
        import traceback
        print("评分失败，堆栈如下：")
        traceback.print_exc()
        try:
            with open(os.path.join(BASE_DIR, 'grading_error.log'), 'a', encoding='utf-8') as log_file:
                log_file.write('\n==== AI Grading Error ===\n')
                traceback.print_exc(file=log_file)
        except Exception as log_exc:
            print(f"写入grading_error.log失败: {log_exc}")
        fallback_score = int(total_score * 0.6)
        return jsonify({
            'success': True,
            'data': {
                'success': False,
                'score': fallback_score,
                'totalScore': total_score,
                'percentage': 60,
                'comment': f'自动评分失败，建议教师手动批改。原因：{str(e)}',
                'strengths': [],
                'improvements': ['请教师手动批改'],
                'error': str(e)
            }
        })

@app.route('/health', methods=['GET'])
def health():
    return jsonify({'status': 'ok'})

if __name__ == '__main__':
    print("🤖 智能助手服务启动中...")
    print("📡 运行在 http://localhost:5052")
    app.run(host='0.0.0.0', port=5052, debug=False)
