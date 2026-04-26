import { CapacitorConfig } from '@capacitor/cli';

const config: CapacitorConfig = {
  appId: 'vn.vnso.cloudstack',
  appName: 'VNSO CloudStack',
  webDir: 'www',
  bundledWebRuntime: false,
  server: {
    // Default: bundle a thin shell that loads the configured management UI.
    // Override at build time by setting VNSO_MGMT_URL in the environment;
    // see scripts/build-web.js. When unset, opens the in-app login screen.
    androidScheme: 'https',
    iosScheme: 'https',
    cleartext: false,
    allowNavigation: ['*.vnso.vn', 'localhost', '127.0.0.1']
  },
  android: {
    backgroundColor: '#0b1220'
  },
  ios: {
    contentInset: 'always',
    backgroundColor: '#0b1220'
  },
  plugins: {
    PushNotifications: {
      presentationOptions: ['badge', 'sound', 'alert']
    }
  }
};

export default config;
