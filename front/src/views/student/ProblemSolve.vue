<template>
  <div class="problem-solve-page">
    <el-row :gutter="20" class="content-row">
      <!-- Left: Problem Description -->
      <el-col :span="10" class="left-col">
        <el-card class="problem-card">
          <template #header>
            <div class="header-actions">
              <span>{{ problem.title }}</span>
              <el-button size="small" @click="$router.back()">返回列表</el-button>
            </div>
          </template>
          <div class="problem-content" v-loading="loading">
            <div class="meta-info">
              <el-tag size="small" :type="getDifficultyType(problem.difficulty)">{{ problem.difficulty }}</el-tag>
              <el-tag size="small" type="info">分数: {{ problem.score }}</el-tag>
              <el-tag size="small" type="warning">时间限制: {{ problem.timeLimit }}ms</el-tag>
              <el-tag size="small" type="warning">内存限制: {{ problem.memoryLimit }}MB</el-tag>
            </div>
            
            <h3>题目描述</h3>
            <div class="desc">{{ problem.description }}</div>
            
            <!-- Samples would go here if I fetched them. For now, assume description contains samples or I fetch them separately -->
          </div>
        </el-card>
        
        <el-card class="history-card">
          <template #header>
            <span>提交历史</span>
            <el-button size="small" link @click="loadHistory">刷新</el-button>
          </template>
          <div class="history-table-wrapper">
            <el-table :data="history" size="small" style="width: 100%">
              <el-table-column prop="result" label="状态" width="100">
                <template #default="scope">
                  <el-tag 
                    :type="getStatusType(scope.row.result)"
                    :effect="scope.row.result === 'AC' ? 'dark' : 'plain'"
                    size="large"
                    round
                    class="status-tag"
                  >
                    <component :is="getStatusIcon(scope.row.result)" style="width: 16px; height: 16px; margin-right: 4px;" />
                    {{ scope.row.result }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="language" label="语言" width="100">
                <template #default="scope">
                  <el-tag size="small" type="info">{{ scope.row.language }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="description" label="详情" min-width="150" show-overflow-tooltip>
                <template #default="scope">
                  <span style="font-size: 12px; color: var(--text-secondary);">{{ scope.row.description }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="runTimeMs" label="耗时" width="80">
                <template #default="scope">{{ scope.row.runTimeMs }}ms</template>
              </el-table-column>
              <el-table-column prop="submitTime" label="提交时间" width="160" />
            </el-table>
          </div>
        </el-card>
      </el-col>
      
      <!-- Right: Code Editor -->
      <el-col :span="14" class="right-col">
        <el-card class="editor-card">
          <template #header>
            <div class="editor-header">
              <el-select 
                v-model="language" 
                placeholder="选择语言" 
                size="small" 
                style="width: 220px;" 
                filterable
                popper-class="language-select-dropdown"
              >
                <el-option 
                  v-for="lang in supportedLanguages" 
                  :key="lang.id" 
                  :label="lang.name" 
                  :value="lang.id" 
                />
              </el-select>
              <el-button type="primary" size="small" :loading="submitting" @click="submit">提交代码</el-button>
            </div>
          </template>
          
          <CodeEditor 
            v-model="code" 
            :language="language" 
            height="550px"
            placeholder="在此输入代码..."
          />
          
          <div v-if="lastResult" class="result-panel" :class="getResultClass(lastResult.result)">
            <div class="result-header">
              <component :is="getStatusIcon(lastResult.result)" class="result-icon" />
              <h4 class="result-title">运行结果: {{ lastResult.result }}</h4>
              <span class="result-status-text" v-if="lastResult.result === 'AC'">✨ 恭喜通过！</span>
              <span class="result-status-text" v-else-if="lastResult.result === 'RE'">❌ 运行时错误</span>
              <span class="result-status-text" v-else-if="lastResult.result === 'WA'">⚠️ 答案错误</span>
              <span class="result-status-text" v-else-if="lastResult.result === 'CE'">⚠️ 编译错误</span>
              <span class="result-status-text" v-else-if="lastResult.result === 'TLE'">⏱️ 超时</span>
              <span class="result-status-text" v-else-if="lastResult.result === 'MLE'">💾 内存超限</span>
            </div>
            
            <!-- 测试用例通过率 -->
            <div v-if="lastResult.passRateText" class="result-info">
              <el-tag :type="lastResult.result === 'AC' ? 'success' : 'info'" size="large">
                {{ lastResult.passRateText }}
              </el-tag>
              <span v-if="lastResult.runTimeMs" style="margin-left: 10px; color: var(--text-secondary);">
                耗时: {{ lastResult.runTimeMs }}ms
              </span>
            </div>
            
            <!-- 结果说明 -->
            <div v-if="lastResult.resultDescription" class="result-description">
              {{ lastResult.resultDescription }}
            </div>
            
            <!-- 错误信息 -->
            <div v-if="lastResult.errorMessage" class="result-error">
              <h5>错误详情：</h5>
              <pre class="error-pre">{{ lastResult.errorMessage }}</pre>
            </div>
            
            <!-- 运行输出 -->
            <div v-if="lastResult.output && lastResult.result !== 'RE'" class="result-output">
              <h5>运行输出：</h5>
              <pre class="output-pre">{{ lastResult.output }}</pre>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getProblemDetail, getExperimentDetail } from '@/api/experiment'
import { submitCode, getSubmissionHistory, getSupportedLanguages } from '@/api/codeExecution'
import { ElMessage } from 'element-plus'
import { 
  CircleCheck, 
  CircleClose, 
  Warning, 
  Clock, 
  WarningFilled, 
  DocumentRemove, 
  Loading, 
  QuestionFilled 
} from '@element-plus/icons-vue'
import CodeEditor from '@/components/CodeEditor.vue'

const route = useRoute()
const experimentId = route.params.experimentId
const problemId = route.params.problemId

const problem = ref({})
const experiment = ref({})
const loading = ref(false)
const history = ref([])
const language = ref('python')
const code = ref('')
const submitting = ref(false)
const lastResult = ref(null)
const allLanguages = [
  { id: 'python', name: 'Python 3', version: '3.10' },
  { id: 'java', name: 'Java', version: '17' },
  { id: 'cpp', name: 'C++', version: 'GCC 11' },
  { id: 'c', name: 'C', version: 'GCC 11' },
  { id: 'javascript', name: 'JavaScript (Node.js)', version: '18' }
]
const supportedLanguages = ref([...allLanguages])

const loadProblem = async () => {
  loading.value = true
  try {
    // 加载题目详情
    const problemRes = await getProblemDetail(problemId)
    if (problemRes.code === 200) {
      problem.value = problemRes.data
    }
    
    // 加载实验信息以获取语言限制
    const experimentRes = await getExperimentDetail(experimentId)
    if (experimentRes.code === 200) {
      experiment.value = experimentRes.data
      
      // 根据实验的allowedLanguages过滤语言选项
      if (experiment.value.allowedLanguages) {
        const allowedLangs = experiment.value.allowedLanguages.split(',').filter(l => l.trim())
        if (allowedLangs.length > 0) {
          supportedLanguages.value = allLanguages.filter(lang => allowedLangs.includes(lang.id))
          // 如果当前选中的语言不在允许列表中，切换到第一个允许的语言
          if (!allowedLangs.includes(language.value) && supportedLanguages.value.length > 0) {
            language.value = supportedLanguages.value[0].id
          }
        }
      }
    }
  } catch (error) {
    ElMessage.error('加载题目失败')
  } finally {
    loading.value = false
  }
}

const loadSupportedLanguages = async () => {
  try {
    const languages = await getSupportedLanguages()
    if (languages && languages.length > 0) {
      supportedLanguages.value = languages
      // 如果当前选择的语言不在列表中，则默认选择第一个
      if (!languages.find(l => l.id === language.value)) {
        language.value = languages[0].id
      }
    }
  } catch (error) {
    console.error('加载语言列表失败:', error)
    // 使用默认语言列表
  }
}

const loadHistory = async () => {
  try {
    const res = await getSubmissionHistory(problemId)
    if (res.code === 200) {
      history.value = res.data
    }
  } catch (error) {
    console.error(error)
  }
}

const submit = async () => {
  if (!code.value.trim()) {
    ElMessage.warning('请输入代码')
    return
  }
  
  submitting.value = true
  lastResult.value = null
  
  try {
    const res = await submitCode({
      experimentId: experimentId,
      problemId: problemId,
      language: language.value,
      code: code.value
    })
    
    if (res.code === 200) {
      lastResult.value = res.data
      if (res.data.result === 'AC') {
        ElMessage.success('恭喜！解答正确')
      } else {
        ElMessage.warning(`解答错误: ${res.data.result}`)
      }
      loadHistory()
    } else {
      ElMessage.error(res.message || '提交失败')
    }
  } catch (error) {
    ElMessage.error('提交异常')
  } finally {
    submitting.value = false
  }
}

const getDifficultyType = (diff) => {
  const map = { 'easy': 'success', 'medium': 'warning', 'hard': 'danger' }
  return map[diff] || 'info'
}

const getResultClass = (result) => {
  const map = {
    'AC': 'result-success',
    'WA': 'result-warning',
    'RE': 'result-error',
    'CE': 'result-warning',
    'TLE': 'result-info',
    'MLE': 'result-info'
  }
  return map[result] || 'result-default'
}

const getStatusType = (status) => {
  const map = { 
    'AC': 'success',     // 通过
    'WA': 'warning',     // 答案错误
    'RE': 'danger',      // 运行时错误
    'TLE': 'info',       // 超时
    'MLE': 'info',       // 内存超限
    'CE': 'warning',     // 编译错误
    'PENDING': 'info'    // 等待中
  }
  return map[status] || 'info'
}

const getStatusIcon = (status) => {
  const iconMap = {
    'AC': CircleCheck,      // 通过 - 打勾
    'WA': CircleClose,      // 答案错误 - 叉号
    'RE': Warning,          // 运行时错误 - 警告
    'TLE': Clock,           // 超时 - 时钟
    'MLE': WarningFilled,   // 内存超限 - 警告填充
    'CE': DocumentRemove,   // 编译错误 - 文档错误
    'PENDING': Loading      // 等待中 - 加载中
  }
  return iconMap[status] || QuestionFilled
}

onMounted(() => {
  loadSupportedLanguages()
  loadProblem()
  loadHistory()
})
</script>

<style scoped>
.problem-solve-page {
  padding: 20px;
  background-color: var(--bg-primary);
  height: calc(100vh - 60px);
  overflow: hidden;
}

.content-row {
  height: 100%;
}

.left-col,
.right-col {
  height: 100%;
  display: flex;
  flex-direction: column;
}
.problem-card {
  flex: 0 0 auto;
  margin-bottom: 20px;
  max-height: 40%;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.problem-card :deep(.el-card__body) {
  overflow-y: auto;
  flex: 1;
}

.header-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.meta-info {
  margin-bottom: 15px;
  display: flex;
  gap: 10px;
}
.desc {
  white-space: pre-wrap;
  line-height: 1.6;
  background: var(--bg-secondary);
  color: var(--text-primary);
  padding: 15px;
  border-radius: 4px;
}
.editor-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.result-panel {
  margin-top: 15px;
  padding: 10px;
  border-radius: 4px;
  background: var(--bg-secondary);
  border: 1px solid var(--border);
  color: var(--text-primary);
}
.result-panel.error {
  border-color: var(--el-color-danger);
}

:deep(.el-card) {
  background-color: var(--bg-float);
  border-color: var(--border);
  color: var(--text-primary);
}

:deep(.el-card__header) {
  background-color: var(--bg-secondary);
  border-bottom-color: var(--border);
}

:deep(.el-textarea__inner) {
  background-color: var(--bg-secondary);
  color: var(--text-primary);
  border-color: var(--border);
}

.history-card {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.history-card :deep(.el-card__body) {
  flex: 1;
  overflow: hidden;
  padding: 0;
}

.history-table-wrapper {
  height: 100%;
  overflow-y: auto;
  padding: 20px;
}

.history-table-wrapper::-webkit-scrollbar {
  width: 8px;
}

.history-table-wrapper::-webkit-scrollbar-track {
  background: var(--bg-secondary);
}

/* 状态标签样式优化 */
.status-tag {
  font-weight: 700;
  font-size: 14px;
  padding: 8px 14px;
  display: inline-flex;
  align-items: center;
  cursor: default;
  transition: all 0.3s;
  color: #ffffff !important;
  border: none !important;
}

.status-tag:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.25);
}

/* AC 状态 - 通过 - 绿色 */
:deep(.el-tag--success.el-tag--dark.status-tag) {
  background: linear-gradient(135deg, #52c41a 0%, #73d13d 100%) !important;
  border-color: #52c41a !important;
  color: #ffffff !important;
  animation: pulse 2s ease-in-out infinite;
  font-weight: 800;
}

:deep(.el-tag--success.el-tag--dark.status-tag svg) {
  color: #ffffff !important;
  fill: #ffffff !important;
}

/* RE 状态 - 运行时错误 - 红色 */
:deep(.el-tag--danger.status-tag) {
  background: linear-gradient(135deg, #ff4d4f 0%, #ff7875 100%) !important;
  border-color: #ff4d4f !important;
  color: #ffffff !important;
  font-weight: 800;
}

:deep(.el-tag--danger.status-tag svg) {
  color: #ffffff !important;
  fill: #ffffff !important;
}

/* WA/CE 状态 - 答案错误/编译错误 - 橙色 */
:deep(.el-tag--warning.status-tag) {
  background: linear-gradient(135deg, #fa8c16 0%, #ffa940 100%) !important;
  border-color: #fa8c16 !important;
  color: #ffffff !important;
  font-weight: 800;
}

:deep(.el-tag--warning.status-tag svg) {
  color: #ffffff !important;
  fill: #ffffff !important;
}

/* TLE/MLE/PENDING 状态 - 超时/内存/等待 - 蓝灰色 */
:deep(.el-tag--info.status-tag) {
  background: linear-gradient(135deg, #597ef7 0%, #85a5ff 100%) !important;
  border-color: #597ef7 !important;
  color: #ffffff !important;
  font-weight: 800;
}

:deep(.el-tag--info.status-tag svg) {
  color: #ffffff !important;
  fill: #ffffff !important;
}

/* AC 状态脉动动画 */
@keyframes pulse {
  0%, 100% {
    box-shadow: 0 0 0 0 rgba(82, 196, 26, 0.5);
  }
  50% {
    box-shadow: 0 0 0 10px rgba(82, 196, 26, 0);
  }
}

.history-table-wrapper::-webkit-scrollbar-thumb {
  background: #424242;
  border-radius: 4px;
}

.history-table-wrapper::-webkit-scrollbar-thumb:hover {
  background: #4e4e4e;
}

.editor-card {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.editor-card :deep(.el-card__body) {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* 运行结果面板样式 */
.result-panel {
  margin-top: 20px;
  padding: 20px;
  border-radius: 8px;
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-left-width: 6px;
  background: rgba(15, 23, 42, 0.92);
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.45);
  transition: all 0.3s;
  color: #e5eaf5;
}

.result-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 15px;
}

.result-icon {
  width: 32px;
  height: 32px;
  flex-shrink: 0;
}

.result-title {
  margin: 0;
  font-size: 18px;
  font-weight: 700;
}

.result-status-text {
  font-size: 14px;
  font-weight: 600;
  margin-left: auto;
}

/* AC - 通过 */
.result-panel.result-success {
  border-left-color: #52c41a;
}

.result-panel.result-success .result-icon {
  color: #7ed957;
}

.result-panel.result-success .result-title,
.result-panel.result-success .result-status-text {
  color: #d4f8c4;
}

/* WA/CE - 答案错误/编译错误 */
.result-panel.result-warning {
  border-left-color: #d48806;
}

.result-panel.result-warning .result-icon {
  color: #ffcf75;
}

.result-panel.result-warning .result-title,
.result-panel.result-warning .result-status-text {
  color: #ffe0a1;
}

/* RE - 运行时错误 */
.result-panel.result-error {
  border-left-color: #ff4d4f;
}

.result-panel.result-error .result-icon {
  color: #ff9ea8;
}

.result-panel.result-error .result-title,
.result-panel.result-error .result-status-text {
  color: #ffd5da;
}

/* TLE/MLE - 超时/内存超限 */
.result-panel.result-info {
  border-left-color: #177ddc;
}

.result-panel.result-info .result-icon {
  color: #56aaff;
}

.result-panel.result-info .result-title,
.result-panel.result-info .result-status-text {
  color: #b3ddff;
}

/* 结果信息样式 */
.result-info {
  margin-top: 12px;
  padding: 8px 0;
  display: flex;
  align-items: center;
}

.result-description {
  margin-top: 12px;
  padding: 12px;
  background: rgba(255, 255, 255, 0.05);
  border-left: 3px solid var(--text-secondary);
  color: var(--text-primary);
  font-size: 14px;
  line-height: 1.6;
}

/* 错误信息和输出样式 */
.result-error, .result-output {
  margin-top: 15px;
}

.result-error h5, .result-output h5 {
  margin: 0 0 10px 0;
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
}

.error-pre, .output-pre {
  padding: 12px;
  border-radius: 6px;
  overflow-x: auto;
  margin: 0;
  font-family: 'Courier New', Consolas, monospace;
  font-size: 13px;
  line-height: 1.6;
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.error-pre {
  color: #ffe0e3;
  background-color: #2b1a21;
  border-color: rgba(255, 77, 79, 0.55);
}

.output-pre {
  color: #d6ecff;
  background-color: #121f33;
  border-color: rgba(64, 169, 255, 0.55);
}
</style>

<style>
/* 全局样式：语言选择下拉框宽度 */
.language-select-dropdown {
  width: 320px !important;
  min-width: 320px !important;
}

.language-select-dropdown .el-select-dropdown__item {
  padding: 0 20px;
  white-space: nowrap;
  overflow: visible;
}
</style>
