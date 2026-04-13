import request from '@/utils/request'

/**
 * 方案1: Piston API (开源免费,支持50+语言)
 * 官网: https://github.com/engineer-man/piston
 * 公共实例: https://emkc.org/api/v2/piston
 */
export const executePiston = async (code, language) => {
  try {
    const response = await fetch('https://emkc.org/api/v2/piston/execute', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        language: language, // python, java, cpp, javascript 等
        version: '*', // 使用最新版本
        files: [{ content: code }]
      })
    })
    return await response.json()
  } catch (error) {
    console.error('Piston执行失败:', error)
    throw error
  }
}

/**
 * 方案2: JDoodle API (商业化,免费200次/天)
 * 官网: https://www.jdoodle.com/compiler-api
 * 需要注册获取clientId和clientSecret
 */
export const executeJDoodle = async (code, language, clientId, clientSecret) => {
  const languageMap = {
    python: 'python3',
    java: 'java',
    cpp: 'cpp17',
    javascript: 'nodejs'
  }
  
  try {
    const response = await fetch('https://api.jdoodle.com/v1/execute', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        script: code,
        language: languageMap[language] || language,
        versionIndex: '0',
        clientId: clientId,
        clientSecret: clientSecret
      })
    })
    return await response.json()
  } catch (error) {
    console.error('JDoodle执行失败:', error)
    throw error
  }
}

/**
 * 方案3: 后端代理执行 (安全性更高)
 */
export const executeViaBackend = (code, language) => {
  return request({
    url: '/experiment/execute',
    method: 'post',
    data: { code, language }
  })
}

/**
 * 方案4: 浏览器内执行 (仅限JavaScript/Python-Pyodide)
 */
export const executeBrowserJS = (code) => {
  try {
    const originalLog = console.log
    const logs = []
    console.log = (...args) => logs.push(args.join(' '))
    
    // eslint-disable-next-line no-eval
    eval(code)
    
    console.log = originalLog
    return {
      success: true,
      output: logs.join('\n'),
      error: null
    }
  } catch (error) {
    return {
      success: false,
      output: null,
      error: error.message
    }
  }
}

/**
 * 统一执行接口 (智能降级)
 */
export const executeCode = async (code, language) => {
  // 优先使用Piston (完全免费,无需注册)
  try {
    console.log('[Piston] 开始执行代码:', { language, codeLength: code.length })
    const result = await executePiston(code, language)
    console.log('[Piston] 执行结果:', result)
    
    if (result.compile?.code) {
      return {
        success: false,
        output: '',
        error: `编译错误:\n${result.compile.stderr || result.compile.output}`
      }
    }
    
    if (result.run?.code && result.run.code !== 0) {
      return {
        success: false,
        output: result.run.stdout || '',
        error: `运行错误 (退出码: ${result.run.code}):\n${result.run.stderr || ''}`
      }
    }
    
    return {
      success: true,
      output: result.run?.stdout || result.run?.output || '(无输出)',
      error: null
    }
  } catch (error) {
    console.error('[Piston] 执行失败:', error)
    
    // JavaScript可在浏览器内执行
    if (language === 'javascript') {
      console.log('[浏览器] 降级到浏览器执行JS')
      return executeBrowserJS(code)
    }
    
    // 其他语言返回错误信息
    return {
      success: false,
      output: '',
      error: `Piston API请求失败: ${error.message}\n\n可能原因:\n1. 网络连接问题\n2. Piston服务暂时不可用\n3. 代理设置导致请求失败\n\n建议: 检查网络连接或稍后重试`
    }
  }
}

// 获取Piston支持的语言列表
export const getSupportedLanguages = async () => {
  try {
    const response = await fetch('https://emkc.org/api/v2/piston/runtimes')
    const runtimes = await response.json()
    
    // 去重：同一个语言可能有多个版本，只保留一个
    const languageMap = new Map()
    runtimes.forEach(r => {
      if (!languageMap.has(r.language)) {
        languageMap.set(r.language, {
          id: r.language,
          name: `${r.language} (${r.version})`,
          version: r.version
        })
      }
    })
    
    // 定义常用语言优先级（C++, Python, Java, C, Go）
    const priorityLanguages = ['cpp', 'python', 'java', 'c', 'go']
    const languages = Array.from(languageMap.values())
    
    // 分离常用语言和其他语言
    const priorityLangs = []
    const otherLangs = []
    
    languages.forEach(lang => {
      const index = priorityLanguages.indexOf(lang.id)
      if (index !== -1) {
        // 按优先级顺序插入
        priorityLangs[index] = lang
      } else {
        otherLangs.push(lang)
      }
    })
    
    // 过滤掉 undefined（某些优先语言可能不存在），然后拼接
    return [...priorityLangs.filter(Boolean), ...otherLangs]
  } catch (error) {
    console.error('获取语言列表失败:', error)
    return [
      { id: 'python', name: 'Python 3', version: '3.10' },
      { id: 'java', name: 'Java', version: '17' },
      { id: 'cpp', name: 'C++', version: 'GCC 11' },
      { id: 'javascript', name: 'JavaScript (Node.js)', version: '18' }
    ]
  }
}

// --- Backend API Integration ---

// 提交代码到后端进行判题
export const submitCode = (data) => {
  return request({
    url: '/code/submit',
    method: 'post',
    data
  })
}

// 获取提交历史
export const getSubmissionHistory = (problemId) => {
  return request({
    url: `/code/history/${problemId}`,
    method: 'get'
  })
}
