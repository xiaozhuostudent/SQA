#!/usr/bin/env python3
# -*- coding: utf-8 -*-

"""
JavaEE课程管理系统 - 一键启动脚本 (跨平台版)
支持 Windows / Linux / macOS
使用远程 MySQL 数据库（无需本地 Docker）
"""

import os
import sys
import time
import subprocess
import socket
import platform
from pathlib import Path
from subprocess import STDOUT

# 颜色定义（Windows PowerShell 支持 ANSI 转义码，Win10+ 默认开启）
RED = '\033[0;31m'
GREEN = '\033[0;32m'
YELLOW = '\033[1;33m'
BLUE = '\033[0;34m'
NC = '\033[0m'  # No Color

PROJECT_DIR = Path(__file__).parent.resolve()
os.chdir(PROJECT_DIR)

# === Maven 本地仓库路径（规避 Windows 中文用户名/编码导致的 classpath 乱码问题）===
# 可通过环境变量 MAVEN_REPO_LOCAL 覆盖。
MAVEN_REPO_LOCAL = os.environ.get("MAVEN_REPO_LOCAL") or str((PROJECT_DIR / ".m2repo").resolve())

AI_SERVICE_DIR = PROJECT_DIR / "ai-service"
AI_SERVICE_PORT = 5052

# 优先使用当前解释器，便于跨平台一致
PYTHON_CMD = str(sys.executable) if sys.executable else (
    "python.exe" if platform.system() == "Windows" else "python3"
)

# === 数据库配置（默认连接远程 MySQL）===
# 说明：start.py 这里只做端口可达性检测；真正的后端连接请看 back/src/main/resources/application.properties
DB_HOST = "47.96.254.64"
DB_PORT = 3306
DB_NAME = "javaee"
DB_USER = "cuigu"
DB_PASS = "cuiguzjuter"

IS_WINDOWS = platform.system() == "Windows"


def print_color(msg, color=NC):
    print(f"{color}{msg}{NC}")


def run_cmd(cmd, shell=None, check=False, capture_output=False):
    if shell is None:
        shell = IS_WINDOWS  # Windows 通常需要 shell=True 执行 .cmd
    return subprocess.run(
        cmd,
        shell=shell,
        check=check,
        capture_output=capture_output,
        text=True,
        encoding='utf-8',
        errors='replace'
    )


def get_mvnw_command(*args):
    """返回适用于当前操作系统的 mvnw 命令列表"""
    base = "mvnw.cmd" if IS_WINDOWS else "./mvnw"
    # 统一指定本地仓库到纯英文路径，避免 fork JVM 时 classpath 指向乱码目录
    return [base, f"-Dmaven.repo.local={MAVEN_REPO_LOCAL}"] + list(args)


def is_port_in_use(port):
    """检查本地端口是否被占用"""
    try:
        if IS_WINDOWS:
            # Windows: 检查是否有 LISTENING 状态的端口
            result = subprocess.run(
                ["netstat", "-ano"],
                capture_output=True, text=True
            )
            # 只检查 LISTENING 状态,排除 ESTABLISHED 等其他状态
            for line in result.stdout.split('\n'):
                if 'LISTENING' in line and f':{port}' in line:
                    return True
            return False
        else:
            result = run_cmd(f"lsof -i:{port}", capture_output=True)
            return result.returncode == 0 and result.stdout.strip()
    except Exception:
        return False


def is_port_open(host, port, timeout=3):
    """检测远程主机端口是否开放"""
    try:
        with socket.create_connection((host, port), timeout=timeout):
            return True
    except (socket.timeout, ConnectionRefusedError, OSError):
        return False


def wait_for_url(url, max_retries=30, interval=2):
    for i in range(1, max_retries + 1):
        try:
            # 在 Windows 上 curl 可能不可用，改用 Python requests？但为保持轻量仍用 curl
            # 若系统无 curl，可后续替换为 urllib
            cmd = f"curl --noproxy '*' -s {url}"
            result = run_cmd(cmd, capture_output=True)
            if result.returncode == 0:
                return True
        except Exception:
            pass
        if i == max_retries:
            return False
        time.sleep(interval)
    return False


def main():
    print_color("🚀 JavaEE课程管理系统 - 一键启动", BLUE)
    print("==================================\n")

    # 确保 Maven 本地仓库目录存在
    try:
        Path(MAVEN_REPO_LOCAL).mkdir(parents=True, exist_ok=True)
        print_color(f"Maven 本地仓库: {MAVEN_REPO_LOCAL}", BLUE)
    except Exception as e:
        print_color(f"⚠️  无法创建 Maven 本地仓库目录: {MAVEN_REPO_LOCAL} ({e})", YELLOW)

    # [0/4] 关闭代理
    print_color("[0/4] 关闭代理设置...", YELLOW)
    env_vars_to_unset = ['http_proxy', 'https_proxy', 'HTTP_PROXY', 'HTTPS_PROXY', 'all_proxy', 'ALL_PROXY']
    for var in env_vars_to_unset:
        os.environ.pop(var, None)
    os.environ['NO_PROXY'] = "localhost,127.0.0.1"
    print("✅ 代理已关闭\n")

    # [1/4] 检查远程 MySQL
    print_color("[1/4] 检查远程MySQL数据库连接...", YELLOW)
    if is_port_open(DB_HOST, DB_PORT):
        print(f"✅ 远程MySQL ({DB_HOST}:{DB_PORT}) 可访问")
    else:
        print_color(f"⚠️  无法连接远程MySQL ({DB_HOST}:{DB_PORT})，请检查网络或防火墙", YELLOW)

    # [2/4] 启动后端
    print_color("\n[2/4] 启动后端服务 (Spring Boot)...", YELLOW)
    if is_port_in_use(8080):
        print("⚠️  端口8080已被占用,跳过后端启动")
    else:
        back_dir = PROJECT_DIR / "back"
        if not back_dir.exists():
            print_color("❌ 'back' 目录不存在，请确认项目结构", RED)
            sys.exit(1)
        os.chdir(back_dir)

        if not (back_dir / "pom.xml").exists():
            print_color("❌ 未找到 pom.xml，'back' 不是有效的 Maven 项目", RED)
            sys.exit(1)

        # 编译
        print("📦 编译后端项目...")
        
        # 设置 JAVA_HOME 环境变量（Windows）
        if IS_WINDOWS:
            java21_path = r"D:\develop\Java\jdk-21"
            if os.path.exists(java21_path):
                os.environ["JAVA_HOME"] = java21_path
                os.environ["PATH"] = f"{java21_path}\\bin;{os.environ.get('PATH', '')}"
                print(f"   使用 Java 21: {java21_path}")
        
        mvnw_build = get_mvnw_command("clean", "package", "-DskipTests", "-q")
        run_cmd(mvnw_build, check=True)

        # 启动
        print("🚀 启动Spring Boot...")
        backend_log_path = PROJECT_DIR / "backend.log"

        if IS_WINDOWS:
            # Windows: 使用 cmd /c start 在新窗口中启动
            mvnw_cmd = str(back_dir / "mvnw.cmd")
            # 使用 cmd 启动新的 PowerShell 窗口
            # Windows 下禁用 fork（再加上固定 maven.repo.local），避免 classpath 乱码导致 NoClassDefFoundError
            cmd = (
                f'start "后端服务" /D "{back_dir}" cmd /c "'
                f'mvnw.cmd -Dmaven.repo.local={MAVEN_REPO_LOCAL} -Dspring-boot.run.fork=false '
                f'spring-boot:run > {backend_log_path} 2>&1"'
            )
            subprocess.Popen(
                cmd,
                shell=True,
                creationflags=subprocess.CREATE_NO_WINDOW
            )
            print(f"✅ 后端已启动 (新窗口, 日志: {backend_log_path.name})")
            print("   💡 提示: 后端在新的命令行窗口中运行,关闭该窗口即可停止后端")
        else:
            backend_log = open(backend_log_path, "w", encoding="utf-8")
            mvnw_run = get_mvnw_command("spring-boot:run")
            backend_proc = subprocess.Popen(
                mvnw_run,
                stdout=backend_log,
                stderr=subprocess.STDOUT,
                preexec_fn=os.setsid
            )
            print(f"✅ 后端已启动 (PID: {backend_proc.pid}, 日志: {backend_log_path.name})")

        print("⏳ 等待后端就绪...")
        if not wait_for_url("http://localhost:8080/course/all", max_retries=30):
            print_color("⚠️  后端启动超时,请检查 backend.log", RED)

    os.chdir(PROJECT_DIR)

    # [3/4] 启动AI评分服务
    print_color("\n[3/4] 启动AI评分服务 (Flask)...", YELLOW)
    if is_port_in_use(AI_SERVICE_PORT):
        print(f"⚠️  端口{AI_SERVICE_PORT}已被占用,跳过AI服务启动")
    else:
        if not AI_SERVICE_DIR.exists():
            print_color("❌ 'ai-service' 目录不存在", RED)
        elif not (AI_SERVICE_DIR / "assistant.py").exists():
            print_color("❌ 未找到 assistant.py，无法启动AI服务", RED)
        else:
            ai_log_path = PROJECT_DIR / "ai_service.log"
            ai_env = os.environ.copy()
            ai_env["PYTHONUTF8"] = "1"
            ai_env["PYTHONIOENCODING"] = "utf-8"

            ai_log = open(ai_log_path, "w", encoding="utf-8")
            ai_proc = subprocess.Popen(
                [PYTHON_CMD, "assistant.py"],
                cwd=AI_SERVICE_DIR,
                stdout=ai_log,
                stderr=STDOUT,
                env=ai_env,
                preexec_fn=os.setsid if (not IS_WINDOWS and hasattr(os, 'setsid')) else None
            )
            print(f"✅ AI服务已启动 (PID: {ai_proc.pid}, 日志: {ai_log_path.name})")
            print("   💡 提示: AI服务以后台进程方式运行, 查看 ai_service.log 可获取最新输出")

            print("⏳ 等待AI服务就绪...")
            if not wait_for_url(f"http://localhost:{AI_SERVICE_PORT}/health", max_retries=20):
                print_color("⚠️  AI服务启动超时,请检查 ai_service.log", RED)

    os.chdir(PROJECT_DIR)

    # [4/4] 启动前端
    print_color("\n[4/4] 启动前端服务 (Vite)...", YELLOW)
    if is_port_in_use(3000) or is_port_in_use(3001):
        print("⚠️  端口3000/3001已被占用,跳过前端启动")
    else:
        front_dir = PROJECT_DIR / "front"
        if not front_dir.exists():
            print_color("❌ 'front' 目录不存在", RED)
            sys.exit(1)
        os.chdir(front_dir)

        if not (front_dir / "package.json").exists():
            print_color("❌ 未找到 package.json，'front' 不是有效的 Node.js 项目", RED)
            sys.exit(1)

        if not (front_dir / "node_modules").exists():
            print("📦 安装前端依赖...")
            run_cmd("npm install", check=True)

        print("🚀 启动Vite开发服务器...")
        frontend_log_path = PROJECT_DIR / "frontend.log"

        if IS_WINDOWS:
            # Windows: 在新窗口中启动前端
            cmd = f'start "前端服务" /D "{front_dir}" cmd /c "npm run dev > {frontend_log_path} 2>&1"'
            subprocess.Popen(
                cmd,
                shell=True,
                creationflags=subprocess.CREATE_NO_WINDOW
            )
            print(f"✅ 前端已启动 (新窗口, 日志: {frontend_log_path.name})")
            print("   💡 提示: 前端在新的命令行窗口中运行,关闭该窗口即可停止前端")
        else:
            frontend_log = open(frontend_log_path, "w", encoding="utf-8")
            frontend_proc = subprocess.Popen(
                ["npm", "run", "dev"],
                stdout=frontend_log,
                stderr=subprocess.STDOUT
            )
            print(f"✅ 前端已启动 (PID: {frontend_proc.pid}, 日志: {frontend_log_path.name})")

        print("⏳ 等待前端就绪...")
        # Vite可能使用3000或3001端口
        frontend_ready = wait_for_url("http://localhost:3000", max_retries=10) or \
                        wait_for_url("http://localhost:3001", max_retries=10)
        if not frontend_ready:
            print_color("⚠️  前端启动超时,请检查 frontend.log", RED)

    os.chdir(PROJECT_DIR)

    # 验证 Piston API
    print_color("\n[✓] 验证Piston代码执行API...", YELLOW)
    try:
        result = run_cmd("curl -s https://emkc.org/api/v2/piston/runtimes", capture_output=True)
        if result.returncode == 0:
            import json
            runtimes = json.loads(result.stdout)
            if isinstance(runtimes, list) and len(runtimes) > 0:
                print(f"✅ Piston API正常 (支持 {len(runtimes)} 种语言)")
            else:
                print_color("⚠️  Piston API响应异常,但不影响其他功能", YELLOW)
        else:
            print_color("⚠️  Piston API请求失败,但不影响其他功能", YELLOW)
    except Exception as e:
        print_color(f"⚠️  Piston API验证出错: {e},但不影响其他功能", YELLOW)

    # 最终输出
    print("\n==================================")
    print_color("✅ 系统启动完成!", GREEN)
    print("==================================\n")

    print_color("📱 访问地址:", BLUE)
    # 检测前端实际端口
    if is_port_in_use(3000):
        print("   🌐 前端: http://localhost:3000")
    elif is_port_in_use(3001):
        print("   🌐 前端: http://localhost:3001")
    else:
        print("   🌐 前端: http://localhost:3000 (未运行)")
    print("   🔌 后端: http://localhost:8080/api\n")

    print_color("👤 测试账号:", BLUE)
    print("   🔑 管理员: admin / password")
    print("   👨‍🏫 教师:   teacher / password")
    print("   🎓 学生:   student / password")
    print("   🎓 学生2:  student001 / password\n")

    print_color("💻 功能模块:", BLUE)
    print("   • 课程管理: 选课、退课、课程列表")
    print("   • 作业系统: 发布作业、提交作业、批改")
    print("   • 在线考试: 题库练习、模拟考试、自动评分")
    print("   • 实验环境: 在线代码编辑器 (87种语言)")
    print("   • 讨论区:   课程讨论、问答互动")
    print("   • 资源库:   课件、视频、文档管理\n")

    print_color("🧪 快速体验代码执行:", BLUE)
    frontend_url = "http://localhost:3001" if is_port_in_use(3001) else "http://localhost:3000"
    print(f"   1. 访问 {frontend_url}")
    print("   2. 登录: student / password")
    print("   3. 导航: 实验管理 → 开始实验")
    print("   4. 选择语言: Python/Java/C++/C 等87种")
    print("   5. 编写代码 → 点击运行 → 查看结果\n")

    print_color("📊 服务状态:", BLUE)
    print("   MySQL:  ", end="")
    if is_port_open(DB_HOST, DB_PORT):
        print_color(f"远程 ({DB_HOST}) 可访问", GREEN)
    else:
        print_color(f"远程 ({DB_HOST}) 不可达", RED)

    print("   后端:   ", end="")
    print_color("运行中", GREEN) if is_port_in_use(8080) else print_color("未运行", RED)

    print("   AI服务: ", end="")
    print_color("运行中", GREEN) if is_port_in_use(AI_SERVICE_PORT) else print_color("未运行", RED)

    print("   前端:   ", end="")
    if is_port_in_use(3000) or is_port_in_use(3001):
        print_color("运行中", GREEN)
    else:
        print_color("未运行", RED)
    print()

    print_color("📝 日志文件:", BLUE)
    print(f"   后端日志: {PROJECT_DIR}/backend.log")
    print(f"   前端日志: {PROJECT_DIR}/frontend.log")
    print(f"   AI服务日志: {PROJECT_DIR}/ai_service.log\n")

    print_color("🛑 停止服务:", BLUE)
    if IS_WINDOWS:
        print("   方式1: 直接关闭后端、前端窗口 (AI服务在后台运行)")
        print("   方式2: taskkill /f /im java.exe (谨慎: 会结束所有 Java 进程)")
        print("   方式3: taskkill /f /im node.exe (谨慎: 会结束所有 Node 进程)")
        print("   方式4: taskkill /f /im python.exe (结束AI服务后台进程)")
    else:
        print("   pkill -f 'spring-boot:run' && pkill -f 'vite'")
    print("\n==================================")


if __name__ == "__main__":
    try:
        main()
    except KeyboardInterrupt:
        print("\n🛑 用户中断")
        sys.exit(0)
    except subprocess.CalledProcessError as e:
        print_color(f"❌ 命令执行失败: {e}", RED)
        if e.stderr:
            print(f"stderr: {e.stderr}")
        sys.exit(1)
    except Exception as e:
        print_color(f"❌ 脚本异常: {e}", RED)
        sys.exit(1)