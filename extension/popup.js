import { listVMs, startVM, stopVM, rebootVM, getConfig } from './api.js';

const $ = (s) => document.querySelector(s);
const list = $('#list');
const err = $('#err');
const status = $('#status');

let cache = [];

async function refresh() {
  err.textContent = '';
  status.textContent = 'Đang tải…';
  try {
    cache = await listVMs($('#state').value || undefined);
    render();
    status.textContent = `${cache.length} VM`;
  } catch (e) {
    status.textContent = '';
    err.textContent = e.message;
  }
}

function render() {
  const q = $('#search').value.toLowerCase();
  const items = cache.filter(v => !q || (v.name || '').toLowerCase().includes(q) || (v.displayname || '').toLowerCase().includes(q));
  list.innerHTML = '';
  for (const vm of items) {
    const li = document.createElement('li');
    li.innerHTML = `
      <span class="name" title="${vm.name}">${vm.displayname || vm.name}</span>
      <span class="badge ${vm.state}">${vm.state}</span>
      <button data-act="start" ${vm.state === 'Running' ? 'disabled' : ''}>Start</button>
      <button data-act="stop"  ${vm.state !== 'Running' ? 'disabled' : ''}>Stop</button>
      <button data-act="reboot" ${vm.state !== 'Running' ? 'disabled' : ''}>Reboot</button>`;
    li.querySelectorAll('button').forEach(b => b.addEventListener('click', async () => {
      try {
        b.disabled = true;
        const fn = { start: startVM, stop: stopVM, reboot: rebootVM }[b.dataset.act];
        await fn(vm.id);
        setTimeout(refresh, 1500);
      } catch (e) { err.textContent = e.message; b.disabled = false; }
    }));
    list.appendChild(li);
  }
}

document.querySelectorAll('.actions button').forEach(btn => btn.addEventListener('click', async () => {
  switch (btn.dataset.cmd) {
    case 'refresh': return refresh();
    case 'options': return chrome.runtime.openOptionsPage();
    case 'catalog': {
      const cfg = await getConfig();
      if (cfg.url) chrome.tabs.create({ url: cfg.url + '/client/#/servicecatalog' });
      return;
    }
    case 'copyEndpoint': {
      const cfg = await getConfig();
      if (cfg.url) {
        await navigator.clipboard.writeText(cfg.url + '/client/api');
        status.textContent = 'Đã copy API URL';
        setTimeout(() => status.textContent = `${cache.length} VM`, 1500);
      }
      return;
    }
  }
}));

$('#state').addEventListener('change', refresh);
$('#search').addEventListener('input', render);

refresh();
