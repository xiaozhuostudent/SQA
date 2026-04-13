#!/bin/bash

# Yali 教学管理系统 - 启动脚本

APP_NAME="yali-system"
JAR_FILE="yali-system.jar"
LOG_DIR="/www/wwwroot/yali-logs"
PID_FILE="$APP_NAME.pid"
JAVA_HOME="/www/server/java/jdk-21.0.2"

# 创建日志目录
mkdir -p $LOG_DIR

# 检查是否已运行
if [ -f "$PID_FILE" ]; then
    PID=$(cat "$PID_FILE")
    if ps -p $PID > /dev/null 2>&1; then
        echo "应用已在运行 (PID: $PID)"
        exit 1
    fi
fi

# 启动应用
echo "正在启动 $APP_NAME..."
nohup $JAVA_HOME/bin/java -jar \
    -Dspring.profiles.active=prod \
    -Xms512m \
    -Xmx1024m \
    -XX:+UseG1GC \
    -XX:MaxGCPauseMillis=200 \
    $JAR_FILE > $LOG_DIR/console.log 2>&1 &

echo $! > "$PID_FILE"
echo "应用已启动 (PID: $(cat $PID_FILE))"
echo "日志文件: $LOG_DIR/application.log"
echo "控制台输出: $LOG_DIR/console.log"
