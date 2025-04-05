// 这个文件用于修复 @popperjs/core 与 element-plus 的兼容性问题
import * as Popper from '@popperjs/core'

// 为 element-plus 提供必要的导出
export const createPopper = Popper.createPopper
export const placements = {
  top: 'top',
  bottom: 'bottom',
  right: 'right',
  left: 'left',
  'top-start': 'top-start',
  'top-end': 'top-end',
  'bottom-start': 'bottom-start',
  'bottom-end': 'bottom-end',
  'right-start': 'right-start',
  'right-end': 'right-end',
  'left-start': 'left-start',
  'left-end': 'left-end'
}

// 导出所有 Popper 功能
export default Popper