declare namespace WechatMiniprogram {
  interface TouchEvent {
    currentTarget: {
      dataset: Record<string, any>;
    };
    detail: any;
  }

  interface CustomEvent<T = any> {
    detail: T;
    currentTarget: {
      dataset: Record<string, any>;
    };
  }

  interface MapMarker {
    id: number;
    latitude: number;
    longitude: number;
    width?: number;
    height?: number;
    alpha?: number;
    iconPath?: string;
    label?: {
      content?: string;
      color?: string;
      fontSize?: number;
      bgColor?: string;
      borderRadius?: number;
      padding?: number;
      anchorX?: number;
      anchorY?: number;
    };
    callout?: {
      content?: string;
      color?: string;
      bgColor?: string;
      borderColor?: string;
      borderWidth?: number;
      borderRadius?: number;
      padding?: number;
      display?: string;
    };
  }

  interface CanvasGradient {
    addColorStop: (stop: number, color: string) => void;
  }

  interface CanvasContext {
    clearRect: (x: number, y: number, width: number, height: number) => void;
    beginPath: () => void;
    moveTo: (x: number, y: number) => void;
    lineTo: (x: number, y: number) => void;
    closePath: () => void;
    stroke: () => void;
    fill: () => void;
    arc: (x: number, y: number, radius: number, startAngle: number, endAngle: number) => void;
    setStrokeStyle: (color: string) => void;
    setFillStyle: (color: string | CanvasGradient) => void;
    setLineWidth: (width: number) => void;
    setLineCap: (lineCap: 'butt' | 'round' | 'square') => void;
    setLineJoin: (lineJoin: 'bevel' | 'round' | 'miter') => void;
    createLinearGradient: (x0: number, y0: number, x1: number, y1: number) => CanvasGradient;
    draw: (reserve?: boolean, callback?: () => void) => void;
  }
}

declare interface IAppOption {
  globalData: {
    roleName: string;
    villageName: string;
  };
  onLaunch?: () => void;
}

declare function App<T>(options: T): void;
declare function Page<T>(options: T): void;
declare function Component<T>(options: T): void;
declare const console: { info: (...args: any[]) => void; log: (...args: any[]) => void; error: (...args: any[]) => void };
declare function setTimeout(handler: (...args: any[]) => void, timeout?: number): number;
declare function clearTimeout(handle?: number): void;

declare const wx: {
  request: (options: {
    url: string;
    method?: 'GET' | 'POST' | 'PUT' | 'DELETE';
    data?: any;
    timeout?: number;
    header?: Record<string, string>;
    success?: (result: { statusCode: number; data: any }) => void;
    fail?: (error: { errMsg?: string }) => void;
  }) => void;
  getSystemInfoSync: () => { windowWidth: number; windowHeight: number };
  createCanvasContext: (canvasId: string, component?: unknown) => WechatMiniprogram.CanvasContext;
  navigateTo: (options: { url: string }) => void;
  switchTab: (options: { url: string }) => void;
  showToast: (options: { title: string; icon?: 'none' | 'success' | 'error' | 'loading' }) => void;
};
