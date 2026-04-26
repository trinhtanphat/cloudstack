# VNSO CloudStack — Mobile

Capacitor wrapper that loads the VNSO CloudStack management UI on Android and iOS.

## Layout
- `capacitor.config.ts` — appId `vn.vnso.cloudstack`, allow-navigation list.
- `scripts/build-web.js` — generates `www/index.html` shell (login + redirect).
- `package.json` — `npm run build:web && npm run sync && npm run open:android|ios`.

## Build
```bash
cd mobile
npm install
VNSO_MGMT_URL=https://manage.example.vn npm run build:web
npx cap add android   # first time
npx cap add ios       # first time, on macOS
npm run sync
npm run open:android  # opens Android Studio
npm run open:ios      # opens Xcode (macOS only)
```

## What ships in the binary
A thin login screen (URL + API key + secret) which stores credentials in
Capacitor Preferences (Keychain on iOS, EncryptedSharedPreferences on Android),
then redirects the WebView to `${VNSO_MGMT_URL}/client`.

## Push notifications (optional)
Configure Firebase (Android) / APNs (iOS) and the `@capacitor/push-notifications`
plugin will surface alerts from the VNSO Prometheus alertmanager bridge.

## Notes
- This is a wrapper, not a re-implementation. The full Vue UI runs server-side
  and is rendered inside the native WebView for parity with desktop UX.
- For pure web (PWA) usage, host `www/` behind any static server — service
  worker registration is left to the embedding management UI.
