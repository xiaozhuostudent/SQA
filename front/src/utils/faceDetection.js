/**
 * 人脸检测工具模块
 * 使用 face-api.js 进行真实的深度学习人脸检测
 */
import * as faceapi from 'face-api.js'

let modelLoaded = false

/**
 * 加载人脸检测模型
 * @returns {Promise<boolean>} 是否加载成功
 */
export async function loadFaceDetectionModels() {
  if (modelLoaded) {
    return true
  }
  
  try {
    console.log('🔄 开始加载人脸识别模型...')
    const MODEL_URL = '/models'
    
    // 加载 Tiny Face Detector 模型（轻量级，适合实时检测）
    await faceapi.nets.tinyFaceDetector.loadFromUri(MODEL_URL)
    
    modelLoaded = true
    console.log('✓ 人脸识别模型加载成功')
    return true
  } catch (error) {
    console.error('❌ 人脸识别模型加载失败:', error)
    console.log('💡 提示：请确保 public/models 目录下有模型文件')
    modelLoaded = false
    return false
  }
}

/**
 * 检测视频中的人脸
 * @param {HTMLVideoElement} videoElement - 视频元素
 * @param {Object} options - 检测选项
 * @returns {Promise<Object>} 检测结果
 */
export async function detectFaces(videoElement, options = {}) {
  if (!modelLoaded) {
    throw new Error('模型未加载')
  }
  
  if (!videoElement || videoElement.videoWidth === 0 || videoElement.videoHeight === 0) {
    return {
      detected: false,
      count: 0,
      message: '视频尚未就绪'
    }
  }
  
  try {
    const detectionOptions = new faceapi.TinyFaceDetectorOptions({
      inputSize: options.inputSize || 224,           // 输入尺寸
      scoreThreshold: options.scoreThreshold || 0.5  // 置信度阈值
    })
    
    const detections = await faceapi.detectAllFaces(videoElement, detectionOptions)
    
    return {
      detected: detections.length > 0,
      count: detections.length,
      detections: detections.map(d => ({
        score: d.score,
        box: d.box
      })),
      message: detections.length > 0 
        ? `检测到 ${detections.length} 张人脸` 
        : '未检测到人脸'
    }
  } catch (error) {
    console.error('人脸检测失败:', error)
    return {
      detected: false,
      count: 0,
      error: error.message
    }
  }
}

/**
 * 基础亮度检测（备用方案）
 * 当深度学习模型不可用时使用
 */
export function detectByBrightness(videoElement, canvasElement) {
  if (!canvasElement) {
    return { detected: false, message: 'Canvas元素不可用' }
  }
  
  const context = canvasElement.getContext('2d')
  canvasElement.width = videoElement.videoWidth
  canvasElement.height = videoElement.videoHeight
  
  if (canvasElement.width === 0 || canvasElement.height === 0) {
    return { detected: false, message: '视频尺寸为0' }
  }
  
  context.drawImage(videoElement, 0, 0, canvasElement.width, canvasElement.height)
  const imageData = context.getImageData(0, 0, canvasElement.width, canvasElement.height)
  const data = imageData.data
  
  // 计算平均亮度和暗像素比例
  let totalBrightness = 0
  let darkPixels = 0
  
  for (let i = 0; i < data.length; i += 4) {
    const brightness = (data[i] + data[i + 1] + data[i + 2]) / 3
    totalBrightness += brightness
    if (brightness < 20) darkPixels++
  }
  
  const pixelCount = data.length / 4
  const avgBrightness = totalBrightness / pixelCount
  const darkRatio = darkPixels / pixelCount
  
  // 判断：亮度合理且不是黑屏
  const detected = avgBrightness > 60 && avgBrightness < 200 && darkRatio < 0.7
  
  return {
    detected,
    count: detected ? 1 : 0,
    brightness: avgBrightness.toFixed(1),
    darkRatio: (darkRatio * 100).toFixed(1) + '%',
    message: detected ? '基础检测：可能有人脸' : '基础检测：未检测到'
  }
}

export function isModelLoaded() {
  return modelLoaded
}
