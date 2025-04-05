import * as PopperCore from '@popperjs/core';

declare global {
  interface Window {
    __VUE_PROD_HYDRATION_MISMATCH_DETAILS__?: boolean;
    Popper: typeof PopperCore;
    createPopper: typeof PopperCore.createPopper;
    placements: {
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
  }
}

export {};