#!/bin/bash

# JavaEE课程管理系统 - 一键启动脚本

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color - 重置所有颜色属性，恢复终端默认颜色

cd $(dirname "$0")
PROJECT_DIR=$(pwd)
mkdir -p "$PROJECT_DIR/logs"

echo -e "${BLUE}🚀 JavaEE课程管理系统 - 一键启动${NC}"
echo "=================================="
echo ""

# 启动Docker服务
start_docker_services() {
    echo -e "${BLUE}🐳 启动Docker服务...${NC}"
    if ! docker info > /dev/null 2>&1; then
        echo -e "${YELLOW}⚠️  Docker未运行，跳过Docker服务启动${NC}"
        echo ""
        return 0
    fi
    
    # 启动SRS直播服务
    if [ -f "docker/docker-compose-srs.yml" ]; then
        echo -e "${YELLOW}启动SRS直播服务...${NC}"
        cd docker
        docker-compose -f docker-compose-srs.yml up -d
        cd ..
        echo -e "${GREEN}✅ SRS直播服务已启动${NC}"
    fi
    
    
    
    echo ""
}

# 强制清理端口函数
clean_port() {
    local port=$1
    local service_name=$2
    
    echo -e "${YELLOW}清理端口 ${port} (${service_name})...${NC}"
    
    # 使用lsof查找占用端口的进程
    local pids=$(lsof -ti:$port 2>/dev/null)
    
    if [ ! -z "$pids" ]; then
        echo -e "${RED}发现占用端口 ${port} 的进程: $pids${NC}"
        echo -e "${YELLOW}强制终止这些进程...${NC}"
        kill -9 $pids 2>/dev/null || true
        # 等待进程完全终止
        sleep 2
        # 再次检查，确保进程已终止
        local remaining_pids=$(lsof -ti:$port 2>/dev/null)
        if [ ! -z "$remaining_pids" ]; then
            echo -e "${RED}⚠️  仍有进程占用端口，尝试强制清理...${NC}"
            kill -9 $remaining_pids 2>/dev/null || true
            sleep 1
        fi
        echo -e "${GREEN}✅ 端口 ${port} 清理完成${NC}"
    else
        echo -e "${GREEN}✅ 端口 ${port} 未被占用${NC}"
    fi
}

# 关闭代理(避免端口冲突)
echo -e "${YELLOW}[0/7] 关闭代理设置...${NC}"
unset http_proxy https_proxy HTTP_PROXY HTTPS_PROXY all_proxy ALL_PROXY
export NO_PROXY="localhost,127.0.0.1"
echo -e "${GREEN}✅ 代理已关闭${NC}"

# 选择AI服务使用的Python解释器，避免误回退到系统Python
AI_PYTHON_BIN=""
pick_ai_python() {
    local candidates=(
        "$PROJECT_DIR/venv/bin/python3"
        "$PROJECT_DIR/.venv/bin/python3"
        "$(command -v python3 2>/dev/null || true)"
    )

    for candidate in "${candidates[@]}"; do
        if [ -x "$candidate" ] && "$candidate" -c "import dashscope, pymysql, flask, flask_cors, requests, openai" >/dev/null 2>&1; then
            AI_PYTHON_BIN="$candidate"
            return 0
        fi
    done

    for candidate in "${candidates[@]}"; do
        if [ -x "$candidate" ]; then
            AI_PYTHON_BIN="$candidate"
            return 0
        fi
    done

    return 1
}

# 1. 启动SRS流媒体服务器 (Docker)
echo ""
echo -e "${YELLOW}[1/7] 启动SRS流媒体服务器...${NC}"
if ! docker info > /dev/null 2>&1; then
    echo -e "${YELLOW}⚠️  Docker未运行，跳过SRS启动${NC}"
else
    echo -e "${CYAN}🎥 启动SRS流媒体服务...${NC}"
    # 停止旧容器（如果存在）
    docker-compose -f docker/docker-compose-srs.yml down 2>/dev/null || true
    # 启动SRS
    docker-compose -f docker/docker-compose-srs.yml up -d
    if docker ps | grep -q yali-srs; then
        echo -e "${GREEN}✅ SRS流媒体服务器启动成功${NC}"
        echo -e "${CYAN}   RTMP推流: rtmp://localhost:8000/live${NC}"
        echo -e "${CYAN}   HLS播放: http://localhost:8088${NC}"
    else
        echo -e "${YELLOW}⚠️  SRS启动失败，请检查Docker日志${NC}"
    fi
fi

# 2. 强制清理所有服务端口
echo ""
echo -e "${YELLOW}[2/7] 强制清理所有服务端口...${NC}"
clean_port 8080 "后端服务(Spring Boot)"
clean_port 3001 "前端服务(Vite)" 
clean_port 5051 "AI服务(Flask)"
clean_port 5052 "AI智能助手(Flask)"
echo -e "${GREEN}✅ 所有端口清理完成${NC}"

# 2.5. 启动Docker服务
start_docker_services

# 3. MySQL连接检查（当前服务器关闭，跳过）
echo ""
echo -e "${YELLOW}[3/7] 跳过MySQL连接检查（服务器关闭）...${NC}"

# 4. 启动后端
echo ""
echo -e "${YELLOW}[4/7] 启动后端服务 (Spring Boot)...${NC}"
cd "$PROJECT_DIR/back"

# 检查Maven包装器权限
if [ -f "mvnw" ]; then
    chmod +x mvnw
fi

echo -e "${CYAN}📦 编译后端项目...${NC}"
./mvnw clean package -DskipTests -q

echo -e "${CYAN}🚀 启动Spring Boot...${NC}"
nohup ./mvnw spring-boot:run > "$PROJECT_DIR/logs/backend.log" 2>&1 &
BACKEND_PID=$!
echo $BACKEND_PID > "$PROJECT_DIR/logs/backend.pid"
echo -e "${GREEN}✅ 后端已启动 (PID: $BACKEND_PID, 日志: logs/backend.log)${NC}"

# 等待后端启动
echo -e "${YELLOW}⏳ 等待后端就绪...${NC}"
BACKEND_READY=false
for i in {1..30}; do
    if curl --max-time 5 --noproxy '*' -s http://localhost:8080/course/all > /dev/null 2>&1; then
        echo -e "${GREEN}✅ 后端服务就绪 (尝试 $i/30)${NC}"
        BACKEND_READY=true
        break
    fi
    echo -e "${CYAN}   尝试 $i/30: 后端启动中...${NC}"
    sleep 2
done

if [ "$BACKEND_READY" = false ]; then
    echo -e "${RED}❌ 后端启动超时，请检查日志: tail -f $PROJECT_DIR/logs/backend.log${NC}"
    # 继续启动其他服务
fi

cd "$PROJECT_DIR"

# 5. 启动前端
echo ""
echo -e "${YELLOW}[5/7] 启动前端服务 (Vite)...${NC}"
cd "$PROJECT_DIR/front"

# 检查依赖
if [ ! -d "node_modules" ]; then
    echo -e "${CYAN}📦 安装前端依赖...${NC}"
    npm install
fi

echo -e "${CYAN}🚀 启动Vite开发服务器...${NC}"
nohup npm run dev > "$PROJECT_DIR/logs/frontend.log" 2>&1 &
FRONTEND_PID=$!
echo $FRONTEND_PID > "$PROJECT_DIR/logs/frontend.pid"
echo -e "${GREEN}✅ 前端已启动 (PID: $FRONTEND_PID, 日志: logs/frontend.log)${NC}"

# 等待前端启动
echo -e "${YELLOW}⏳ 等待前端就绪...${NC}"
FRONTEND_READY=false
for i in {1..20}; do
    if curl --max-time 5 -s http://localhost:3001 > /dev/null 2>&1; then
        echo -e "${GREEN}✅ 前端服务就绪 (尝试 $i/20)${NC}"
        FRONTEND_READY=true
        break
    fi
    echo -e "${CYAN}   尝试 $i/20: 前端启动中...${NC}"
    sleep 2
done

if [ "$FRONTEND_READY" = false ]; then
    echo -e "${RED}❌ 前端启动超时，请检查日志: tail -f $PROJECT_DIR/logs/frontend.log${NC}"
    # 继续启动其他服务
fi

cd "$PROJECT_DIR"

# 依赖检查
check_dependencies() {
    echo -e "${CYAN}🔍 检查Python依赖...${NC}"

    if [ -z "$AI_PYTHON_BIN" ]; then
        pick_ai_python || true
    fi

    if [ -n "$AI_PYTHON_BIN" ]; then
        echo -e "${GREEN}✅ 选择Python解释器: $AI_PYTHON_BIN${NC}"
    else
        echo -e "${YELLOW}⚠️  未找到可用Python解释器，回退到系统环境${NC}"
        AI_PYTHON_BIN="$(command -v python3)"
    fi

    local required_packages=("flask" "flask_cors" "requests" "openai")
    local missing_packages=()

    for package in "${required_packages[@]}"; do
        if "$AI_PYTHON_BIN" -c "import $package" 2>/dev/null; then
            echo -e "  ✅ $package 已安装"
        else
            echo -e "  ❌ $package 未安装"
            missing_packages+=("$package")
        fi
    done

    if [ ${#missing_packages[@]} -eq 0 ]; then
        echo -e "${GREEN}✅ 所有依赖已安装${NC}"
        return 0
    else
        echo -e "${YELLOW}⚠️  缺少依赖: ${missing_packages[*]}${NC}"
        return 1
    fi
}

# 6. AI服务启动
echo ""
echo -e "${YELLOW}[6/7] 启动AI服务 (Flask)...${NC}"

cd "$PROJECT_DIR"

# 先检查依赖状态
if check_dependencies; then
    echo -e "${GREEN}✅ 依赖检查完成，跳过安装步骤${NC}"
else
    echo -e "${YELLOW}⚠️  依赖不完整，跳过安装步骤（避免等待）${NC}"
    echo -e "${CYAN}💡 如需安装依赖，请手动运行: pip install -r requirements.txt${NC}"
fi

# 注释：不再启动AI.py，仅使用assistant.py作为智能助手服务
# echo -e "${CYAN}🚀 启动主AI服务 (AI.py)...${NC}"
# cd "$PROJECT_DIR/ai-service"
# nohup python3 AI.py > "$PROJECT_DIR/logs/ai_service.log" 2>&1 &
# AI_PID=$!
# echo $AI_PID > "$PROJECT_DIR/logs/ai_service.pid"
# echo -e "${GREEN}✅ 主AI服务已启动 (PID: $AI_PID, 日志: logs/ai_service.log)${NC}"
# sleep 2

cd "$PROJECT_DIR"
if [ -z "$AI_PYTHON_BIN" ]; then
    pick_ai_python || true
fi

echo -e "${GREEN}🎯 AI服务Python路径: ${AI_PYTHON_BIN:-$(command -v python3)}${NC}"

echo -e "${CYAN}🤖 启动智能助手服务 (assistant.py)...${NC}"
cd "$PROJECT_DIR/ai-service"
nohup "$AI_PYTHON_BIN" assistant.py > "$PROJECT_DIR/logs/ai_assistant.log" 2>&1 &
ASSISTANT_PID=$!
echo $ASSISTANT_PID > "$PROJECT_DIR/logs/ai_assistant.pid"
echo -e "${GREEN}✅ 智能助手服务已启动 (PID: $ASSISTANT_PID, 日志: logs/ai_assistant.log)${NC}"

echo -e "${CYAN}⚙️ 启动本地Piston兼容执行服务 (piston_compat.py)...${NC}"
nohup "$AI_PYTHON_BIN" piston_compat.py > "$PROJECT_DIR/logs/piston_compat.log" 2>&1 &
PISTON_PID=$!
echo $PISTON_PID > "$PROJECT_DIR/logs/piston_compat.pid"
echo -e "${GREEN}✅ 本地Piston兼容服务已启动 (PID: $PISTON_PID, 日志: logs/piston_compat.log)${NC}"

# 等待智能助手服务启动
echo -e "${YELLOW}⏳ 等待智能助手服务就绪...${NC}"
ASSISTANT_READY=false
for i in {1..15}; do
    if curl --max-time 5 -s http://localhost:5052/ > /dev/null 2>&1; then
        echo -e "${GREEN}✅ 智能助手服务就绪 (尝试 $i/15)${NC}"
        ASSISTANT_READY=true
        break
    fi
    echo -e "${CYAN}   尝试 $i/15: 智能助手启动中...${NC}"
    sleep 2
done

if [ "$ASSISTANT_READY" = false ]; then
    echo -e "${RED}❌ 智能助手启动超时，请检查日志: tail -f $PROJECT_DIR/logs/ai_assistant.log${NC}"
fi

# 等待本地Piston兼容执行服务启动
echo -e "${YELLOW}⏳ 等待本地Piston兼容服务就绪...${NC}"
PISTON_READY=false
for i in {1..15}; do
    if curl --max-time 5 -s http://localhost:5053/health > /dev/null 2>&1; then
        echo -e "${GREEN}✅ 本地Piston兼容服务就绪 (尝试 $i/15)${NC}"
        PISTON_READY=true
        break
    fi
    echo -e "${CYAN}   尝试 $i/15: Piston兼容服务启动中...${NC}"
    sleep 1
done

if [ "$PISTON_READY" = false ]; then
    echo -e "${RED}❌ 本地Piston兼容服务启动超时，请检查日志: tail -f $PROJECT_DIR/logs/piston_compat.log${NC}"
fi

cd "$PROJECT_DIR"

# 7. Piston API检查
echo ""
echo -e "${YELLOW}[7/7] 检查Piston代码执行API...${NC}"
if command -v jq >/dev/null 2>&1; then
    PISTON_RESPONSE=$(curl -s --max-time 10 http://localhost:5053/api/v2/piston/runtimes 2>/dev/null)
    if [ $? -eq 0 ] && [ ! -z "$PISTON_RESPONSE" ]; then
        PISTON_COUNT=$(echo "$PISTON_RESPONSE" | jq -r 'length' 2>/dev/null || echo "0")
        if [ "$PISTON_COUNT" -gt 0 ]; then
            echo -e "${GREEN}✅ 本地Piston兼容API正常 (支持 $PISTON_COUNT 种语言)${NC}"
        else
            echo -e "${YELLOW}⚠️  本地Piston兼容API响应错误${NC}"
        fi
    else
        echo -e "${YELLOW}⚠️  本地Piston兼容API连接失败${NC}"
    fi
else
    echo -e "${YELLOW}⚠️  jq命令未安装，跳过Piston API检查${NC}"
fi

echo ""
echo "=================================="
echo -e "${GREEN}✅ 系统启动完成!${NC}"
echo "=================================="
echo ""
echo -e "${BLUE}📱 访问地址:${NC}"
echo -e "   🌐 ${CYAN}前端: http://localhost:3001${NC}"
echo -e "   🔌 ${CYAN}后端: http://localhost:8080/api${NC}"
echo -e "   🤖 ${CYAN}智能助手: http://localhost:5052 (三端通用AI助手)${NC}"
echo ""
echo -e "${BLUE}直播功能:${NC}"
echo -e "   🎥 ${CYAN}直播管理(教师端): http://localhost:3001#/teacher/livestream${NC}"
echo -e "   👀 ${CYAN}观看直播(学生端): http://localhost:3001#/student/livestream${NC}"
echo ""
echo -e "${BLUE}👤 测试账号:${NC}"
echo -e "   🔑 ${PURPLE}管理员: admin / 123456${NC}"
echo -e "   👨‍🏫 ${PURPLE}教师:   teacher / 123456${NC}"
echo -e "   🎓 ${PURPLE}学生:   student / 123456${NC}"
echo -e "   🎓 ${PURPLE}学生2:  student001 / 123456${NC}"
echo ""
echo -e "${BLUE}💻 功能模块:${NC}"
echo -e "   • ${CYAN}课程管理: 选课、退课、课程列表${NC}"
echo -e "   • ${CYAN}作业系统: 发布作业、提交作业、批改${NC}"
echo -e "   • ${CYAN}在线考试: 题库练习、模拟考试、自动评分${NC}"
echo -e "   • ${CYAN}实验环境: 在线代码编辑器 (87种语言)${NC}"
echo -e "   • ${CYAN}讨论区:   课程讨论、问答互动${NC}"
echo -e "   • ${CYAN}资源库:   课件、视频、文档管理${NC}"
echo -e "   • ${CYAN}智能助手: AI对话、数据库查询、文件上传 (三端可用)${NC}"
echo ""
echo -e "${BLUE}🧪 快速体验代码执行:${NC}"
echo -e "   1. ${CYAN}访问 http://localhost:3001${NC}"
echo -e "   2. ${CYAN}登录: student / 123456${NC}"
echo -e "   3. ${CYAN}导航: 实验管理 → 开始实验${NC}"
echo -e "   4. ${CYAN}选择语言: Python/Java/C++/C 等87种${NC}"
echo -e "   5. ${CYAN}编写代码 → 点击运行 → 查看结果${NC}"
echo ""
echo -e "${BLUE}📊 服务状态:${NC}"
echo -n "   后端:   "
if lsof -i:8080 > /dev/null 2>&1; then echo -e "${GREEN}运行中${NC}"; else echo -e "${RED}未运行${NC}"; fi
echo -n "   前端:   "
if lsof -i:3001 > /dev/null 2>&1; then echo -e "${GREEN}运行中${NC}"; else echo -e "${RED}未运行${NC}"; fi
echo -n "   智能助手: "
if lsof -i:5052 > /dev/null 2>&1; then echo -e "${GREEN}运行中 (端口5052)${NC}"; else echo -e "${RED}未运行${NC}"; fi
echo -n "   代码执行: "
if lsof -i:5053 > /dev/null 2>&1; then echo -e "${GREEN}运行中 (端口5053)${NC}"; else echo -e "${RED}未运行${NC}"; fi
echo ""
echo -e "${BLUE}📝 日志文件:${NC}"
echo -e "   ${CYAN}后端日志: tail -f $PROJECT_DIR/logs/backend.log${NC}"
echo -e "   ${CYAN}前端日志: tail -f $PROJECT_DIR/logs/frontend.log${NC}"
echo -e "   ${CYAN}智能助手日志: tail -f $PROJECT_DIR/logs/ai_assistant.log${NC}"
echo -e "   ${CYAN}Piston兼容服务日志: tail -f $PROJECT_DIR/logs/piston_compat.log${NC}"
echo -e "   ${CYAN}SRS日志: docker logs -f yali-srs${NC}"
echo ""
echo -e "${BLUE}🛑 停止服务:${NC}"
echo -e "   ${CYAN}一键停止所有: ./stop.sh${NC}"
echo -e "   ${CYAN}手动停止应用: pkill -f 'spring-boot:run' && pkill -f 'vite' && pkill -f 'python3 assistant.py' && pkill -f 'python3 piston_compat.py'${NC}"
echo -e "   ${CYAN}停止Docker服务: cd docker && docker-compose -f docker-compose-srs.yml down${NC}"
echo ""
echo -e "${BLUE}🔧 进程PID文件:${NC}"
echo -e "   ${CYAN}后端PID: $PROJECT_DIR/logs/backend.pid${NC}"
echo -e "   ${CYAN}前端PID: $PROJECT_DIR/logs/frontend.pid${NC}"
echo -e "   ${CYAN}智能助手PID: $PROJECT_DIR/logs/ai_assistant.pid${NC}"
echo -e "   ${CYAN}Piston兼容服务PID: $PROJECT_DIR/logs/piston_compat.pid${NC}"
echo ""
