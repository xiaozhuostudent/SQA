#!/usr/bin/env python3
# -*- coding: utf-8 -*-

"""
JavaEE 课程管理系统 - Windows专用启动脚本
===========================================
适用于 Windows 系统
开局清除代理配置，避免影响项目请求调用
假设 Docker 镜像已预下载，直接启动服务
===========================================
"""

import os
import sys
import platform
import subprocess
import time
import socket
import urllib.request
from pathlib import Path

# 颜色定义 (Windows兼容)
class Colors:
    RED = '\033[0;31m'
    GREEN = '\033[0;32m'
    YELLOW = '\033[1;33m'
    BLUE = '\033[0;34m'
    PURPLE = '\033[0;35m'
    CYAN = '\033[0;36m'
    NC = '\033[0m'  # No Color

    @staticmethod
    def disable_on_windows():
        """启用 Windows ANSI 转义码支持"""
        if platform.system() == "Windows":
            os.system('')

Colors.disable_on_windows()

PROJECT_DIR = Path(__file__).parent.resolve()
IS_WINDOWS = platform.system() == "Windows"

def print_color(msg, color=Colors.NC):
    """打印带颜色的消息"""
    print(f"{color}{msg}{Colors.NC}")

def print_banner():
    """打印欢迎横幅"""
    print_color("\n" + "="*60, Colors.BLUE)
    print_color("    JavaEE 课程管理系统 - Windows启动脚本", Colors.BLUE)
    print_color("="*60 + "\n", Colors.BLUE)

def clear_proxy_settings():
    """清除系统代理设置"""
    print_color("🔧 清除代理配置...", Colors.YELLOW)

    try:
        # 清除环境变量中的代理设置
        proxy_env_vars = [
            'HTTP_PROXY', 'HTTPS_PROXY', 'http_proxy', 'https_proxy',
            'ALL_PROXY', 'all_proxy', 'NO_PROXY', 'no_proxy'
        ]

        cleared_vars = []
        for var in proxy_env_vars:
            if var in os.environ:
                del os.environ[var]
                cleared_vars.append(var)

        if cleared_vars:
            print_color(f"   ✅ 清除环境变量: {', '.join(cleared_vars)}", Colors.GREEN)
        else:
            print_color("   ℹ️  未发现代理环境变量", Colors.CYAN)

        # Windows 特有的代理清除
        if IS_WINDOWS:
            try:
                # 清除 Windows 系统代理
                subprocess.run(['netsh', 'winhttp', 'reset', 'proxy'],
                             stdout=subprocess.PIPE, stderr=subprocess.PIPE, check=False)
                print_color("   ✅ 清除 Windows 系统代理", Colors.GREEN)
            except:
                print_color("   ⚠️  无法清除 Windows 系统代理（需要管理员权限）", Colors.YELLOW)

        print_color("✅ 代理配置清除完成", Colors.GREEN)

    except Exception as e:
        print_color(f"⚠️  代理清除过程中出错: {e}", Colors.YELLOW)

def disable_proxy_tools():
    """禁用常见的代理工具"""
    print_color("\n🔌 检查并禁用代理工具...", Colors.YELLOW)

    proxy_processes = [
        'clash.exe', 'v2ray.exe', 'ssr.exe', 'shadowsocks.exe',
        'proxifier.exe', 'tor.exe', 'privoxy.exe'
    ]

    found_processes = []
    for proc in proxy_processes:
        try:
            # 检查进程是否存在
            result = subprocess.run(['tasklist', '/FI', f'IMAGENAME eq {proc}', '/NH'],
                                  stdout=subprocess.PIPE, stderr=subprocess.PIPE, text=True)
            if proc.lower().replace('.exe', '') in result.stdout.lower():
                found_processes.append(proc)
        except:
            pass

    if found_processes:
        print_color(f"   ⚠️  发现运行中的代理进程: {', '.join(found_processes)}", Colors.YELLOW)
        print_color("   💡 请手动关闭这些代理工具以避免网络问题", Colors.CYAN)
    else:
        print_color("   ✅ 未发现运行中的代理工具", Colors.GREEN)

def check_command(cmd):
    """检查命令是否存在"""
    try:
        subprocess.run([cmd, '--version'], stdout=subprocess.PIPE, stderr=subprocess.PIPE, timeout=5)
        return True
    except:
        return False

def check_docker():
    """检查 Docker 是否运行"""
    try:
        result = subprocess.run(['docker', 'info'], stdout=subprocess.PIPE, stderr=subprocess.PIPE, timeout=10)
        return result.returncode == 0
    except:
        return False

def check_docker_compose():
    """检查 Docker Compose 命令"""
    # 优先检查新版命令
    if check_command("docker"):
        try:
            result = subprocess.run(['docker', 'compose', 'version'],
                                  stdout=subprocess.PIPE, stderr=subprocess.PIPE, timeout=5)
            if result.returncode == 0:
                return "docker compose"
        except:
            pass

    # 检查旧版命令
    if check_command("docker-compose"):
        return "docker-compose"

    return None

def wait_for_service(url, max_retries=30, delay=2):
    """等待服务就绪（无代理版本）"""
    print_color(f"   正在检查 {url}...", Colors.CYAN)

    # 创建无代理的 opener
    proxy_handler = urllib.request.ProxyHandler({})
    opener = urllib.request.build_opener(proxy_handler)
    urllib.request.install_opener(opener)

    for i in range(max_retries):
        try:
            req = urllib.request.Request(url, headers={
                'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36'
            })
            with urllib.request.urlopen(req, timeout=5) as response:
                if response.status == 200:
                    print_color(f"   ✅ 服务响应正常", Colors.GREEN)
                    return True
        except Exception as e:

            if i == max_retries - 1:
                print_color(f"   ❌ 连接失败: {e}", Colors.RED)
            else:
                print_color(f"   ⏳ 等待中... ({i+1}/{max_retries})", Colors.YELLOW)
                time.sleep(delay)

    return False

def check_environment():
    """检查运行环境"""
    print_color("="*60, Colors.BLUE)
    print_color("   环境检查", Colors.BLUE)
    print_color("="*60, Colors.BLUE)
    print()

    issues = []

    # 检查 Python
    print_color("🐍 检查 Python...", Colors.CYAN)
    version = sys.version_info
    if version.major >= 3 and version.minor >= 7:
        print_color(f"   ✅ Python {version.major}.{version.minor}.{version.micro} (>= 3.7 ✓)", Colors.GREEN)
    else:
        print_color(f"   ❌ Python {version.major}.{version.minor}.{version.micro} (需要 >= 3.7)", Colors.RED)
        issues.append('Python版本过低')

    # 检查 Docker
    print_color("\n🐳 检查 Docker...", Colors.CYAN)
    if check_docker():
        print_color("   ✅ Docker Desktop 运行中", Colors.GREEN)
    else:
        print_color("   ❌ Docker Desktop 未运行", Colors.RED)
        print_color("   💡 请启动 Docker Desktop 应用", Colors.CYAN)
        issues.append('Docker未运行')

    # 检查 Docker Compose
    print_color("\n🔧 检查 Docker Compose...", Colors.CYAN)
    compose_cmd = check_docker_compose()
    if compose_cmd:
        print_color(f"   ✅ {compose_cmd} 可用", Colors.GREEN)
    else:
        print_color("   ❌ Docker Compose 未找到", Colors.RED)
        issues.append('Docker Compose未找到')

    print()
    print_color("="*60, Colors.BLUE)

    return issues, compose_cmd

def docker_start(compose_cmd):
    """Docker 启动方式"""
    print_color("\n🚀 启动 Docker 服务...", Colors.BLUE)
    print_color("="*60, Colors.BLUE)

    # 1. 停止可能存在的旧服务
    print_color("\n[1/3] 清理旧服务...", Colors.YELLOW)
    try:
        if compose_cmd == "docker compose":
            subprocess.run(["docker", "compose", "down"], stdout=subprocess.PIPE, stderr=subprocess.PIPE, timeout=30)
        else:
            subprocess.run(["docker-compose", "down"], stdout=subprocess.PIPE, stderr=subprocess.PIPE, timeout=30)
        print_color("✅ 清理完成", Colors.GREEN)
    except Exception as e:
        print_color(f"⚠️  清理过程出错: {e}", Colors.YELLOW)

    # 2. 启动服务（假设镜像已下载）
    print_color("\n[2/3] 启动服务（使用已下载的镜像）...", Colors.YELLOW)
    try:
        if compose_cmd == "docker compose":
            result = subprocess.run(["docker", "compose", "up", "-d"], timeout=120)
        else:
            result = subprocess.run(["docker-compose", "up", "-d"], timeout=120)

        if result.returncode == 0:
            print_color("✅ 服务启动成功", Colors.GREEN)
        else:
            print_color("❌ 服务启动失败", Colors.RED)
            return False
    except subprocess.TimeoutExpired:
        print_color("⚠️  启动超时，但服务可能仍在后台启动", Colors.YELLOW)
    except Exception as e:
        print_color(f"❌ 启动过程出错: {e}", Colors.RED)
        return False

    # 3. 检查服务状态
    print_color("\n[3/3] 检查服务状态...", Colors.YELLOW)

    services = [
        ("Redis缓存服务", "http://localhost:6379", 6379, "TCP"),  # Redis 使用 TCP 连接检查
        ("后端服务", "http://localhost:8080/course/all", 8080, "HTTP"),
        ("前端服务", "http://localhost:3001", 3001, "HTTP"),
        ("AI服务", "http://localhost:5051", 5051, "HTTP"),
        ("OnlyOffice文档服务", "http://localhost:8081/healthcheck", 8081, "HTTP"),
        ("SRS直播服务", "http://localhost:1985/api/v1/versions", 1985, "HTTP")
    ]

    all_ready = True
    for name, url, port, check_type in services:
        print_color(f"\n检查 {name} (端口 {port})...", Colors.CYAN)

        if check_type == "TCP":
            # TCP 端口检查
            if check_tcp_port(port):
                print_color(f"✅ {name} 端口 {port} 开放", Colors.GREEN)
            else:
                print_color(f"❌ {name} 端口 {port} 未开放", Colors.RED)
                all_ready = False
        else:
            # HTTP 服务检查
            if wait_for_service(url, max_retries=15):
                print_color(f"✅ {name} 就绪", Colors.GREEN)
            else:
                print_color(f"⚠️  {name} 响应超时（可能仍在启动中）", Colors.YELLOW)
                all_ready = False

    return all_ready

def check_tcp_port(port):
    """检查 TCP 端口是否开放"""
    try:
        sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        sock.settimeout(2)
        result = sock.connect_ex(('localhost', port))
        sock.close()
        return result == 0
    except:
        return False

def show_info():
    """显示服务信息"""
    print_color("\n" + "="*60, Colors.GREEN)
    print_color("✅ 系统启动完成!", Colors.GREEN)
    print_color("="*60 + "\n", Colors.GREEN)

    print_color("📱 访问地址:", Colors.BLUE)
    print_color("   🌐 前端界面: http://localhost:3001", Colors.CYAN)
    print_color("   🔌 后端 API: http://localhost:8080/api", Colors.CYAN)
    print_color("   🤖 AI 服务: http://localhost:5051", Colors.CYAN)
    print_color("   📄 OnlyOffice: http://localhost:8081", Colors.CYAN)
    print_color("   🎥 直播 RTMP: rtmp://localhost:8000/live", Colors.CYAN)
    print_color("   📺 直播 HLS: http://localhost:8088", Colors.CYAN)

    print()
    print_color("👤 测试账号:", Colors.BLUE)
    print_color("   🔑 管理员: admin / password", Colors.PURPLE)
    print_color("   👨‍🏫 教师: teacher / password", Colors.PURPLE)
    print_color("   🎓 学生: student / password", Colors.PURPLE)

    print()
    print_color("📊 Docker 管理命令:", Colors.BLUE)
    print_color("   查看服务状态: docker-compose ps", Colors.CYAN)
    print_color("   查看日志: docker-compose logs -f [service]", Colors.CYAN)
    print_color("   停止服务: docker-compose down", Colors.CYAN)
    print_color("   重启服务: docker-compose restart", Colors.CYAN)

    print()
    print_color("🔍 故障排查:", Colors.BLUE)
    print_color("   查看所有日志: docker-compose logs", Colors.CYAN)
    print_color("   查看后端日志: docker-compose logs backend", Colors.CYAN)
    print_color("   查看 AI 日志: docker-compose logs ai-service", Colors.CYAN)

def main():
    """主函数"""
    print_banner()

    # 第一步：清除代理配置
    clear_proxy_settings()

    # 第二步：检查代理工具
    disable_proxy_tools()

    # 第三步：环境检查
    print_color("\n🔍 检查运行环境...", Colors.BLUE)
    issues, compose_cmd = check_environment()

    if issues:
        print_color("\n❌ 环境检查失败:", Colors.RED)
        for issue in issues:
            print_color(f"   • {issue}", Colors.RED)
        print_color("\n请解决上述问题后重新运行", Colors.YELLOW)
        input("\n按 Enter 键退出...")
        sys.exit(1)

    # 第四步：启动服务
    print_color("\n✅ 环境检查通过，开始启动服务...", Colors.GREEN)
    print_color("提示: 假设 Docker 镜像已预下载，启动过程较快", Colors.CYAN)

    success = docker_start(compose_cmd)

    if success:
        show_info()
        print_color("\n🎉 所有服务启动成功！", Colors.GREEN)
    else:
        print_color("\n⚠️  部分服务可能仍在启动中，请稍后检查", Colors.YELLOW)
        print_color("💡 可以使用 'docker-compose ps' 查看服务状态", Colors.CYAN)

    print_color("\n按 Enter 键退出...", Colors.CYAN)
    input()

if __name__ == "__main__":
    try:
        main()
    except KeyboardInterrupt:
        print_color("\n\n用户中断", Colors.YELLOW)
    except Exception as e:
        print_color(f"\n❌ 发生错误: {e}", Colors.RED)
        import traceback
        traceback.print_exc()
    finally:
        input("\n按 Enter 键退出...")