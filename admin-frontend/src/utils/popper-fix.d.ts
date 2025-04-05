// 为 popper-fix.js 提供类型声明
import * as PopperCore from '@popperjs/core';

export const createPopper: typeof PopperCore.createPopper;
export const placements: {
  top: string;
  bottom: string;
  right: string;
  left: string;
  'top-start': string;
  'top-end': string;
  'bottom-start': string;
  'bottom-end': string;
  'right-start': string;
  'right-end': string;
  'left-start': string;
  'left-end': string;
};

// 导出所有 Popper 功能
declare const Popper: typeof PopperCore;
export default Popper;