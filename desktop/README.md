# VNSO CloudStack — Desktop (Electron)

Cross-platform desktop wrapper around the VNSO CloudStack management UI.

## Run from source
```bash
cd desktop
npm install
npm start
```

## Package
```bash
npm run build:linux   # AppImage + deb
npm run build:mac     # dmg (on macOS only)
npm run build:win     # nsis (on Windows only)
```

## Security defaults
- `contextIsolation: true`, `nodeIntegration: false`, `sandbox: true`.
- Preload exposes only `window.vnso.{getUrl,setUrl}` — no Node APIs.
- External links and any cross-origin navigation are forwarded to the OS browser.
- A baseline Content-Security-Policy is injected on responses missing one.

## Files
- `src/main.js` — main process (window, menu, hardening).
- `src/preload.js` — narrow IPC surface.
- `assets/setup.html` — first-run management URL setup screen.
- `package.json` — electron-builder config (Linux/macOS/Windows targets).
