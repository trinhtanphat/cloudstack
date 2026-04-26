/* Service worker: optional periodic VM-state polling for desktop notifications. */
import { listVMs } from './api.js';

const ALARM = 'vnso-poll';
chrome.runtime.onInstalled.addListener(() => {
  chrome.alarms.create(ALARM, { periodInMinutes: 5 });
});

chrome.alarms?.onAlarm.addListener(async (a) => {
  if (a.name !== ALARM) return;
  try {
    const vms = await listVMs('Error');
    if (vms.length) {
      chrome.notifications?.create({
        type: 'basic',
        iconUrl: 'icons/icon128.png',
        title: 'VNSO CloudStack',
        message: `${vms.length} VM ở trạng thái Error`,
        priority: 1
      });
    }
  } catch (e) { /* not configured yet — silent */ }
});
