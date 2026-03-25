# ==========================================
# 前端服务 Dockerfile (Vue3 + Vite)
# ==========================================
FROM node:18-alpine AS build

WORKDIR /app

# 复制 package.json 和 package-lock.json
COPY package*.json ./

# 安装依赖
RUN npm install

# 复制源代码
COPY . .

# 构建生产版本（可选，如果要生产部署）
# RUN npm run build

# ==========================================
# 开发模式运行（直接运行 vite dev server）
# ==========================================
FROM node:18-alpine

WORKDIR /app

# 复制构建阶段的 node_modules
COPY --from=build /app/node_modules ./node_modules
COPY . .

# 暴露端口
EXPOSE 3001

# 健康检查
HEALTHCHECK --interval=30s --timeout=10s --start-period=30s --retries=3 \
  CMD wget --quiet --tries=1 --spider http://localhost:3001 || exit 1

# 启动开发服务器
CMD ["npm", "run", "dev", "--", "--host", "0.0.0.0", "--port", "3001"]
