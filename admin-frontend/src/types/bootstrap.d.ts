declare module 'bootstrap' {
  export class Modal {
    constructor(element: string | Element, options?: Record<string, unknown>);
    show(): void;
    hide(): void;
    toggle(): void;
    dispose(): void;
    static getInstance(element: string | Element): Modal | null;
  }

  export class Tooltip {
    constructor(element: string | Element, options?: Record<string, unknown>);
    show(): void;
    hide(): void;
    toggle(): void;
    dispose(): void;
    static getInstance(element: string | Element): Tooltip | null;
  }

  export class Popover {
    constructor(element: string | Element, options?: Record<string, unknown>);
    show(): void;
    hide(): void;
    toggle(): void;
    dispose(): void;
    static getInstance(element: string | Element): Popover | null;
  }
}