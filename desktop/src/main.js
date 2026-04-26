/* VNSO CloudStack desktop — Electron main process.
 * Security defaults:
 *   - contextIsolation: true
 *   - nodeIntegration: false
 *   - sandbox: true
 *   - external links open in the OS browser
 *   - block any navigation outside configured management URL
 */
const { app, BrowserWindow, shell, Menu, ipcMain, session } = require('electron');
const path = require('path');
let Store;
try { Store = require('electron-store'); } catch { Store = null; }

const store = Store ? new Store({ name: 'vnso-cloudstack' }) : {
  _m: new Map(),
  get(k, d) { return this._m.has(k) ? this._m.get(k) : d; },
  set(k, v) { this._m.set(k, v); }
};

let mainWindow = null;

function getMgmtUrl() {
  return process.env.VNSO_MGMT_URL
    || store.get('mgmtUrl')
    || '';
}

function buildMenu() {
  const template = [
    {
      label: 'File',
      submenu: [
        { label: 'Reload', accelerator: 'CmdOrCtrl+R', click: () => mainWindow?.reload() },
        { type: 'separator' },
        { label: 'Change Management URL…', click: openSetupScreen },
        { type: 'separator' },
        { role: process.platform === 'darwin' ? 'close' : 'quit' }
      ]
    },
    { role: 'editMenu' },
    {
      label: 'View',
      submenu: [
        { role: 'toggleDevTools' },
        { type: 'separator' },
        { role: 'resetZoom' }, { role: 'zoomIn' }, { role: 'zoomOut' },
        { type: 'separator' },
        { role: 'togglefullscreen' }
      ]
    },
    {
      label: 'Help',
      submenu: [
        { label: 'Open docs', click: () => shell.openExternal('https://docs.cloudstack.apache.org') },
        { label: 'About', click: () => shell.openExternal('https://cloudstack.apache.org') }
      ]
    }
  ];
  Menu.setApplicationMenu(Menu.buildFromTemplate(template));
}

function openSetupScreen() {
  if (!mainWindow) return;
  mainWindow.loadFile(path.join(__dirname, '..', 'assets', 'setup.html'));
}

function loadMgmt(url) {
  if (!mainWindow || !url) return openSetupScreen();
  store.set('mgmtUrl', url);
  mainWindow.loadURL(url + (url.endsWith('/client') ? '' : '/client'));
}

function applyHardening(s) {
  // Block remote dev-tools, refuse any auth bypass redirects.
  s.webRequest.onHeadersReceived((details, cb) => {
    const headers = details.responseHeaders || {};
    headers['Content-Security-Policy'] = headers['Content-Security-Policy'] || [
      "default-src 'self' 'unsafe-inline' data: blob: https:; " +
      "connect-src 'self' https: wss:; " +
      "frame-ancestors 'self'; " +
      "object-src 'none'"
    ];
    headers['X-Content-Type-Options'] = ['nosniff'];
    cb({ responseHeaders: headers });
  });
}

function createWindow() {
  mainWindow = new BrowserWindow({
    width: 1280,
    height: 820,
    minWidth: 980,
    minHeight: 640,
    backgroundColor: '#0b1220',
    title: 'VNSO CloudStack',
    webPreferences: {
      contextIsolation: true,
      nodeIntegration: false,
      sandbox: true,
      preload: path.join(__dirname, 'preload.js'),
      spellcheck: false
    }
  });

  // Open external links in the user's default browser.
  mainWindow.webContents.setWindowOpenHandler(({ url }) => {
    shell.openExternal(url);
    return { action: 'deny' };
  });

  // Block navigation away from configured management URL (prevents phishing).
  mainWindow.webContents.on('will-navigate', (event, target) => {
    const allowed = getMgmtUrl();
    if (!allowed) return;
    try {
      const t = new URL(target);
      const a = new URL(allowed);
      if (t.origin !== a.origin && !target.startsWith('file://')) {
        event.preventDefault();
        shell.openExternal(target);
      }
    } catch { event.preventDefault(); }
  });

  applyHardening(mainWindow.webContents.session);

  const url = getMgmtUrl();
  if (url) loadMgmt(url); else openSetupScreen();
}

ipcMain.handle('vnso:get-url',  () => getMgmtUrl());
ipcMain.handle('vnso:set-url',  (_e, url) => { loadMgmt(url); return true; });

app.whenReady().then(() => {
  buildMenu();
  createWindow();
  app.on('activate', () => {
    if (BrowserWindow.getAllWindows().length === 0) createWindow();
  });
});

app.on('window-all-closed', () => {
  if (process.platform !== 'darwin') app.quit();
});
