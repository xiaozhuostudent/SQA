<template>
  <div class="create-question-dialog">
    <el-dialog
      v-model="visible"
      :title="title"
      width="800px"
      :close-on-click-modal="false"
      @close="handleClose"
    >
      <!-- 题目类型选择 -->
      <el-form :model="form" label-width="120px">
        <el-form-item label="题目类型">
          <el-radio-group v-model="questionCategory" @change="handleCategoryChange" class="question-type-selector">
            <el-radio-button label="objective">客观题</el-radio-button>
            <el-radio-button label="subjective">主观题</el-radio-button>
          </el-radio-group>
        </el-form-item>
      </el-form>

      <!-- 客观题表单 -->
      <el-form
        v-if="questionCategory === 'objective'"
        :model="form"
        :rules="objectiveRules"
        ref="objectiveFormRef"
        label-width="120px"
      >
        <el-form-item label="题型" prop="questionType">
          <el-select v-model="form.questionType" placeholder="请选择题型">
            <el-option label="单选题" value="single_choice" />
            <el-option label="多选题" value="multiple_choice" />
            <el-option label="判断题" value="true_false" />
            <el-option label="填空题" value="fill_blank" />
          </el-select>
        </el-form-item>

        <el-form-item label="题目内容" prop="content">
          <el-input
            v-model="form.content"
            type="textarea"
            :rows="4"
            placeholder="请输入题目内容"
          />
        </el-form-item>

        <!-- 选择题选项 -->
        <el-form-item
          v-if="form.questionType === 'single_choice' || form.questionType === 'multiple_choice'"
          label="选项"
          prop="options"
        >
          <div v-for="(option, index) in options" :key="index" class="option-item">
            <el-input
              v-model="option.label"
              :placeholder="`选项 ${String.fromCharCode(65 + index)}`"
              style="width: calc(100% - 100px)"
            />
            <el-button
              v-if="options.length > 2"
              type="danger"
              link
              @click="removeOption(index)"
              style="margin-left: 10px"
            >
              删除
            </el-button>
          </div>
          <el-button type="primary" link @click="addOption" style="margin-top: 10px">
            + 添加选项
          </el-button>
        </el-form-item>

        <el-form-item label="正确答案" prop="answer">
          <!-- 单选题 -->
          <el-radio-group
            v-if="form.questionType === 'single_choice'"
            v-model="form.answer"
          >
            <el-radio
              v-for="(option, index) in options"
              :key="index"
              :label="String.fromCharCode(65 + index)"
            >
              {{ String.fromCharCode(65 + index) }}
            </el-radio>
          </el-radio-group>

          <!-- 多选题 -->
          <el-checkbox-group
            v-else-if="form.questionType === 'multiple_choice'"
            v-model="multipleAnswer"
          >
            <el-checkbox
              v-for="(option, index) in options"
              :key="index"
              :label="String.fromCharCode(65 + index)"
            >
              {{ String.fromCharCode(65 + index) }}
            </el-checkbox>
          </el-checkbox-group>

          <!-- 判断题 -->
          <el-radio-group v-else-if="form.questionType === 'true_false'" v-model="form.answer">
            <el-radio label="true">正确</el-radio>
            <el-radio label="false">错误</el-radio>
          </el-radio-group>

          <!-- 填空题 -->
          <el-input
            v-else-if="form.questionType === 'fill_blank'"
            v-model="form.answer"
            placeholder="请输入正确答案（多个答案用|分隔）"
          />
        </el-form-item>

        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="难度" prop="difficulty">
              <el-select v-model="form.difficulty" placeholder="请选择难度">
                <el-option label="简单" value="easy" />
                <el-option label="中等" value="medium" />
                <el-option label="困难" value="hard" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="分值" prop="score">
              <el-input-number v-model="form.score" :min="1" :max="100" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="学生可见">
              <el-switch v-model="isVisible" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="答案解析">
          <el-input
            v-model="form.explanation"
            type="textarea"
            :rows="3"
            placeholder="请输入答案解析（可选）"
          />
        </el-form-item>

        <el-form-item label="标签">
          <el-input
            v-model="form.tags"
            placeholder="多个标签用逗号分隔（可选）"
          />
        </el-form-item>

        <el-form-item label="知识点">
          <el-input
            v-model="form.knowledgePoints"
            placeholder="多个知识点用逗号分隔（可选）"
          />
        </el-form-item>
      </el-form>

      <!-- 主观题表单 -->
      <el-form
        v-else
        :model="form"
        :rules="subjectiveRules"
        ref="subjectiveFormRef"
        label-width="120px"
      >
        <el-form-item label="题型" prop="questionType">
          <el-select v-model="form.questionType" placeholder="请选择题型">
            <el-option label="简答题" value="short_answer" />
            <el-option label="编程题" value="programming" />
          </el-select>
        </el-form-item>

        <el-form-item label="题目内容" prop="content">
          <el-input
            v-model="form.content"
            type="textarea"
            :rows="6"
            placeholder="请输入题目内容"
          />
        </el-form-item>

        <el-form-item label="标准答案" prop="answer">
          <el-input
            v-model="form.answer"
            type="textarea"
            :rows="6"
            placeholder="请输入标准答案（用于AI评分参考）"
          />
        </el-form-item>
        

        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="难度" prop="difficulty">
              <el-select v-model="form.difficulty" placeholder="请选择难度">
                <el-option label="简单" value="easy" />
                <el-option label="中等" value="medium" />
                <el-option label="困难" value="hard" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="分值" prop="score">
              <el-input-number v-model="form.score" :min="1" :max="100" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="学生可见">
              <el-switch v-model="isVisible" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="答案解析">
          <el-input
            v-model="form.explanation"
            type="textarea"
            :rows="3"
            placeholder="请输入答案解析（可选）"
          />
        </el-form-item>

        <el-form-item label="标签">
          <el-input
            v-model="form.tags"
            placeholder="多个标签用逗号分隔（可选）"
          />
        </el-form-item>

        <el-form-item label="知识点">
          <el-input
            v-model="form.knowledgePoints"
            placeholder="多个知识点用逗号分隔（可选）"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="handleClose">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, watch, computed } from 'vue'
import { ElMessage } from 'element-plus'
import axios from 'axios'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  courseId: {
    type: Number,
    default: null
  }
})

const emit = defineEmits(['update:modelValue', 'success'])

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const title = ref('创建题目')
const questionCategory = ref('objective') // objective: 客观题, subjective: 主观题
const submitting = ref(false)
const objectiveFormRef = ref(null)
const subjectiveFormRef = ref(null)

// 表单数据
const form = reactive({
  courseId: null,
  questionType: 'single_choice',
  difficulty: 'medium',
  content: '',
  options: null,
  answer: '',
  explanation: '',
  score: 5,
  tags: '',
  knowledgePoints: '',
  status: 'active',
  isVisible: 1
})

// 监听courseId变化，自动设置到表单
watch(() => props.courseId, (newCourseId) => {
  if (newCourseId) {
    form.courseId = newCourseId
  }
}, { immediate: true })

// 选项数据（用于选择题）
const options = ref([
  { label: '' },
  { label: '' }
])

// 多选题答案
const multipleAnswer = ref([])

// 是否可见开关
const isVisible = ref(true)

// 客观题验证规则
const objectiveRules = {
  questionType: [{ required: true, message: '请选择题型', trigger: 'change' }],
  content: [{ required: true, message: '请输入题目内容', trigger: 'blur' }],
  answer: [{ required: true, message: '请设置正确答案', trigger: 'change' }],
  difficulty: [{ required: true, message: '请选择难度', trigger: 'change' }],
  score: [{ required: true, message: '请设置分值', trigger: 'blur' }]
}

// 主观题验证规则
const subjectiveRules = {
  questionType: [{ required: true, message: '请选择题型', trigger: 'change' }],
  content: [{ required: true, message: '请输入题目内容', trigger: 'blur' }],
  answer: [{ required: true, message: '请输入标准答案', trigger: 'blur' }],
  difficulty: [{ required: true, message: '请选择难度', trigger: 'change' }],
  score: [{ required: true, message: '请设置分值', trigger: 'blur' }]
}

// 添加选项
const addOption = () => {
  if (options.value.length < 10) {
    options.value.push({ label: '' })
  } else {
    ElMessage.warning('最多只能添加10个选项')
  }
}

// 删除选项
const removeOption = (index) => {
  options.value.splice(index, 1)
}

// 题目类别切换
const handleCategoryChange = () => {
  // 重置表单
  form.questionType = questionCategory.value === 'objective' ? 'single_choice' : 'short_answer'
  form.content = ''
  form.answer = ''
  form.explanation = ''
  form.tags = ''
  form.knowledgePoints = ''
  options.value = [{ label: '' }, { label: '' }]
  multipleAnswer.value = []
}

// 监听题型变化
watch(() => form.questionType, (newType) => {
  form.answer = ''
  multipleAnswer.value = []
  if (newType === 'true_false') {
    options.value = []
  } else if (newType === 'single_choice' || newType === 'multiple_choice') {
    if (options.value.length === 0) {
      options.value = [{ label: '' }, { label: '' }]
    }
  }
})

// 监听可见开关
watch(isVisible, (val) => {
  form.isVisible = val ? 1 : 0
})

// 提交表单
const handleSubmit = async () => {
  const formRef = questionCategory.value === 'objective' ? objectiveFormRef.value : subjectiveFormRef.value
  
  try {
    await formRef.validate()
  } catch (error) {
    ElMessage.warning('请完善必填项')
    return
  }

  // 处理选择题的选项和答案
  if (form.questionType === 'single_choice' || form.questionType === 'multiple_choice') {
    // 验证选项是否填写完整
    if (options.value.some(opt => !opt.label.trim())) {
      ElMessage.warning('请填写所有选项内容')
      return
    }

    // 转换为JSON格式
    form.options = JSON.stringify(
      options.value.map((opt, index) => ({
        key: String.fromCharCode(65 + index),
        value: opt.label
      }))
    )

    // 处理多选题答案
    if (form.questionType === 'multiple_choice') {
      if (multipleAnswer.value.length === 0) {
        ElMessage.warning('请至少选择一个正确答案')
        return
      }
      form.answer = multipleAnswer.value.sort().join(',')
    }
  } else {
    form.options = null
  }

  // 处理判断题答案
  if (form.questionType === 'true_false') {
    // answer已经是'true'或'false'字符串
  }

  submitting.value = true

  try {
    const token = localStorage.getItem('token')
    const response = await axios.post(
      '/api/questions/create',
      form,
      {
        headers: {
          Authorization: `Bearer ${token}`
        }
      }
    )

    if (response.data.code === 200) {
      ElMessage.success('题目创建成功')
      emit('success', response.data.data)
      handleClose()
    } else {
      ElMessage.error(response.data.msg || '创建失败')
    }
  } catch (error) {
    console.error('创建题目失败:', error)
    ElMessage.error('创建题目失败: ' + (error.response?.data?.msg || error.message))
  } finally {
    submitting.value = false
  }
}

// 关闭对话框
const handleClose = () => {
  // 重置表单
  questionCategory.value = 'objective'
  form.courseId = props.courseId // 保持课程ID
  form.questionType = 'single_choice'
  form.difficulty = 'medium'
  form.content = ''
  form.options = null
  form.answer = ''
  form.explanation = ''
  form.score = 5
  form.tags = ''
  form.knowledgePoints = ''
  form.status = 'active'
  form.isVisible = 1
  options.value = [{ label: '' }, { label: '' }]
  multipleAnswer.value = []
  isVisible.value = true
  
  visible.value = false
}
</script>

<style scoped>
.option-item {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
}

:deep(.el-alert ul) {
  list-style: disc;
}

/* 题目类型选择器样式 - TokyoNight主题 */
.question-type-selector :deep(.el-radio-button__inner) {
  padding: 12px 24px;
  font-weight: 500;
  background-color: #24283b;
  border-color: #414868;
  color: #a9b1d6;
}

.question-type-selector :deep(.el-radio-button__inner:hover) {
  background-color: #292e42;
  color: #c0caf5;
}

.question-type-selector :deep(.el-radio-button__original-radio:checked + .el-radio-button__inner) {
  background-color: #7aa2f7;
  border-color: #7aa2f7;
  color: #1a1b26;
  box-shadow: -1px 0 0 0 #7aa2f7;
  font-weight: 600;
}

.question-type-selector :deep(.el-radio-button:first-child .el-radio-button__inner) {
  border-radius: 4px 0 0 4px;
}

.question-type-selector :deep(.el-radio-button:last-child .el-radio-button__inner) {
  border-radius: 0 4px 4px 0;
}

/* AI评分说明样式 - TokyoNight主题 */
.ai-grading-alert :deep(.el-alert) {
  background-color: #24283b;
  border-color: #414868;
}

.ai-grading-alert :deep(.el-alert__title) {
  color: #7dcfff;
  font-weight: 600;
}

.ai-grading-alert :deep(.el-alert__description) {
  color: #a9b1d6;
}

.ai-grading-alert :deep(ul) {
  color: #c0caf5;
}

.ai-grading-alert :deep(li) {
  margin: 6px 0;
}
</style>
