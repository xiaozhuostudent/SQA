@echo off
echo ========================================
echo Face-api.js 模型文件下载脚本
echo ========================================
echo.

cd /d "%~dp0public\models"

echo 正在下载 Tiny Face Detector 模型...
echo.

echo [1/2] 下载 weights manifest...
powershell -Command "Invoke-WebRequest -Uri 'https://raw.githubusercontent.com/justadudewhohacks/face-api.js/master/weights/tiny_face_detector_model-weights_manifest.json' -OutFile 'tiny_face_detector_model-weights_manifest.json'"

echo [2/2] 下载 model shard...
powershell -Command "Invoke-WebRequest -Uri 'https://raw.githubusercontent.com/justadudewhohacks/face-api.js/master/weights/tiny_face_detector_model-shard1' -OutFile 'tiny_face_detector_model-shard1'"

echo.
echo ========================================
echo 下载完成！
echo ========================================
echo.
echo 已下载文件：
dir /b
echo.
pause
