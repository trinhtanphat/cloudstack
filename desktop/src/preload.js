/* Preload — narrow IPC surface; no Node APIs leaked to renderer. */
const { contextBridge, ipcRenderer } = require('electron');

contextBridge.exposeInMainWorld('vnso', {
  getUrl: () => ipcRenderer.invoke('vnso:get-url'),
  setUrl: (url) => ipcRenderer.invoke('vnso:set-url', url)
});
