<template>
  <div class="code-editor-wrapper">
    <textarea
      ref="textarea"
      v-model="localCode"
      class="code-editor-textarea"
      :placeholder="placeholder"
      @input="handleInput"
      @keydown="handleKeydown"
      spellcheck="false"
    ></textarea>
  </div>
</template>

<script setup>
import { ref, watch, nextTick } from 'vue'

const props = defineProps({
  modelValue: {
    type: String,
    default: ''
  },
  language: {
    type: String,
    default: 'python'
  },
  placeholder: {
    type: String,
    default: '在此输入代码...'
  },
  height: {
    type: String,
    default: '500px'
  }
})

const emit = defineEmits(['update:modelValue'])

const textarea = ref(null)
const localCode = ref(props.modelValue)

const handleInput = () => {
  emit('update:modelValue', localCode.value)
}

const handleKeydown = (e) => {
  // Tab键缩进支持
  if (e.key === 'Tab') {
    e.preventDefault()
    const start = e.target.selectionStart
    const end = e.target.selectionEnd
    const spaces = '  ' // 2个空格

    localCode.value = localCode.value.substring(0, start) + spaces + localCode.value.substring(end)
    
    nextTick(() => {
      textarea.value.selectionStart = textarea.value.selectionEnd = start + spaces.length
    })
  }
}

watch(() => props.modelValue, (newVal) => {
  if (newVal !== localCode.value) {
    localCode.value = newVal
  }
})

watch(() => props.language, () => {
  console.log('语言切换:', props.language)
})
</script>

<style scoped>
.code-editor-wrapper {
  position: relative;
  width: 100%;
  height: v-bind(height);
  border: 1px solid var(--border, #3a3a3a);
  border-radius: 4px;
  overflow: hidden;
  background: var(--bg-secondary, #2d2d2d);
}

.code-editor-textarea {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  padding: 12px;
  margin: 0;
  border: none;
  outline: none;
  resize: none;
  background: transparent;
  color: #d4d4d4;
  font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
  font-size: 14px;
  line-height: 1.5;
  tab-size: 2;
  white-space: pre;
  overflow-wrap: normal;
  overflow-x: auto;
  overflow-y: auto;
}

.code-editor-textarea::placeholder {
  color: #6a6a6a;
}

.code-editor-textarea::-webkit-scrollbar {
  width: 12px;
  height: 12px;
}

.code-editor-textarea::-webkit-scrollbar-track {
  background: var(--bg-secondary, #2d2d2d);
}

.code-editor-textarea::-webkit-scrollbar-thumb {
  background: #424242;
  border-radius: 6px;
}

.code-editor-textarea::-webkit-scrollbar-thumb:hover {
  background: #4e4e4e;
}
</style>
