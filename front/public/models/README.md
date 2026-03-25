# Face-api.js 模型文件

此目录用于存放 face-api.js 所需的模型文件。

## 下载模型文件

从以下地址下载所需的模型文件到此目录：
https://github.com/justadudewhohacks/face-api.js/tree/master/weights

### 必需的文件（轻量级方案）：

```bash
# 方案1：轻量级模型（推荐用于考试监控）
tiny_face_detector_model-weights_manifest.json
tiny_face_detector_model-shard1

# 方案2：高精度模型（可选）
ssd_mobilenetv1_model-weights_manifest.json
ssd_mobilenetv1_model-shard1
ssd_mobilenetv1_model-shard2
```

### 快速下载脚本：

```bash
cd public/models

# 下载 Tiny Face Detector（轻量级，适合实时检测）
curl -O https://raw.githubusercontent.com/justadudewhohacks/face-api.js/master/weights/tiny_face_detector_model-weights_manifest.json
curl -O https://raw.githubusercontent.com/justadudewhohacks/face-api.js/master/weights/tiny_face_detector_model-shard1

# 或使用 wget
wget https://raw.githubusercontent.com/justadudewhohacks/face-api.js/master/weights/tiny_face_detector_model-weights_manifest.json
wget https://raw.githubusercontent.com/justadudewhohacks/face-api.js/master/weights/tiny_face_detector_model-shard1
```

### Windows PowerShell 下载：

```powershell
cd public\models

# 下载轻量级模型
Invoke-WebRequest -Uri "https://raw.githubusercontent.com/justadudewhohacks/face-api.js/master/weights/tiny_face_detector_model-weights_manifest.json" -OutFile "tiny_face_detector_model-weights_manifest.json"
Invoke-WebRequest -Uri "https://raw.githubusercontent.com/justadudewhohacks/face-api.js/master/weights/tiny_face_detector_model-shard1" -OutFile "tiny_face_detector_model-shard1"
```

## 模型说明

- **tiny_face_detector**: 轻量级模型，速度快，适合实时检测
- **ssd_mobilenetv1**: 精度更高，但速度较慢
- **mtcnn**: 精度最高，但性能开销大

**推荐**：考试监控场景使用 `tiny_face_detector`，平衡了准确度和性能。
