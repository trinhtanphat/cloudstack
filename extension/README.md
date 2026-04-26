# VNSO CloudStack Quick Actions — Browser Extension (MV3)

A Chrome/Edge MV3 extension for fast VM operations from the toolbar.

## Features
- Popup list of VMs with state badge (Running/Stopped/Error).
- Filter by state and search by name.
- Per-VM Start / Stop / Reboot buttons.
- One-click links: Service Catalog, copy `${url}/client/api` to clipboard.
- Background poll every 5 min — desktop notification when any VM enters Error.
- Options page stores URL + API key/secret in `chrome.storage.local`.

## Install (developer mode)
1. Open `chrome://extensions` (or `edge://extensions`).
2. Enable **Developer mode**.
3. **Load unpacked** → pick `cloudstack/extension/`.
4. Open the popup → **Cấu hình** → fill management URL + API key/secret.

## Files
- `manifest.json` — MV3, host permissions, CSP `script-src 'self'`.
- `api.js` — HMAC-SHA1 CloudStack request signing via `crypto.subtle`.
- `popup.html|css|js` — main UI.
- `options.html` — credential storage.
- `background.js` — periodic Error-state notifier.
- `icons/` — placeholder PNGs (replace with real branding before publishing).

## Notes
- Uses native `crypto.subtle.sign('HMAC', SHA-1, …)`; no external libs.
- `host_permissions` is `https://*/*` so the extension can hit any private
  management URL; tighten before publishing to a public store.
- Icons here are 1×1 placeholders — replace with real 16/48/128 PNGs.
