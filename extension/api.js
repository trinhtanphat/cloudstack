/* VNSO CloudStack extension — shared API client (HMAC-SHA1 signing). */

export async function getConfig() {
  const c = await chrome.storage.local.get(['url', 'apikey', 'secret']);
  return { url: (c.url || '').replace(/\/$/, ''), apikey: c.apikey || '', secret: c.secret || '' };
}

export async function setConfig(cfg) {
  await chrome.storage.local.set(cfg);
}

/** CloudStack API signing: lower-case sorted query string, HMAC-SHA1, base64. */
async function sign(params, secret) {
  const enc = new TextEncoder();
  const sorted = Object.keys(params).sort()
    .map(k => `${k.toLowerCase()}=${encodeURIComponent(params[k]).toLowerCase().replace(/\+/g, '%20')}`)
    .join('&');
  const key = await crypto.subtle.importKey(
    'raw', enc.encode(secret), { name: 'HMAC', hash: 'SHA-1' }, false, ['sign']);
  const sig = await crypto.subtle.sign('HMAC', key, enc.encode(sorted));
  return btoa(String.fromCharCode(...new Uint8Array(sig)));
}

export async function call(command, extra = {}) {
  const { url, apikey, secret } = await getConfig();
  if (!url || !apikey || !secret) throw new Error('Chưa cấu hình URL/API key trong Options.');
  const params = { command, response: 'json', apikey, ...extra };
  params.signature = await sign(params, secret);
  const qs = new URLSearchParams(params).toString();
  const r = await fetch(`${url}/client/api?${qs}`, { method: 'GET', credentials: 'omit' });
  if (!r.ok) throw new Error(`HTTP ${r.status}`);
  const j = await r.json();
  const key = Object.keys(j).find(k => k.endsWith('response'));
  return key ? j[key] : j;
}

export async function listVMs(state) {
  const r = await call('listVirtualMachines', state ? { state } : {});
  return r.virtualmachine || [];
}
export const startVM = (id) => call('startVirtualMachine', { id });
export const stopVM  = (id) => call('stopVirtualMachine',  { id });
export const rebootVM = (id) => call('rebootVirtualMachine', { id });
