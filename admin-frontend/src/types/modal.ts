/**
 * Modal 接口定义
 * 用于统一处理不同类型的模态框
 */
export interface Modal {
  /**
   * 显示模态框
   * @param message 显示的消息内容
   * @param title 模态框标题
   * @returns 可能返回一个 Promise
   */
  show: (message: string, title: string) => any;
  
  /**
   * 隐藏/关闭模态框
   */
  hide: () => void;
}