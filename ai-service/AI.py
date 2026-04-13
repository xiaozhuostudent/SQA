import time
from flask import Flask, request, jsonify, render_template, send_from_directory
from aippt import AIPPT
from openai import OpenAI
import requests
import dashscope
import json
import os

import asyncio
# 引入图像生成相关模块
from huggingface_hub import InferenceClient
from deep_translator import GoogleTranslator
from flask import Flask
from flask_cors import CORS

BASE_DIR = os.path.dirname(os.path.abspath(__file__))
PROJECT_ROOT = os.path.dirname(BASE_DIR)
DEFAULT_TEMPLATE_DIR = os.path.join(PROJECT_ROOT, "templates")
FALLBACK_TEMPLATE_DIR = os.path.join(BASE_DIR, "templates")
# Prefer the shared templates directory in the repo root, but fall back to a local folder if it exists
TEMPLATE_DIR = DEFAULT_TEMPLATE_DIR if os.path.isdir(DEFAULT_TEMPLATE_DIR) else FALLBACK_TEMPLATE_DIR

app = Flask(__name__, template_folder=TEMPLATE_DIR)
CORS(app) 
# ============== 通用配置 ==============
# 大模型对话相关
API_KEY = os.getenv("DASHSCOPE_API_KEY", "sk-cc859963a4c2462ea6bf822147df4bb0")

# aippt 配置（用于生成 PPT）
APP_ID = "975280f5"
API_SECRET = os.getenv("AIPPT_API_SECRET", "")

# 图像生成相关配置
HF_TOKEN = os.getenv("HF_TOKEN", "")
client = InferenceClient("stabilityai/stable-diffusion-3.5-large", token=HF_TOKEN)
translator = GoogleTranslator(source='zh-CN', target='en')

# 初始化OpenAI客户端
openai_client = OpenAI(
    api_key = os.getenv("DASHSCOPE_API_KEY", ""),
    base_url="https://dashscope.aliyuncs.com/compatible-mode/v1"
)
# 在全局配置区域添加会话历史存储
conversation_history = {}

# ============== 路由定义 ==============

@app.route("/")
def index():
    return render_template("myhtml.html")  # 渲染前端页面

@app.route("/chat", methods=["POST"])
def chat():
    data = request.get_json()

    if not data or "message" not in data:
        return jsonify({"error": "请求格式错误，必须包含 'message' 字段！"}), 400

    # 获取会话 ID，如果没有则创建新的
    session_id = data.get("session_id", "default")
    if session_id not in conversation_history:
        conversation_history[session_id] = []

    user_message = data["message"].strip()
    if not user_message:
        return jsonify({"error": "消息不能为空！"}), 400

    try:
        # 构建包含历史对话的消息列表
        messages = conversation_history[session_id] + [
            {"role": "user", "content": user_message}
        ]

        # 调用大模型对话 API
        response = dashscope.Generation.call(
            model="qwen-plus",  # 可换成 qwen-turbo
            api_key=API_KEY,
            messages=messages
        )

        print("API 返回结果:", response)  # 输出调试信息

        if response and "output" in response and "text" in response["output"]:
            bot_reply = response["output"]["text"]
            
            # 更新对话历史
            conversation_history[session_id].append({"role": "user", "content": user_message})
            conversation_history[session_id].append({"role": "assistant", "content": bot_reply})
            
            # 保持历史记录在合理范围内（例如最近10轮对话）
            if len(conversation_history[session_id]) > 20:  # 保留10轮对话（每轮包含问答各一条）
                conversation_history[session_id] = conversation_history[session_id][-20:]
        else:
            bot_reply = "API 响应异常，请检查 API 访问情况。"

        return jsonify({
            "reply": bot_reply,
            "session_id": session_id
        })

    except Exception as e:
        return jsonify({"error": "请求失败", "details": str(e)}), 500

@app.route("/createppt", methods=["POST"])
def create_ppt():
    # 获取前端传来的数据
    data = request.json
    word_document_url = data.get('wordDocumentUrl')  # 获取 Word 文档的 URL

    # 如果没有提供 Word 文档的 URL，返回错误
    if not word_document_url:
        return jsonify({"error": "Word 文档的 URL 必须提供"}), 400
    try:
        print(f"收到创建 PPT 文档请求，Word 文档 URL: {word_document_url}")

        title = "教学"   # 设定大纲主题
        filename = "有丝分裂PPT.docx"
        templateId = "20240718489569D"
        fileUrl = word_document_url
        demo = AIPPT(APP_ID, API_SECRET, title, templateId)
        res = demo.createOutlineByDoc(fileName=filename, fileUrl=fileUrl)
        data = json.loads(res)
        outline = data["data"]["outline"]
        taskid = demo.createPptByOutline(outline)

        result = demo.get_result(taskid)
        ppt_url = result
        return jsonify({"success": True, "message": "PPT 文档创建成功", "pptUrl": ppt_url})

    except Exception as e:
        return jsonify({"error": "创建 PPT 文档失败", "details": str(e)}), 500

@app.route("/searchpicture", methods=["POST"])
def search_picture():
    # 获取前端传来的查询参数
    query = request.json.get('query', '').strip()
    if not query:
        return jsonify({"status": "error", "message": "查询参数不能为空"}), 400

    # 设置 API 密钥和搜索引擎 ID
    google_api_key = os.getenv("GOOGLE_API_KEY", "")
    cx = 'b077fcf1ff7504041'  # 你在 Google Custom Search 中设置的搜索引擎 ID

    # 构建请求 URL
    url = f'https://www.googleapis.com/customsearch/v1?q={query}&cx={cx}&searchType=image&key={google_api_key}'

    # 发送请求到 Google Custom Search API
    response = requests.get(url)
    if response.status_code != 200:
        return jsonify({"status": "error", "message": "API 请求失败", "details": response.text}), 500

    # 解析响应数据
    data = response.json()
    image_urls = [item['link'] for item in data.get('items', [])]

    if not image_urls:
        return jsonify({"status": "error", "message": "未找到相关图片"}), 404

    # 返回图片 URL 列表
    return jsonify({"status": "success", "images": image_urls})

@app.route("/generate_image", methods=["POST"])
def generate_image():
    data = request.get_json()
    prompt = data.get("prompt", "").strip()
    if not prompt:
        return jsonify({"success": False, "error": "提示词不能为空"}), 400

    try:
        # 翻译提示词到英文
        english_prompt = translator.translate(prompt)
        print(f"Translated Prompt: {english_prompt}")

        # 生成图像
        image = client.text_to_image(english_prompt)

        # 确保 static 文件夹存在
        static_folder = os.path.join(os.getcwd(), "static")
        if not os.path.exists(static_folder):
            os.makedirs(static_folder)

        # 使用时间戳生成唯一文件名
        timestamp = int(time.time() * 1000)
        image_filename = f"generated_image_{timestamp}.png"
        image_path = os.path.join(static_folder, image_filename)
        image.save(image_path)

        # 返回图片URL
        image_url = f"/static/{image_filename}"
        return jsonify({"success": True, "imageUrl": image_url})

    except Exception as e:
        print(f"图像生成错误: {str(e)}")
        return jsonify({"success": False, "error": str(e)}), 500


@app.route("/deep_thinking", methods=["POST"])
def deep_thinking():
    data = request.get_json()
    message = data.get("message", "").strip()
    
    if not message:
        return jsonify({"error": "消息不能为空"}), 400
        
    try:
        # 初始化会话历史
        session_id = data.get("session_id", "default")
        if session_id not in conversation_history:
            conversation_history[session_id] = []
            
        # 添加用户消息到历史记录
        conversation_history[session_id].append({
            "role": "user", 
            "content": message
        })
        
        # 创建聊天完成请求
        completion = openai_client.chat.completions.create(
            model="qwq-32b",
            messages=conversation_history[session_id],
            stream=True
        )
        
        reasoning_content = ""
        answer_content = ""
        is_answering = False
        
        # 处理流式响应
        for chunk in completion:
            if not chunk.choices:
                continue
                
            delta = chunk.choices[0].delta
            
            # 收集思考过程
            if hasattr(delta, 'reasoning_content') and delta.reasoning_content:
                reasoning_content += delta.reasoning_content
            
            # 收集回答内容
            elif delta.content:
                if not is_answering and delta.content.strip():
                    is_answering = True
                answer_content += delta.content
        
        # 更新对话历史
        conversation_history[session_id].append({
            "role": "assistant",
            "content": answer_content
        })
        
        # 保持历史记录在合理范围内
        if len(conversation_history[session_id]) > 20:
            conversation_history[session_id] = conversation_history[session_id][-20:]
        
        return jsonify({
            "success": True,
            "reasoning_content": reasoning_content,
            "answer_content": answer_content,
            "session_id": session_id
        })
        
    except Exception as e:
        print(f"深度思考错误: {str(e)}")
        return jsonify({
            "success": False,
            "error": str(e)
        }), 500
@app.route("/searchvideo", methods=["POST"])
def searchvideo():
    try:
        # 获取请求参数
        data = request.get_json()
        keyword = data.get("keyword", "有丝分裂")  # 默认搜索关键词
        page_size = int(data.get("page_size", 20))  # 默认返回1条结果
        
        # 运行异步搜索
        async def async_search():
            result = await search.search_by_type(
                keyword, 
                search_type=search.SearchObjectType.VIDEO, 
                page_size=page_size
            )
            
            if not result or "result" not in result or len(result["result"]) == 0:
                return None

            videos = []
            for video in result["result"]:
                videos.append({
                    "title": video["title"].replace('<em class="keyword">', '').replace('</em>', ''),
                    "bvid": video["bvid"],
                    "author": video["author"],
                    "play": video["play"],
                    "duration": video["duration"],
                    "cover": f"https:{video['pic']}",
                    "url": f"https://www.bilibili.com/video/{video['bvid']}",
                    "pubdate": video["pubdate"]
                })
            
            return {
                "total": result.get("numResults", 0),
                "videos": videos
            }

        # 创建新事件循环运行异步函数
        loop = asyncio.new_event_loop()
        asyncio.set_event_loop(loop)
        search_result = loop.run_until_complete(async_search())
        loop.close()

        if not search_result:
            return jsonify({
                "code": 404,
                "message": "未找到相关视频"
            }), 404

        return jsonify({
            "code": 200,
            "data": search_result
        })

    except Exception as e:
        return jsonify({
            "code": 500,
            "message": f"服务器错误: {str(e)}"
        }), 500


if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5051, debug=True)
