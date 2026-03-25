export const normalizeOptions = (rawOptions, fallbackLabels = true) => {
  if (!rawOptions) {
    return []
  }

  let parsed = rawOptions
  if (typeof parsed === 'string') {
    try {
      parsed = JSON.parse(parsed)
    } catch (error) {
      const segments = parsed
        .split(/\r?\n|;|\|/)
        .map(text => text.trim())
        .filter(Boolean)
      return segments.map((text, index) => ({
        key: String.fromCharCode(65 + index),
        value: text
      }))
    }
  }

  if (!Array.isArray(parsed)) {
    return []
  }

  return parsed.map((option, index) => {
    if (typeof option === 'string') {
      return {
        key: fallbackLabels ? String.fromCharCode(65 + index) : option,
        value: option
      }
    }
    return {
      key:
        option.key ||
        option.label ||
        option.option ||
        (fallbackLabels ? String.fromCharCode(65 + index) : ''),
      value: option.value || option.label || option.text || ''
    }
  })
}

export const formatOptionsText = (rawOptions) => {
  const list = normalizeOptions(rawOptions)
  if (!list.length) {
    return '—'
  }
  return list
    .map(item => `${item.key ? item.key + '. ' : ''}${item.value}`.trim())
    .join('; ')
}
