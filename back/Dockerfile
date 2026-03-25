# ==========================================
# 后端服务 Dockerfile (Spring Boot)
# ==========================================
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

# 复制 pom.xml 和源代码
COPY pom.xml .
COPY src ./src
COPY mvnw .
COPY mvnw.cmd .
COPY .mvn ./.mvn

# 构建应用
RUN chmod +x mvnw && ./mvnw clean package -DskipTests

# ==========================================
# 运行阶段
# ==========================================
FROM eclipse-temurin:21-jre

WORKDIR /app

# 复制构建好的 jar 文件
COPY --from=build /app/target/*.war app.war

# 创建日志和存储目录
RUN mkdir -p /app/logs /app/storage

# 暴露端口
EXPOSE 8080

# 健康检查
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/course/all || exit 1

# 启动应用
ENTRYPOINT ["java", "-jar", "/app/app.war"]
