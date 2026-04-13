# Yali 教学管理系统 - 部署包

## 文件说明
- `yali-system.jar` - 应用程序包（包含前端和后端）
- `application-prod.properties` - 生产环境配置文件
- `start.sh` - 启动脚本
- `stop.sh` - 停止脚本
- `restart.sh` - 重启脚本

## 快速部署

### 1. 上传文件
将整个 deploy 目录上传到服务器 `/www/wwwroot/yali/`

### 2. 配置检查
编辑 `application-prod.properties`，确认数据库、Redis等配置正确

### 3. 创建必要目录
```bash
mkdir -p /www/wwwroot/yali-logs
mkdir -p /www/wwwroot/yali-storage
```

### 4. 启动应用
```bash
cd /www/wwwroot/yali
./start.sh
```

### 5. 查看日志
```bash
tail -f /www/wwwroot/yali-logs/application.log
tail -f /www/wwwroot/yali-logs/console.log
```

### 6. 停止应用
```bash
./stop.sh
```

### 7. 重启应用
```bash
./restart.sh
```

## 宝塔面板配置

### Java 项目管理器
- **项目路径**: /www/wwwroot/yali
- **项目名称**: yali-system
- **项目JDK**: JDK 21
- **项目启动命令**: 
  ```bash
  java -jar -Dspring.profiles.active=prod -Xms512m -Xmx1024m yali-system.jar
  ```
- **项目端口**: 8080

## 注意事项
1. 确保服务器已安装 JDK 21
2. 确保 MySQL 和 Redis 服务正常运行
3. 如需修改配置，编辑 `application-prod.properties` 后重启应用
4. 首次部署需配置 Nginx 反向代理（见下方配置）
