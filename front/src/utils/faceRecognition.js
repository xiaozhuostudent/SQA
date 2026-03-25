/**
 * 人脸识别工具模块（升级版）
 * 支持人脸检测 + 特征提取 + 人脸识别
 * 使用 face-api.js 的完整功能
 */
import * as faceapi from 'face-api.js'

let modelLoaded = false
let recognitionModelLoaded = false

/**
 * 加载所有需要的模型
 * @returns {Promise<boolean>} 是否加载成功
 */
export async function loadFaceRecognitionModels() {
  if (modelLoaded && recognitionModelLoaded) {
    return true
  }
  
  try {
    console.log('🔄 开始加载人脸识别模型...')
    const MODEL_URL = '/models'
    
    // 1. 加载 Tiny Face Detector（检测人脸位置）
    await faceapi.nets.tinyFaceDetector.loadFromUri(MODEL_URL)
    modelLoaded = true
    console.log('✓ 人脸检测模型加载成功')
    
    // 2. 加载 Face Landmark（检测面部特征点，68个关键点）
    await faceapi.nets.faceLandmark68Net.loadFromUri(MODEL_URL)
    console.log('✓ 面部特征点模型加载成功')
    
    // 3. 加载 Face Recognition（提取128维人脸特征向量）
    await faceapi.nets.faceRecognitionNet.loadFromUri(MODEL_URL)
    recognitionModelLoaded = true
    console.log('✓ 人脸识别模型加载成功')
    
    console.log('✅ 所有模型加载完成，系统已就绪')
    return true
  } catch (error) {
    console.error('❌ 模型加载失败:', error)
    console.log('💡 提示：请确保 public/models 目录下有以下模型文件：')
    console.log('  - tiny_face_detector_model-weights_manifest.json')
    console.log('  - face_landmark_68_model-weights_manifest.json')
    console.log('  - face_recognition_model-weights_manifest.json')
    return false
  }
}

/**
 * 从视频或图片中提取人脸特征向量
 * @param {HTMLVideoElement|HTMLImageElement} source - 视频或图片元素
 * @returns {Promise<Object>} 包含特征向量和检测信息
 */
export async function extractFaceDescriptor(source) {
  if (!modelLoaded || !recognitionModelLoaded) {
    throw new Error('模型未加载，请先调用 loadFaceRecognitionModels()')
  }
  
  if (!source || source.videoWidth === 0 || source.videoHeight === 0) {
    return {
      success: false,
      message: '视频/图片尚未就绪'
    }
  }
  
  try {
    // 检测人脸 + 提取特征点 + 生成特征向量（一步完成）
    const detection = await faceapi
      .detectSingleFace(source, new faceapi.TinyFaceDetectorOptions())
      .withFaceLandmarks()
      .withFaceDescriptor()
    
    if (!detection) {
      return {
        success: false,
        message: '未检测到人脸'
      }
    }
    
    return {
      success: true,
      descriptor: Array.from(detection.descriptor), // 128维特征向量
      landmarks: detection.landmarks.positions,      // 68个面部特征点
      box: detection.detection.box,                  // 人脸框位置
      score: detection.detection.score,              // 检测置信度
      message: '人脸特征提取成功'
    }
  } catch (error) {
    console.error('特征提取失败:', error)
    return {
      success: false,
      error: error.message
    }
  }
}

/**
 * 比对两个人脸特征向量，判断是否为同一人
 * @param {Float32Array|Array} descriptor1 - 第一个特征向量（128维）
 * @param {Float32Array|Array} descriptor2 - 第二个特征向量（128维）
 * @param {number} threshold - 相似度阈值（默认0.6，越小越严格）
 * @returns {Object} 比对结果
 */
export function compareFaceDescriptors(descriptor1, descriptor2, threshold = 0.6) {
  if (!descriptor1 || !descriptor2) {
    return {
      match: false,
      message: '特征向量不能为空'
    }
  }
  
  // 转换为 Float32Array
  const desc1 = descriptor1 instanceof Float32Array ? descriptor1 : new Float32Array(descriptor1)
  const desc2 = descriptor2 instanceof Float32Array ? descriptor2 : new Float32Array(descriptor2)
  
  // 计算欧氏距离
  const distance = faceapi.euclideanDistance(desc1, desc2)
  
  // 判断是否匹配
  const match = distance < threshold
  
  // 计算相似度百分比（距离越小越相似）
  const similarity = Math.max(0, (1 - distance) * 100)
  
  return {
    match,                              // 是否匹配
    distance,                           // 欧氏距离（0-1之间，越小越相似）
    similarity: similarity.toFixed(2),  // 相似度百分比
    threshold,                          // 使用的阈值
    message: match 
      ? `匹配成功！相似度：${similarity.toFixed(2)}%` 
      : `不匹配。相似度：${similarity.toFixed(2)}%`
  }
}

/**
 * 从图片文件中提取人脸特征（用于注册人脸）
 * @param {File} imageFile - 图片文件
 * @returns {Promise<Object>} 特征提取结果
 */
export async function extractFaceFromImage(imageFile) {
  return new Promise((resolve, reject) => {
    const reader = new FileReader()
    
    reader.onload = async (e) => {
      const img = new Image()
      img.onload = async () => {
        try {
          const result = await extractFaceDescriptor(img)
          resolve(result)
        } catch (error) {
          reject(error)
        }
      }
      img.onerror = () => reject(new Error('图片加载失败'))
      img.src = e.target.result
    }
    
    reader.onerror = () => reject(new Error('文件读取失败'))
    reader.readAsDataURL(imageFile)
  })
}

/**
 * 批量检测视频中的所有人脸并提取特征
 * @param {HTMLVideoElement} videoElement - 视频元素
 * @returns {Promise<Array>} 所有检测到的人脸特征
 */
export async function detectAllFacesWithDescriptors(videoElement) {
  if (!modelLoaded || !recognitionModelLoaded) {
    throw new Error('模型未加载')
  }
  
  try {
    const detections = await faceapi
      .detectAllFaces(videoElement, new faceapi.TinyFaceDetectorOptions())
      .withFaceLandmarks()
      .withFaceDescriptors()
    
    return {
      success: true,
      count: detections.length,
      faces: detections.map(d => ({
        descriptor: Array.from(d.descriptor),
        box: d.detection.box,
        score: d.detection.score,
        landmarks: d.landmarks.positions
      })),
      message: `检测到 ${detections.length} 张人脸`
    }
  } catch (error) {
    console.error('批量检测失败:', error)
    return {
      success: false,
      count: 0,
      error: error.message
    }
  }
}

/**
 * 实时人脸识别监控
 * @param {HTMLVideoElement} videoElement - 视频元素
 * @param {Array} knownFaces - 已知人脸数据库 [{name, descriptor}, ...]
 * @param {Function} onDetection - 检测回调函数
 * @param {number} interval - 检测间隔（毫秒）
 * @returns {Function} 停止监控的函数
 */
export function startFaceRecognitionMonitor(videoElement, knownFaces, onDetection, interval = 2000) {
  let isMonitoring = true
  
  const monitor = async () => {
    if (!isMonitoring) return
    
    try {
      const result = await detectAllFacesWithDescriptors(videoElement)
      
      if (result.success && result.count > 0) {
        // 对每个检测到的人脸进行识别
        const recognitions = result.faces.map(face => {
          // 在已知人脸库中查找最匹配的
          let bestMatch = null
          let bestDistance = Infinity
          
          for (const known of knownFaces) {
            const comparison = compareFaceDescriptors(face.descriptor, known.descriptor)
            if (comparison.distance < bestDistance) {
              bestDistance = comparison.distance
              bestMatch = {
                ...known,
                ...comparison
              }
            }
          }
          
          return {
            ...face,
            recognition: bestMatch
          }
        })
        
        onDetection({
          success: true,
          count: recognitions.length,
          faces: recognitions,
          timestamp: Date.now()
        })
      } else {
        onDetection({
          success: false,
          count: 0,
          message: '未检测到人脸',
          timestamp: Date.now()
        })
      }
    } catch (error) {
      console.error('监控出错:', error)
    }
    
    // 继续下一次检测
    if (isMonitoring) {
      setTimeout(monitor, interval)
    }
  }
  
  // 开始监控
  monitor()
  
  // 返回停止函数
  return () => {
    isMonitoring = false
  }
}

/**
 * 可视化绘制人脸检测框和特征点
 * @param {HTMLCanvasElement} canvas - 画布元素
 * @param {Array} detections - 检测结果
 * @param {Object} options - 绘制选项
 */
export function drawFaceDetections(canvas, detections, options = {}) {
  const ctx = canvas.getContext('2d')
  
  detections.forEach(detection => {
    const { box, landmarks, score } = detection
    
    // 绘制人脸框
    ctx.strokeStyle = options.boxColor || '#00ff00'
    ctx.lineWidth = options.lineWidth || 2
    ctx.strokeRect(box.x, box.y, box.width, box.height)
    
    // 绘制置信度
    ctx.fillStyle = options.textColor || '#00ff00'
    ctx.font = options.font || '16px Arial'
    ctx.fillText(`${(score * 100).toFixed(1)}%`, box.x, box.y - 5)
    
    // 绘制特征点（可选）
    if (options.drawLandmarks && landmarks) {
      ctx.fillStyle = options.landmarkColor || '#ff0000'
      landmarks.forEach(point => {
        ctx.beginPath()
        ctx.arc(point.x, point.y, 2, 0, 2 * Math.PI)
        ctx.fill()
      })
    }
  })
}

export default {
  loadFaceRecognitionModels,
  extractFaceDescriptor,
  compareFaceDescriptors,
  extractFaceFromImage,
  detectAllFacesWithDescriptors,
  startFaceRecognitionMonitor,
  drawFaceDetections
}
