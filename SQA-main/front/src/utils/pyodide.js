/**
 * Pyodide - 浏览器内运行Python的WebAssembly实现
 * 优点: 完全本地执行,无网络请求,支持NumPy/Pandas等科学计算库
 * 缺点: 首次加载较慢(~6MB),仅支持Python
 */

let pyodide = null

export const initPyodide = async () => {
  if (pyodide) return pyodide
  
  try {
    // 从CDN加载Pyodide
    const script = document.createElement('script')
    script.src = 'https://cdn.jsdelivr.net/pyodide/v0.24.1/full/pyodide.js'
    document.head.appendChild(script)
    
    await new Promise((resolve, reject) => {
      script.onload = resolve
      script.onerror = reject
    })
    
    pyodide = await window.loadPyodide({
      indexURL: 'https://cdn.jsdelivr.net/pyodide/v0.24.1/full/'
    })
    
    console.log('Pyodide初始化成功')
    return pyodide
  } catch (error) {
    console.error('Pyodide加载失败:', error)
    throw error
  }
}

export const executePython = async (code) => {
  try {
    if (!pyodide) {
      await initPyodide()
    }
    
    // 重定向stdout
    pyodide.runPython(`
import sys
from io import StringIO
sys.stdout = StringIO()
`)
    
    // 执行用户代码
    const startTime = performance.now()
    await pyodide.runPythonAsync(code)
    const endTime = performance.now()
    
    // 获取输出
    const output = pyodide.runPython('sys.stdout.getvalue()')
    
    return {
      success: true,
      output: output,
      error: null,
      time: ((endTime - startTime) / 1000).toFixed(3) + 's',
      memory: 0
    }
  } catch (error) {
    return {
      success: false,
      output: null,
      error: error.message,
      time: 0,
      memory: 0
    }
  }
}

// 安装Python包 (如numpy, pandas)
export const installPackage = async (packageName) => {
  if (!pyodide) await initPyodide()
  await pyodide.loadPackage(packageName)
}
