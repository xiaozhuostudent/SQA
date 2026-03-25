<template>
  <div class="experiment-page">
    <el-card>
      <template #header>
        <span>实验管理</span>
      </template>

      <el-table :data="experiments" style="width: 100%" v-loading="loading" element-loading-text="加载实验数据中...">
        <el-table-column prop="title" label="实验名称" width="200" />
        <el-table-column prop="courseName" label="课程" width="150" />
        <el-table-column prop="deadline" label="截止时间" width="180" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">{{ scope.row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250">
          <template #default="scope">
            <el-button size="small" type="primary" @click="startExperiment(scope.row)">
              开始实验
            </el-button>
            <el-button size="small" @click="submitReport(scope.row)">
              提交报告
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 在线代码编辑器对话框 -->
    <el-dialog v-model="codeEditorVisible" title="在线代码编辑器" width="90%" fullscreen>
      <div class="code-editor-container">
        <div class="editor-header">
          <el-alert
            title="在线代码执行环境已就绪"
            type="success"
            :closable="false"
          />
          <div class="editor-controls">
            <el-select v-model="editorLanguage" placeholder="选择编程语言" style="width: 220px" filterable>
              <el-option-group label="⭐ 重点语言">
                <el-option
                  v-for="lang in languageOptions.filter(l => l.category === '重点')"
                  :key="lang.id"
                  :label="lang.name"
                  :value="lang.id"
                />
              </el-option-group>
              <el-option-group label="📜 脚本语言">
                <el-option
                  v-for="lang in languageOptions.filter(l => l.category === '脚本')"
                  :key="lang.id"
                  :label="lang.name"
                  :value="lang.id"
                />
              </el-option-group>
              <el-option-group label="🔧 系统语言">
                <el-option
                  v-for="lang in languageOptions.filter(l => l.category === '系统')"
                  :key="lang.id"
                  :label="lang.name"
                  :value="lang.id"
                />
              </el-option-group>
              <el-option-group label="☕ JVM语言">
                <el-option
                  v-for="lang in languageOptions.filter(l => l.category === 'JVM')"
                  :key="lang.id"
                  :label="lang.name"
                  :value="lang.id"
                />
              </el-option-group>
              <el-option-group label="🔷 .NET语言">
                <el-option
                  v-for="lang in languageOptions.filter(l => l.category === '.NET')"
                  :key="lang.id"
                  :label="lang.name"
                  :value="lang.id"
                />
              </el-option-group>
              <el-option-group label="λ 函数式">
                <el-option
                  v-for="lang in languageOptions.filter(l => l.category === '函数式')"
                  :key="lang.id"
                  :label="lang.name"
                  :value="lang.id"
                />
              </el-option-group>
              <el-option-group label="📊 数据科学">
                <el-option
                  v-for="lang in languageOptions.filter(l => l.category === '数据科学')"
                  :key="lang.id"
                  :label="lang.name"
                  :value="lang.id"
                />
              </el-option-group>
              <el-option-group label="📦 其他">
                <el-option
                  v-for="lang in languageOptions.filter(l => l.category === '其他')"
                  :key="lang.id"
                  :label="lang.name"
                  :value="lang.id"
                />
              </el-option-group>
            </el-select>
            <el-button type="primary" :loading="isRunning" @click="runCode">
              <span v-if="!isRunning">▶ 运行代码</span>
              <span v-else>执行中...</span>
            </el-button>
            <el-tag type="success" effect="plain">支持35+种编程语言</el-tag>
          </div>
        </div>

        <div class="editor-body">
          <div class="code-panel">
            <div class="panel-header">代码编辑区</div>
            <el-input
              v-model="editorCode"
              type="textarea"
              :rows="20"
              class="code-textarea"
              placeholder="在此输入代码..."
            />
          </div>

          <div class="output-panel">
            <div class="panel-header">运行结果</div>
            <pre class="output-content">{{ editorOutput || '点击"运行代码"按钮执行程序' }}</pre>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="codeEditorVisible = false">关闭编辑器</el-button>
      </template>
    </el-dialog>

    <!-- 提交报告对话框 -->
    <el-dialog v-model="reportDialogVisible" title="提交实验报告" width="700px">
      <el-alert
        v-if="hasSubmittedReport"
        title="您已经提交过该实验报告"
        type="warning"
        description="再次提交将覆盖之前的内容，包括文件和实验总结"
        :closable="false"
        show-icon
        style="margin-bottom: 20px;"
      />
      <el-form :model="reportForm" label-width="100px">
        <el-form-item label="实验名称">
          <el-input v-model="currentExperiment.title" disabled />
        </el-form-item>
        <el-form-item label="实验总结">
          <el-input
            v-model="reportForm.summary"
            type="textarea"
            :rows="8"
            placeholder="请输入实验总结"
          />
        </el-form-item>
        <el-form-item label="实验报告">
          <el-upload
            ref="uploadRef"
            :action="uploadAction"
            :headers="uploadHeaders"
            :file-list="reportForm.files"
            :on-success="handleUploadSuccess"
            :on-remove="handleRemoveFile"
            :on-error="handleUploadError"
            :show-file-list="true"
          >
            <el-button size="small">上传报告文件</el-button>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reportDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitReport">{{ hasSubmittedReport ? '重新提交' : '提交' }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { executeCode, getSupportedLanguages } from '@/api/codeExecution'
import { getExperimentList, submitExperimentReport } from '@/api/experiment'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false) // 页面加载状态
const envDialogVisible = ref(false)
const reportDialogVisible = ref(false)
const currentExperiment = ref({})
const uploadRef = ref(null)
const hasSubmittedReport = ref(false)
const reportForm = ref({
  summary: '',
  files: []
})

// 上传配置
const uploadAction = '/api/upload'
const uploadHeaders = ref({
  'Authorization': 'Bearer ' + localStorage.getItem('token')
})

const experiments = ref([])

const getStatusType = (status) => {
  const typeMap = {
    '未开始': 'info',
    '进行中': 'warning',
    '已完成': 'success'
  }
  return typeMap[status] || 'info'
}

const loadExperiments = async () => {
  loading.value = true
  try {
    const res = await getExperimentList()
    const list = res.data || []
    
    // 处理状态显示
    experiments.value = list.map(item => {
      const now = new Date()
      const start = new Date(item.startTime)
      const end = new Date(item.deadline)
      
      let status = '未开始'
      if (now > end) {
        status = '已完成'
      } else if (now >= start) {
        status = '进行中'
      }
      
      return {
        ...item,
        status
      }
    })
  } catch (error) {
    console.error('获取实验列表失败:', error)
    ElMessage.error('获取实验列表失败')
  } finally {
    loading.value = false
  }
}

const codeEditorVisible = ref(false)
const editorCode = ref(`# Python 3示例代码
def fibonacci(n):
    if n <= 1:
        return n
    return fibonacci(n-1) + fibonacci(n-2)

print("斐波那契数列前10项:")
for i in range(10):
    print(f"F({i}) = {fibonacci(i)}")
`)

// 代码模板
const codeTemplates = {
  cpp: `#include <iostream>
#include <vector>
using namespace std;

int main() {
    cout << "C++ 示例 - 冒泡排序" << endl;
    
    vector<int> arr = {64, 34, 25, 12, 22, 11, 90};
    int n = arr.size();
    
    // 冒泡排序
    for (int i = 0; i < n-1; i++) {
        for (int j = 0; j < n-i-1; j++) {
            if (arr[j] > arr[j+1]) {
                swap(arr[j], arr[j+1]);
            }
        }
    }
    
    cout << "排序后: ";
    for (int num : arr) {
        cout << num << " ";
    }
    cout << endl;
    
    return 0;
}`,
  
  c: `#include <stdio.h>

int main() {
    printf("C 示例 - 斐波那契数列\\n");
    
    int n = 10;
    int fib[n];
    fib[0] = 0;
    fib[1] = 1;
    
    for (int i = 2; i < n; i++) {
        fib[i] = fib[i-1] + fib[i-2];
    }
    
    printf("前10项斐波那契数列:\\n");
    for (int i = 0; i < n; i++) {
        printf("F(%d) = %d\\n", i, fib[i]);
    }
    
    return 0;
}`,
  
  java: `public class Main {
    public static void main(String[] args) {
        System.out.println("Java 示例 - 快速排序");
        
        int[] arr = {64, 34, 25, 12, 22, 11, 90};
        
        System.out.print("排序前: ");
        printArray(arr);
        
        quickSort(arr, 0, arr.length - 1);
        
        System.out.print("排序后: ");
        printArray(arr);
    }
    
    static void quickSort(int[] arr, int low, int high) {
        if (low < high) {
            int pi = partition(arr, low, high);
            quickSort(arr, low, pi - 1);
            quickSort(arr, pi + 1, high);
        }
    }
    
    static int partition(int[] arr, int low, int high) {
        int pivot = arr[high];
        int i = low - 1;
        
        for (int j = low; j < high; j++) {
            if (arr[j] < pivot) {
                i++;
                int temp = arr[i];
                arr[i] = arr[j];
                arr[j] = temp;
            }
        }
        
        int temp = arr[i + 1];
        arr[i + 1] = arr[high];
        arr[high] = temp;
        
        return i + 1;
    }
    
    static void printArray(int[] arr) {
        for (int num : arr) {
            System.out.print(num + " ");
        }
        System.out.println();
    }
}`,
  
  python: `# Python 3示例代码
def fibonacci(n):
    if n <= 1:
        return n
    return fibonacci(n-1) + fibonacci(n-2)

print("斐波那契数列前10项:")
for i in range(10):
    print(f"F({i}) = {fibonacci(i)}")`
}
const editorLanguage = ref('python') // 默认Python
const editorOutput = ref('')
const isRunning = ref(false)

// 常用编程语言列表 (前5个为重点语言)
const languageOptions = ref([
  // === 常用语言 (置顶方便查找) ===
  { id: 'cpp', name: 'C++', category: '常用' },
  { id: 'python', name: 'Python 3', category: '常用' },
  { id: 'java', name: 'Java', category: '常用' },
  { id: 'c', name: 'C', category: '常用' },
  { id: 'go', name: 'Go', category: '常用' },
  
  // === 常用脚本语言 ===
  { id: 'javascript', name: 'JavaScript (Node.js)', category: '脚本' },
  { id: 'typescript', name: 'TypeScript', category: '脚本' },
  { id: 'php', name: 'PHP', category: '脚本' },
  { id: 'ruby', name: 'Ruby', category: '脚本' },
  { id: 'perl', name: 'Perl', category: '脚本' },
  { id: 'bash', name: 'Bash', category: '脚本' },
  
  // === 现代系统语言 ===
  { id: 'rust', name: 'Rust', category: '系统' },
  { id: 'swift', name: 'Swift', category: '系统' },
  { id: 'kotlin', name: 'Kotlin', category: '系统' },
  
  // === .NET系列 ===
  { id: 'csharp', name: 'C#', category: '.NET' },
  { id: 'fsharp', name: 'F#', category: '.NET' },
  
  // === JVM语言 ===
  { id: 'scala', name: 'Scala', category: 'JVM' },
  { id: 'groovy', name: 'Groovy', category: 'JVM' },
  { id: 'clojure', name: 'Clojure', category: 'JVM' },
  
  // === 函数式语言 ===
  { id: 'haskell', name: 'Haskell', category: '函数式' },
  { id: 'ocaml', name: 'OCaml', category: '函数式' },
  { id: 'elixir', name: 'Elixir', category: '函数式' },
  { id: 'erlang', name: 'Erlang', category: '函数式' },
  
  // === 数据科学 ===
  { id: 'r', name: 'R', category: '数据科学' },
  { id: 'julia', name: 'Julia', category: '数据科学' },
  { id: 'octave', name: 'Octave', category: '数据科学' },
  
  // === 其他常用语言 ===
  { id: 'lua', name: 'Lua', category: '其他' },
  { id: 'dart', name: 'Dart', category: '其他' },
  { id: 'pascal', name: 'Pascal', category: '其他' },
  { id: 'fortran', name: 'Fortran', category: '其他' },
  { id: 'cobol', name: 'COBOL', category: '其他' },
  { id: 'assembly', name: 'Assembly (NASM)', category: '其他' }
])

const startExperiment = (experiment) => {
  // 跳转到实验题目列表页面
  router.push({
    name: 'StudentExperimentProblems',
    params: { id: experiment.id }
  })
}

const runCode = async () => {
  if (!editorCode.value.trim()) {
    ElMessage.warning('请输入代码')
    return
  }

  isRunning.value = true
  editorOutput.value = '正在执行代码...\n'

  try {
    // 使用Piston API (开源免费,支持50+语言)
    const result = await executeCode(editorCode.value, editorLanguage.value)
    
    if (result.success) {
      editorOutput.value = `=== 执行成功 ===\n${result.output || '(无输出)'}`
      ElMessage.success('代码执行完成')
    } else {
      editorOutput.value = `=== 执行失败 ===\n${result.error || '未知错误'}`
      ElMessage.error('代码执行失败')
    }
  } catch (error) {
    editorOutput.value = `=== 错误 ===\n${error.message}`
    ElMessage.error('代码执行服务异常')
  } finally {
    isRunning.value = false
  }
}

const submitReport = async (experiment) => {
  currentExperiment.value = experiment
  hasSubmittedReport.value = false
  
  // 检查是否已经提交过
  try {
    const res = await getExperimentList()
    const exp = (res.data || []).find(e => e.id === experiment.id)
    if (exp && exp.hasSubmitted) {
      hasSubmittedReport.value = true
    }
  } catch (error) {
    console.error('检查提交状态失败:', error)
  }
  
  reportForm.value = {
    summary: '',
    files: []
  }
  reportDialogVisible.value = true
}

// 文件上传成功回调
const handleUploadSuccess = (response, file, fileList) => {
  if (response.code === 200) {
    reportForm.value.files.push({
      name: file.name,
      url: response.data.url,
      path: response.data.path
    })
  } else {
    ElMessage.error('文件上传失败: ' + response.message)
  }
}

// 文件删除回调
const handleRemoveFile = (file, fileList) => {
  const index = reportForm.value.files.findIndex(f => f.url === file.url)
  if (index > -1) {
    reportForm.value.files.splice(index, 1)
  }
}

// 文件上传失败回调
const handleUploadError = (error) => {
  ElMessage.error('文件上传失败,请重试')
  console.error('Upload error:', error)
}

const handleSubmitReport = async () => {
  if (!reportForm.value.summary || reportForm.value.summary.trim() === '') {
    ElMessage.warning('请输入实验总结')
    return
  }

  try {
    const data = {
      experimentId: currentExperiment.value.id,
      content: reportForm.value.summary,
      files: reportForm.value.files
    }
    
    await submitExperimentReport(data)
    ElMessage.success('实验报告提交成功')
    reportDialogVisible.value = false
    loadExperiments()
  } catch (error) {
    console.error('提交失败:', error)
    ElMessage.error('提交失败,请重试')
  }
}

// 组件挂载时加载数据
onMounted(async () => {
  loadExperiments()
  
  // 从Piston API动态加载语言列表
  try {
    const languages = await getSupportedLanguages()
    if (languages && languages.length > 0) {
      // 保留重点语言顺序,其他语言按分类添加
      const priorityLangs = [
        { id: 'cpp', name: 'C++', category: '重点' },
        { id: 'c', name: 'C', category: '重点' },
        { id: 'java', name: 'Java', category: '重点' },
        { id: 'python', name: 'Python', category: '重点' }
      ]
      
      // 其他常用语言
      const otherLangs = languages
        .filter(l => !['cpp', 'c', 'java', 'python'].includes(l.id))
        .map(l => ({ id: l.id, name: l.name, category: '其他' }))
      
      languageOptions.value = [...priorityLangs, ...otherLangs]
    }
  } catch (error) {
    console.error('加载语言列表失败:', error)
    ElMessage.warning('语言列表加载失败,使用默认列表')
  }
})
</script>

<style scoped>
.experiment-page {
  padding: 0;
}

.experiment-env {
  height: calc(100vh - 200px);
}

.env-info {
  margin-bottom: 20px;
  padding: 16px;
  background: var(--bg-secondary);
  border: 1px solid var(--border);
  border-radius: var(--radius-md);
  color: var(--text-primary);
}

.env-info p {
  margin: 8px 0;
  color: var(--text-primary);
}

.env-info strong {
  color: var(--text-primary);
}

.env-iframe {
  width: 100%;
  height: calc(100% - 150px);
  border: 1px solid var(--border);
  border-radius: var(--radius-md);
  background: var(--bg-secondary);
}

.experiment-page :deep(.el-card) {
  background: var(--bg-float);
  border: 1px solid var(--border);
  box-shadow: var(--shadow-sm);
}

.experiment-page :deep(.el-card__header) {
  background: var(--bg-secondary);
  border-bottom: 1px solid var(--border);
  color: var(--text-primary);
}

.experiment-page :deep(.el-table) {
  background: transparent;
  color: var(--text-primary);
}

.experiment-page :deep(.el-table th) {
  background: var(--bg-secondary);
  color: var(--text-primary);
  border-color: var(--border);
}

.experiment-page :deep(.el-table td) {
  border-color: var(--border);
}

.experiment-page :deep(.el-table tr:hover > td) {
  background: var(--bg-highlight) !important;
}

.experiment-page :deep(.el-dialog) {
  background: var(--bg-float);
  border: 1px solid var(--border);
}

.experiment-page :deep(.el-dialog__title) {
  color: var(--text-primary);
}

.experiment-page :deep(.el-alert) {
  background: var(--bg-secondary);
  border-color: var(--accent-green);
}

.experiment-page :deep(.el-alert__title) {
  color: var(--accent-green);
}

.experiment-page :deep(.el-form-item__label) {
  color: var(--text-primary);
}

.experiment-page :deep(.el-input__wrapper),
.experiment-page :deep(.el-textarea__inner) {
  background: var(--bg-secondary);
  border: 1px solid var(--border);
  color: var(--text-primary);
  box-shadow: none;
}

.experiment-page :deep(.el-select) {
  --el-select-input-focus-border-color: var(--accent-blue);
}

.experiment-page :deep(.el-button--primary) {
  background: var(--accent-blue);
  border-color: var(--accent-blue);
}

/* 代码编辑器样式 */
.code-editor-container {
  height: calc(100vh - 150px);
  display: flex;
  flex-direction: column;
}

.editor-header {
  margin-bottom: 16px;
}

.editor-controls {
  display: flex;
  gap: 12px;
  margin-top: 12px;
  align-items: center;
}

.editor-body {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  flex: 1;
  min-height: 0;
}

.code-panel,
.output-panel {
  display: flex;
  flex-direction: column;
  border: 1px solid var(--border);
  border-radius: var(--radius-md);
  overflow: hidden;
  background: var(--bg-secondary);
}

.panel-header {
  padding: 12px 16px;
  background: var(--bg-float);
  border-bottom: 1px solid var(--border);
  color: var(--text-primary);
  font-weight: 500;
}

.code-textarea {
  flex: 1;
}

.code-textarea :deep(.el-textarea__inner) {
  height: 100% !important;
  resize: none;
  font-family: 'Courier New', 'Monaco', Consolas, monospace;
  font-size: 14px;
  line-height: 1.6;
  border: none;
  border-radius: 0;
}

.output-content {
  flex: 1;
  margin: 0;
  padding: 16px;
  font-family: 'Courier New', Consolas, monospace;
  font-size: 14px;
  line-height: 1.6;
  color: var(--text-primary);
  background: var(--bg-secondary);
  overflow: auto;
  white-space: pre-wrap;
  word-wrap: break-word;
}
</style>
