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

declare const wx: {
  navigateTo: (options: { url: string }) => void;
  switchTab: (options: { url: string }) => void;
  showToast: (options: { title: string; icon?: 'none' | 'success' | 'error' | 'loading' }) => void;
};
