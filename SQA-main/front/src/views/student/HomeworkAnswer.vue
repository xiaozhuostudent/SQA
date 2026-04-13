<template>
  <div class="homework-answer-page">
    <el-card v-loading="loading">
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center;">
          <span style="font-size: 18px; font-weight: bold;">{{ currentHomework.homework?.title }}</span>
          <div>
            <el-tag type="warning" size="large" style="margin-right: 15px;">
              截止时间: {{ currentHomework.homework?.deadline }}
            </el-tag>
            <el-tag type="primary" size="large">
              总分: {{ currentHomework.homework?.totalScore }}
            </el-tag>
          </div>
        </div>
      </template>

      <!-- 题目列表 -->
      <div v-if="currentHomework.questions && currentHomework.questions.length > 0">
        <div v-for="(question, index) in currentHomework.questions" :key="question.id" style="margin-bottom: 30px">
          <el-card shadow="hover">
            <template #header>
              <div style="display: flex; justify-content: space-between; align-items: center">
                <span style="font-size: 16px;">
                  第 {{ index + 1 }} 题
                  <el-tag size="small" style="margin-left: 10px">{{ getQuestionTypeName(question.questionType) }}</el-tag>
                </span>
                <el-tag type="primary" size="large">{{ question.score }} 分</el-tag>
              </div>
            </template>

            <div style="margin-bottom: 20px; font-size: 15px;">
              <strong>题目:</strong> {{ question.content }}
            </div>

            <!-- 单选题 -->
            <div v-if="question.questionType === 'single_choice'" style="margin-left: 20px; margin-bottom: 15px;">
              <el-radio-group v-model="answers[question.id]" size="large">
                <el-radio 
                  v-for="(option, i) in parseOptions(question.options)" 
                  :key="i" 
                  :label="option.key || String.fromCharCode(65 + i)"
                  style="display: block; margin-bottom: 12px; padding: 8px;">
                  {{ option.key || String.fromCharCode(65 + i) }}. {{ option.value || option }}
                </el-radio>
              </el-radio-group>
            </div>

            <!-- 多选题 -->
            <div v-if="question.questionType === 'multiple_choice'" style="margin-left: 20px; margin-bottom: 15px;">
              <el-checkbox-group v-model="answers[question.id]" size="large">
                <el-checkbox 
                  v-for="(option, i) in parseOptions(question.options)" 
                  :key="i" 
                  :label="option.key || String.fromCharCode(65 + i)"
                  style="display: block; margin-bottom: 12px; padding: 8px;">
                  {{ option.key || String.fromCharCode(65 + i) }}. {{ option.value || option }}
                </el-checkbox>
              </el-checkbox-group>
            </div>

            <!-- 判断题 -->
            <div v-if="question.questionType === 'true_false'">
              <el-radio-group v-model="answers[question.id]" size="large">
                <el-radio label="true" style="margin-right: 30px;">正确</el-radio>
                <el-radio label="false">错误</el-radio>
              </el-radio-group>
            </div>

            <!-- 填空题 -->
            <div v-if="question.questionType === 'fill_blank'">
              <el-input 
                v-model="answers[question.id]" 
                placeholder="请输入答案"
                type="textarea"
                :rows="3"
                size="large"
              />
            </div>

            <!-- 简答题 -->
            <div v-if="question.questionType === 'short_answer'">
              <el-input 
                v-model="answers[question.id]" 
                placeholder="请输入答案"
                type="textarea"
                :rows="6"
                size="large"
              />
            </div>

            <!-- 编程题 -->
            <div v-if="question.questionType === 'programming'">
              <el-input 
                v-model="answers[question.id]" 
                placeholder="请输入代码"
                type="textarea"
                :rows="10"
                size="large"
              />
            </div>
          </el-card>
        </div>

        <!-- 提交按钮 -->
        <div style="text-align: center; margin-top: 30px;">
          <el-button size="large" @click="goBack">返回</el-button>
          <el-button 
            type="primary" 
            size="large" 
            @click="handleSubmit" 
            :loading="submitting"
            style="margin-left: 20px; padding: 12px 40px;">
            提交作业
          </el-button>
        </div>
      </div>

      <el-empty v-else description="暂无题目" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  getHomeworkDetail,
  startHomework as startHomeworkApi,
  submitHomework as submitHomeworkApi
} from '@/api/homework'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const studentId = computed(() => userStore.userInfo?.id)
const homeworkId = computed(() => parseInt(route.params.id))

const loading = ref(false)
const submitting = ref(false)
const currentHomework = ref({})
const answers = reactive({})

// 题型映射
const getQuestionTypeName = (type) => {
  const typeMap = {
    'single_choice': '单选题',
    'multiple_choice': '多选题',
    'true_false': '判断题',
    'fill_blank': '填空题',
    'short_answer': '简答题',
    'programming': '编程题'
  }
  return typeMap[type] || type
}

// 解析选项
const parseOptions = (options) => {
  if (!options) return []
  if (typeof options === 'string') {
    try {
      return JSON.parse(options)
    } catch {
      return []
    }
  }
  return options
}

// 加载作业详情
const loadHomeworkDetail = async () => {
  if (!studentId.value) {
    ElMessage.error('请先登录')
    router.push('/login')
    return
  }

  if (!homeworkId.value) {
    ElMessage.error('作业ID无效')
    router.back()
    return
  }

  loading.value = true
  try {
    // 先调用开始作业接口
    const startRes = await startHomeworkApi(homeworkId.value, studentId.value)
    
    // 获取作业详情
    const detailRes = await getHomeworkDetail(homeworkId.value)
    if (detailRes.code === 200) {
      currentHomework.value = detailRes.data
      currentHomework.value.studentHomeworkId = startRes.data.id
      
      // 初始化答案
      Object.keys(answers).forEach(key => delete answers[key])
      
      detailRes.data.questions?.forEach(q => {
        if (q.questionType === 'multiple_choice') {
          answers[q.id] = []
        }
      })
      
      console.log('作业详情加载成功:', currentHomework.value)
    } else {
      ElMessage.error('加载作业详情失败')
      router.back()
    }
  } catch (error) {
    console.error('加载作业失败:', error)
    ElMessage.error('加载作业失败')
    router.back()
  } finally {
    loading.value = false
  }
}

// 提交作业
const handleSubmit = async () => {
  try {
    await ElMessageBox.confirm('确定要提交作业吗？提交后将不能修改。', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    submitting.value = true

    // 构造答案数组
    const answerList = currentHomework.value.questions.map(q => {
      const answer = answers[q.id]
      return {
        questionId: q.id,
        studentAnswer: Array.isArray(answer) 
          ? answer.join(',') 
          : (answer ? String(answer) : '')
      }
    })

    console.log('提交的答案:', answerList)

    const res = await submitHomeworkApi(
      currentHomework.value.studentHomeworkId, 
      answerList
    )

    ElMessage.success(res.message || '作业提交成功')
    
    // 关闭当前页面，返回作业列表
    setTimeout(() => {
      window.close()
    }, 1000)
  } catch (error) {
    if (error !== 'cancel') {
      console.error('提交作业失败:', error)
      ElMessage.error(error.response?.data?.message || error.message || '提交失败')
    }
  } finally {
    submitting.value = false
  }
}

// 返回
const goBack = () => {
  router.push('/student/homework')
}

onMounted(() => {
  loadHomeworkDetail()
})
</script>

<style scoped>
.homework-answer-page {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

:deep(.el-card__header) {
  background-color: #f5f7fa;
}

:deep(.el-radio),
:deep(.el-checkbox) {
  white-space: normal;
  line-height: 1.8;
}

:deep(.el-radio__label),
:deep(.el-checkbox__label) {
  white-space: normal;
  word-break: break-word;
}
</style>
