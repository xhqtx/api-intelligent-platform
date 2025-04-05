/**
 * ResizeObserver 错误修复工具
 * 
 * 此文件提供了处理 ResizeObserver 相关错误的工具函数，
 * 用于解决 Element Plus 和其他组件库中常见的 ResizeObserver 循环错误问题。
 */

/**
 * 初始化 ResizeObserver 错误处理
 * 这将覆盖默认的错误处理行为，忽略 ResizeObserver 循环错误
 */
export function setupResizeObserverErrorHandling(): void {
  // 1. 处理全局错误
  const originalWindowError = window.onerror;
  window.onerror = (message, source, lineno, colno, error) => {
    if (message && typeof message === 'string' && message.includes('ResizeObserver loop')) {
      console.debug('Ignored ResizeObserver error:', message);
      return true; // 阻止错误继续传播
    }
    return originalWindowError ? originalWindowError(message, source, lineno, colno, error) : false;
  };

  // 2. 处理未捕获的 Promise rejection
  window.addEventListener('unhandledrejection', (event) => {
    if (event.reason && event.reason.message && event.reason.message.includes('ResizeObserver loop')) {
      event.preventDefault();
      event.stopPropagation();
      console.debug('Ignored ResizeObserver promise rejection:', event.reason.message);
      return true;
    }
  }, true);

  // 3. 覆盖控制台错误输出
  const originalConsoleError = console.error;
  console.error = (...args) => {
    if (args.length > 0 && 
        typeof args[0] === 'string' && 
        args[0].includes('ResizeObserver loop')) {
      console.debug('Suppressed ResizeObserver console error:', args[0]);
      return;
    }
    originalConsoleError.apply(console, args);
  };
}

/**
 * 创建安全的 ResizeObserver
 * 包装原始的 ResizeObserver 以捕获和处理循环错误
 */
export function createSafeResizeObserver(callback: ResizeObserverCallback): ResizeObserver {
  try {
    const observer = new ResizeObserver((...args) => {
      try {
        callback(...args);
      } catch (error) {
        if (error instanceof Error && error.message.includes('ResizeObserver loop')) {
          console.debug('Caught ResizeObserver callback error:', error.message);
          return;
        }
        throw error;
      }
    });
    return observer;
  } catch (error) {
    console.warn('Failed to create ResizeObserver:', error);
    // 返回一个空的 ResizeObserver 模拟对象
    return {
      observe: () => {
        /* 空实现：用作 ResizeObserver 降级时的后备方案 */
      },
      unobserve: () => {
        /* 空实现：用作 ResizeObserver 降级时的后备方案 */
      },
      disconnect: () => {
        /* 空实现：用作 ResizeObserver 降级时的后备方案 */
      }
    };
  }
}

export default {
  setupResizeObserverErrorHandling,
  createSafeResizeObserver
};