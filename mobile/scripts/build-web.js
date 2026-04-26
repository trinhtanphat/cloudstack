#!/usr/bin/env node
/**
 * Build a thin web shell at ./www that:
 *  1. Loads a small login screen to capture management URL + API key.
 *  2. Stores credentials via Capacitor Preferences.
 *  3. Redirects to the management UI inside the WebView.
 *
 * If VNSO_MGMT_URL is set at build time, that URL is hard-coded as default.
 */
const fs = require('fs');
const path = require('path');

const outDir = path.join(__dirname, '..', 'www');
fs.mkdirSync(outDir, { recursive: true });

const defaultUrl = process.env.VNSO_MGMT_URL || '';

const html = `<!doctype html>
<html lang="vi">
<head>
<meta charset="utf-8" />
<meta name="viewport" content="width=device-width, initial-scale=1, viewport-fit=cover" />
<title>VNSO CloudStack</title>
<style>
  :root {
    color-scheme: light dark;
    --bg: #0b1220; --card: #111a2c; --text: #e6edf7;
    --muted: #a8b3c7; --border: #243047; --accent: #2dd4bf;
  }
  @media (prefers-color-scheme: light) {
    :root { --bg:#fff; --card:#f5f7fa; --text:#0f172a; --muted:#475569; --border:#d8dee6; --accent:#0f766e; }
  }
  body { margin: 0; background: var(--bg); color: var(--text); font: 16px/1.5 system-ui, sans-serif; }
  main { max-width: 460px; margin: 8vh auto; padding: 24px; background: var(--card); border: 1px solid var(--border); border-radius: 12px; }
  h1 { margin-top: 0; font-size: 1.4rem; }
  label { display:block; margin-top: 14px; font-size: .9rem; color: var(--muted); }
  input { width: 100%; padding: 10px 12px; margin-top: 4px; border: 1px solid var(--border); border-radius: 8px; background: var(--bg); color: var(--text); }
  button { margin-top: 18px; width: 100%; padding: 12px; border: none; border-radius: 8px; background: var(--accent); color: #fff; font-weight: 600; }
  .err { color: #fca5a5; font-size: .9rem; margin-top: 10px; }
  .muted { color: var(--muted); font-size: .85rem; margin-top: 14px; }
</style>
</head>
<body>
<main>
  <h1>VNSO CloudStack</h1>
  <p class="muted">Đăng nhập vào management server. URL được lưu cục bộ trên thiết bị.</p>
  <label>Management URL
    <input id="url" type="url" placeholder="https://manage.example.vn" value="${defaultUrl}" />
  </label>
  <label>API key
    <input id="apikey" type="text" autocomplete="off" />
  </label>
  <label>Secret key
    <input id="secret" type="password" autocomplete="off" />
  </label>
  <button id="go" type="button">Mở Console</button>
  <div id="err" class="err"></div>
  <p class="muted">Bản 1.0 — wrapper Capacitor cho VNSO CloudStack UI.</p>
</main>
<script type="module">
  // Capacitor preferences are optional (fallback to localStorage on web).
  let Preferences;
  try { ({ Preferences } = await import('@capacitor/preferences')); } catch(e) {}
  const get = async (k) => Preferences ? (await Preferences.get({ key: k })).value : localStorage.getItem(k);
  const set = async (k, v) => Preferences ? Preferences.set({ key: k, value: v }) : localStorage.setItem(k, v);

  const $ = (id) => document.getElementById(id);
  $('url').value     = (await get('vnso.url'))    || $('url').value;
  $('apikey').value  = (await get('vnso.apikey')) || '';
  $('secret').value  = (await get('vnso.secret')) || '';

  $('go').addEventListener('click', async () => {
    const url = $('url').value.trim().replace(/\\/$/, '');
    if (!/^https?:\\/\\//.test(url)) { $('err').textContent = 'URL phải bắt đầu bằng https://'; return; }
    await set('vnso.url', url);
    if ($('apikey').value) await set('vnso.apikey', $('apikey').value);
    if ($('secret').value) await set('vnso.secret', $('secret').value);
    location.href = url + '/client';
  });
</script>
</body>
</html>`;

fs.writeFileSync(path.join(outDir, 'index.html'), html);
console.log('Built mobile shell ->', path.join(outDir, 'index.html'));
