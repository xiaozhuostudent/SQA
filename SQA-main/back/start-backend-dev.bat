@echo off
chcp 65001 >nul
echo 停止现有的Java进程...
taskkill /F /IM java.exe 2>nul

echo 设置Java 21环境...
set JAVA_HOME=D:\develop\Java\jdk-21
set PATH=%JAVA_HOME%\bin;%PATH%

echo 当前Java版本:
java -version

echo 启动后端服务...
cd /d "%~dp0"
mvn spring-boot:run
