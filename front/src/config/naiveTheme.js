// Naive UI Tokyo Night 主题配置
import { darkTheme } from 'naive-ui'

// Tokyo Night 色彩变量
const tokyoNightColors = {
  primaryColor: '#7aa2f7',
  primaryColorHover: '#7dcfff',
  primaryColorPressed: '#5a7fbf',
  primaryColorSuppl: '#7dcfff',
  
  infoColor: '#7aa2f7',
  infoColorHover: '#7dcfff',
  infoColorPressed: '#5a7fbf',
  infoColorSuppl: '#7dcfff',
  
  successColor: '#9ece6a',
  successColorHover: '#b3d98c',
  successColorPressed: '#7ea855',
  successColorSuppl: '#b3d98c',
  
  warningColor: '#ff9e64',
  warningColorHover: '#ffb380',
  warningColorPressed: '#e08950',
  warningColorSuppl: '#ffb380',
  
  errorColor: '#f7768e',
  errorColorHover: '#f99aab',
  errorColorPressed: '#d85e76',
  errorColorSuppl: '#f99aab',
}

export const naiveThemeOverrides = {
  common: {
    // 主色调
    ...tokyoNightColors,
    
    // 背景色 - 使用浅蓝色
    bodyColor: '#1a1b26',
    cardColor: '#24283b',
    modalColor: '#24283b', // 浅蓝色背景
    popoverColor: '#24283b', // 浅蓝色背景
    tableColor: '#24283b',
    
    // 输入框背景 - 浅蓝色
    inputColor: '#24283b', // 浅蓝色背景
    inputColorDisabled: '#1f2335',
    
    // 边框和分割线
    borderColor: '#3b4261',
    dividerColor: '#3b4261',
    
    // 文字颜色
    textColorBase: '#e0e6ff',
    textColor1: '#e0e6ff',
    textColor2: '#c0caf5',
    textColor3: '#6b7694',
    textColorDisabled: '#565f89',
    placeholderColor: '#6b7694',
    placeholderColorDisabled: '#414868',
    
    // 圆角
    borderRadius: '8px',
    borderRadiusSmall: '6px',
    
    // 阴影
    boxShadow1: '0 2px 8px rgba(0,0,0,0.3)',
    boxShadow2: '0 8px 24px rgba(0,0,0,0.4)',
    boxShadow3: '0 16px 48px rgba(0,0,0,0.5)',
    
    // 字体
    fontSize: '15px',
    fontSizeMini: '13px',
    fontSizeTiny: '13px',
    fontSizeSmall: '14px',
    fontSizeMedium: '15px',
    fontSizeLarge: '16px',
    fontSizeHuge: '18px',
    
    // 高度
    heightTiny: '28px',
    heightSmall: '32px',
    heightMedium: '36px',
    heightLarge: '40px',
    heightHuge: '44px',
  },
  
  // Input 组件特殊配置
  Input: {
    color: '#24283b', // 浅蓝色背景
    colorDisabled: '#1f2335',
    colorFocus: '#24283b',
    textColor: '#e0e6ff',
    border: '1px solid #3b4261',
    borderHover: '1px solid #7aa2f7',
    borderFocus: '1px solid #7dcfff',
    boxShadowFocus: '0 0 0 3px rgba(125, 207, 255, 0.1)',
  },
  
  // Select 组件特殊配置
  Select: {
    peers: {
      InternalSelection: {
        color: '#24283b', // 浅蓝色背景
        colorActive: '#24283b',
        border: '1px solid #3b4261',
        borderHover: '1px solid #7aa2f7',
        borderActive: '1px solid #7dcfff',
        boxShadowActive: '0 0 0 3px rgba(125, 207, 255, 0.1)',
      }
    }
  },
  
  // DatePicker 组件特殊配置
  DatePicker: {
    peers: {
      Input: {
        color: '#24283b', // 浅蓝色背景
      }
    },
    panelColor: '#24283b', // 浅蓝色背景
  },
  
  // Dropdown 组件特殊配置
  Dropdown: {
    color: '#24283b', // 浅蓝色背景
    optionTextColor: '#e0e6ff',
    optionColorHover: 'rgba(122, 162, 247, 0.2)',
    optionColorActive: 'rgba(122, 162, 247, 0.25)',
  },
  
  // Popover 组件特殊配置
  Popover: {
    color: '#24283b', // 浅蓝色背景
    textColor: '#e0e6ff',
  },
  
  // Modal/Dialog 组件特殊配置
  Modal: {
    color: '#24283b', // 浅蓝色背景
    textColor: '#e0e6ff',
    titleTextColor: '#e0e6ff',
  },
  
  Dialog: {
    color: '#24283b', // 浅蓝色背景
    textColor: '#e0e6ff',
    titleTextColor: '#e0e6ff',
  },
  
  // Message 组件特殊配置
  Message: {
    color: '#24283b', // 浅蓝色背景
    textColor: '#e0e6ff',
  },
  
  // Notification 组件特殊配置
  Notification: {
    color: '#24283b', // 浅蓝色背景
    textColor: '#e0e6ff',
    titleTextColor: '#e0e6ff',
  },
  
  // Tooltip 组件特殊配置
  Tooltip: {
    color: '#24283b', // 浅蓝色背景
    textColor: '#e0e6ff',
  },
  
  // Button 组件
  Button: {
    colorPrimary: 'linear-gradient(135deg, #7aa2f7, #7dcfff)',
    textColorPrimary: '#1a1b26',
    colorHoverPrimary: 'linear-gradient(135deg, #7dcfff, #7aa2f7)',
    borderRadiusMedium: '6px',
  },
  
  // Card 组件
  Card: {
    color: '#24283b',
    colorModal: '#24283b',
    colorTarget: '#24283b',
    borderColor: '#3b4261',
    titleTextColor: '#e0e6ff',
  },
  
  // Table 组件
  DataTable: {
    thColor: '#1f2335',
    tdColor: '#24283b',
    tdColorHover: '#2f3549',
    borderColor: '#3b4261',
    thTextColor: '#c0caf5',
    tdTextColor: '#e0e6ff',
  },
  
  // Tag 组件
  Tag: {
    color: '#1f2335',
    textColor: '#e0e6ff',
    border: '1px solid #3b4261',
  },
}

export { darkTheme }
