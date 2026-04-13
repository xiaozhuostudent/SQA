# 教学资源存储目录说明

本目录用于存放教学管理系统的所有学习资料文件。

## 📁 目录结构

```
storage/
## ✅ 必备示例资源清单

为了让演示环境中的资源预览/下载菜单立即可用，请按照下表把实际文件放进 `storage/resources` 目录，并保持与数据库 `resource.file_url` 完全一致的相对路径：

| 资源类型 | 用途 | 建议文件名 (可自定义内容) | 需要放置的位置 |
| --- | --- | --- | --- |
| 课件 PDF | `Java程序设计` 第 1 章节 | `Java程序设计_第01章_语言概述.pdf` | `storage/resources/documents/课件/Java程序设计_第01章_语言概述.pdf` |
| 课件 PDF | `Java程序设计` 第 2 章节 | `Java程序设计_第02章_面向对象.pdf` | `storage/resources/documents/课件/Java程序设计_第02章_面向对象.pdf` |
| 教材 PDF | Java 核心教材 | `Java核心技术卷I.pdf` | `storage/resources/documents/教材/Java核心技术卷I.pdf` |
| 课件 PDF | `数据结构与算法` 第 1 章 | `数据结构_第01章_线性表.pdf` | `storage/resources/documents/课件/数据结构_第01章_线性表.pdf` |
| 课件 PDF | `JavaEE企业级开发` 第 1 章 | `JavaEE_第01章_SpringBoot入门.pdf` | `storage/resources/documents/课件/JavaEE_第01章_SpringBoot入门.pdf` |
| 课件 PDF | `Web前端开发` 参考资料 | `Vue3开发指南.pdf` | `storage/resources/documents/教材/Vue3开发指南.pdf` |
| 课程录像 MP4 | `JavaEE企业级开发` 11/20 课程 | `JavaEE开发_20241120_第01章.mp4` | `storage/resources/videos/课程录像/JavaEE开发_20241120_第01章.mp4` |

> 若你修改了数据库中的 `file_url`，务必同步更新这里的文件名或路径；否则前端会提示“文件不存在”。

└── resources/                  # 教学资源根目录
    ├── documents/              # 文档类资源
    │   ├── 课件/               # PPT、PDF课件
    │   ├── 教材/               # 教材PDF、电子书
    │   ├── 作业模板/           # 作业模板Word/PDF
    │   └── 实验指导书/         # 实验指导文档
    ├── videos/                 # 视频类资源
    │   ├── 课程录像/           # MP4课程录像
    │   ├── 实验演示/           # 实验操作演示视频
    │   └── 其他视频/           # 其他教学视频
    ├── images/                 # 图片类资源
    │   ├── 图片素材/           # 教学用图片
    │   └── 课程图片/           # 课程封面等
    └── archives/               # 压缩包及其他
        ├── 压缩包/             # ZIP、RAR等压缩文件
        └── 其他文件/           # 其他类型文件
```

## 📝 文件命名规范

### 文档类 (documents/)
- **课件**: `课程名_章节_课件名.pdf` 或 `.pptx`
  - 例如: `Java程序设计_第03章_面向对象编程.pdf`
- **教材**: `教材名_作者.pdf`
  - 例如: `Java核心技术卷I_Cay_Horstmann.pdf`
- **作业模板**: `课程名_作业名_模板.docx`
  - 例如: `数据结构_第一次作业_模板.docx`
- **实验指导书**: `课程名_实验编号_实验名.pdf`
  - 例如: `JavaEE开发_实验01_Spring入门.pdf`

### 视频类 (videos/)
- **课程录像**: `课程名_日期_章节.mp4`
  - 例如: `JavaEE开发_20251124_第03章.mp4`
- **实验演示**: `实验名_演示.mp4`
  - 例如: `Spring_MVC配置_演示.mp4`

### 图片类 (images/)
- **图片素材**: `描述性名称.jpg/png`
  - 例如: `MVC架构图.png`
- **课程图片**: `课程代码_封面.jpg`
  - 例如: `CS101_封面.jpg`

### 压缩包类 (archives/)
- **压缩包**: `资源名称_版本.zip`
  - 例如: `JavaEE开发环境配置_v1.0.zip`

## 🔧 使用说明

### 1. 上传文件
将准备好的文件按照分类放入对应目录：
- Word文档 → `documents/课件/` 或 `documents/作业模板/`
- PDF文件 → `documents/课件/` 或 `documents/教材/` 或 `documents/实验指导书/`
- MP4视频 → `videos/课程录像/` 或 `videos/实验演示/`
- 图片 → `images/图片素材/` 或 `images/课程图片/`
- 压缩包 → `archives/压缩包/`

### 2. 数据库关联
上传文件后，需要在数据库`resource`表中添加记录，字段说明：
- `title`: 资源标题
- `description`: 资源描述
- `file_url`: 文件相对路径（从`storage/`开始）
  - 例如: `resources/documents/课件/Java程序设计_第03章_面向对象编程.pdf`
- `file_type`: 文件类型（pdf, docx, mp4, jpg等）
- `file_size`: 文件大小（字节）
- `course_id`: 关联课程ID
- `uploader_id`: 上传者用户ID

### 3. 文件访问
- 前端通过API获取资源列表
- 后端返回文件的访问URL
- 用户点击下载或在线预览

## 🚀 快速开始

### 示例：上传一个课件
1. 将文件放入：`storage/resources/documents/课件/Java程序设计_第03章_面向对象编程.pdf`
2. 在数据库插入记录：
```sql
INSERT INTO resource (course_id, course_name, title, description, file_url, file_type, file_size, uploader_id, uploader_name)
VALUES (1, 'Java程序设计', '第03章 面向对象编程', '介绍Java面向对象的核心概念', 
        'resources/documents/课件/Java程序设计_第03章_面向对象编程.pdf', 
        'pdf', 2048576, 2, '张老师');
```

## ⚠️ 注意事项

1. **文件大小限制**
   - 单个文档文件建议不超过 50MB
   - 单个视频文件建议不超过 500MB
   - 如需上传大文件，建议使用阿里云OSS等云存储

2. **文件安全**
   - 不要上传含有病毒或恶意代码的文件
   - 注意版权问题，只上传有授权的资源

3. **定期备份**
   - 建议定期备份`storage/`目录
   - 可使用rsync或云同步工具

4. **权限控制**
   - 确保Web服务器对该目录有读取权限
   - 建议配置Nginx/Apache进行静态文件访问控制

## 📊 容量监控

查看当前存储使用情况：
```bash
# 查看总大小
du -sh storage/

# 查看各分类大小
du -sh storage/resources/*
```

## 🔄 迁移到云存储（可选）

如果本地存储空间不足，可以迁移到阿里云OSS：
1. 创建OSS Bucket
2. 配置后端`application.properties`中的OSS参数
3. 使用ossutil工具批量上传文件
4. 更新数据库中的`file_url`为OSS地址
