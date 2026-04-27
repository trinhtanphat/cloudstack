/* ============================================================
   ProxmoxAI Docs · Theme Picker
   Auto-mounts a button next to the existing .theme-toggle in
   the docs nav. Persists choice in localStorage('docs.theme').
   Supported themes: see THEMES list. The plain Light/Dark
   button stays as a quick toggle.
   ============================================================ */
(function () {
  'use strict';

  // Reuse the same key the inline pre-paint script reads on every docs
  // page (`localStorage.getItem("theme")`), so picker selections survive
  // reload without a light/dark flash.
  var STORAGE_KEY = 'theme';
  var THEMES = [
    { id: 'light',             name: 'Light',             group: 'Cơ bản',  bg: '#f1f5f9', accent: '#e6500a' },
    { id: 'dark',              name: 'Dark',              group: 'Cơ bản',  bg: '#0b1120', accent: '#e6500a' },
    { id: 'anthropic',         name: 'Anthropic',         group: 'AI',      bg: '#1f1e1d', accent: '#d97706' },
    { id: 'anthropic-light',   name: 'Anthropic Light',   group: 'AI',      bg: '#faf7f2', accent: '#c2410c' },
    { id: 'cursor',            name: 'Cursor',            group: 'AI',      bg: '#0a0d12', accent: '#58a6ff' },
    { id: 'perplexity',        name: 'Perplexity',        group: 'AI',      bg: '#191a1a', accent: '#20b8cd' },
    { id: 'dracula',           name: 'Dracula',           group: 'Dev',     bg: '#282a36', accent: '#ff79c6' },
    { id: 'nord',              name: 'Nord',              group: 'Dev',     bg: '#2e3440', accent: '#88c0d0' },
    { id: 'tokyo-night',       name: 'Tokyo Night',       group: 'Dev',     bg: '#1a1b26', accent: '#7aa2f7' },
    { id: 'catppuccin',        name: 'Catppuccin',        group: 'Dev',     bg: '#1e1e2e', accent: '#f5c2e7' },
    { id: 'gruvbox',           name: 'Gruvbox',           group: 'Dev',     bg: '#282828', accent: '#fe8019' },
    { id: 'rosepine',          name: 'Rosé Pine',         group: 'Dev',     bg: '#191724', accent: '#eb6f92' },
    { id: 'github-light',      name: 'GitHub Light',      group: 'Brand',   bg: '#ffffff', accent: '#0969da' },
    { id: 'github-dim',        name: 'GitHub Dim',        group: 'Brand',   bg: '#22272e', accent: '#539bf5' },
    { id: 'vercel-light',      name: 'Vercel Light',      group: 'Brand',   bg: '#ffffff', accent: '#000000' },
    { id: 'stripe',            name: 'Stripe',            group: 'Brand',   bg: '#0a0e27', accent: '#635bff' },
    { id: 'supabase',          name: 'Supabase',          group: 'Brand',   bg: '#1c1c1c', accent: '#3ecf8e' },
    { id: 'discord',           name: 'Discord',           group: 'Brand',   bg: '#36393f', accent: '#5865f2' },
    { id: 'solarized',         name: 'Solarized',         group: 'Classic', bg: '#002b36', accent: '#cb4b16' },
    { id: 'monokai',           name: 'Monokai',           group: 'Classic', bg: '#272822', accent: '#f92672' },
    { id: 'sepia',             name: 'Sepia',             group: 'Classic', bg: '#f5ecd9', accent: '#8b4513' },
    { id: 'forest',            name: 'Forest',            group: 'Nature',  bg: '#0d1f15', accent: '#4ade80' },
    { id: 'ocean-deep',        name: 'Ocean Deep',        group: 'Nature',  bg: '#001830', accent: '#00b4d8' },
    { id: 'sunset',            name: 'Sunset',            group: 'Nature',  bg: '#1a0e1a', accent: '#ff6b35' },
    { id: 'cyberpunk',         name: 'Cyberpunk',         group: 'Retro',   bg: '#0a0014', accent: '#ff006e' },
    { id: 'matrix',            name: 'Matrix',            group: 'Retro',   bg: '#000000', accent: '#00ff41' },
    { id: 'vaporwave',         name: 'Vaporwave',         group: 'Retro',   bg: '#1a0033', accent: '#ff71ce' },
    { id: 'synthwave',         name: 'Synthwave',         group: 'Retro',   bg: '#1a1033', accent: '#ff2a6d' },
    { id: 'high-contrast-dark',name: 'High Contrast',     group: 'A11y',    bg: '#000000', accent: '#ffff00' }
  ];

  function getStored() {
    try { return localStorage.getItem(STORAGE_KEY); } catch (e) { return null; }
  }
  function setStored(v) { try { localStorage.setItem(STORAGE_KEY, v); } catch (e) {} }

  function applyTheme(themeId) {
    // The pre-paint inline script on every docs page sets data-theme to
    // "light" or "dark" based on saved value. For the legacy values we
    // keep that behaviour; for fancy themes (anthropic, dracula, etc.)
    // we just set data-theme to the theme id — the CSS in themes.css
    // matches html[data-theme="..."].
    document.documentElement.setAttribute('data-theme', themeId);
    setStored(themeId);
    window.dispatchEvent(new CustomEvent('docs-theme-changed', { detail: { theme: themeId } }));
  }

  function buildPicker() {
    if (document.getElementById('docs-theme-picker-btn')) return;
    var nav = document.querySelector('.nav .container');
    if (!nav) return;
    var existingToggle = nav.querySelector('.theme-toggle');

    var btn = document.createElement('button');
    btn.id = 'docs-theme-picker-btn';
    btn.className = 'theme-picker-btn';
    btn.setAttribute('aria-label', 'Đổi theme');
    btn.setAttribute('title', 'Đổi theme');
    btn.innerHTML = '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="13.5" cy="6.5" r=".5"/><circle cx="17.5" cy="10.5" r=".5"/><circle cx="8.5" cy="7.5" r=".5"/><circle cx="6.5" cy="12.5" r=".5"/><path d="M12 2C6.5 2 2 6.5 2 12s4.5 10 10 10c.926 0 1.648-.746 1.648-1.688 0-.437-.18-.835-.437-1.125-.29-.289-.438-.652-.438-1.125a1.64 1.64 0 0 1 1.668-1.668h1.996c3.051 0 5.555-2.503 5.555-5.554C21.965 6.012 17.461 2 12 2z"/></svg>';

    var menu = document.createElement('div');
    menu.id = 'docs-theme-picker-menu';
    menu.className = 'theme-picker-menu';
    menu.hidden = true;

    var groups = {};
    THEMES.forEach(function (t) { (groups[t.group] = groups[t.group] || []).push(t); });
    Object.keys(groups).forEach(function (g) {
      var head = document.createElement('div');
      head.className = 'theme-picker-group';
      head.textContent = g;
      menu.appendChild(head);
      var grid = document.createElement('div');
      grid.className = 'theme-picker-grid';
      groups[g].forEach(function (t) {
        var item = document.createElement('button');
        item.type = 'button';
        item.className = 'theme-picker-item';
        item.dataset.theme = t.id;
        item.title = t.name;
        item.innerHTML = '<span class="sw" style="background:linear-gradient(135deg,' + t.bg + ' 50%,' + t.accent + ' 50%);"></span>' +
                         '<span class="lbl">' + t.name + '</span>';
        item.addEventListener('click', function () {
          applyTheme(t.id);
          menu.hidden = true;
          markActive();
        });
        grid.appendChild(item);
      });
      menu.appendChild(grid);
    });

    function markActive() {
      var current = getStored() || 'light';
      menu.querySelectorAll('.theme-picker-item').forEach(function (it) {
        it.classList.toggle('active', it.dataset.theme === current);
      });
    }

    btn.addEventListener('click', function (e) {
      e.stopPropagation();
      menu.hidden = !menu.hidden;
      if (!menu.hidden) markActive();
    });
    document.addEventListener('click', function (e) {
      if (!menu.hidden && !menu.contains(e.target) && e.target !== btn) menu.hidden = true;
    });
    document.addEventListener('keydown', function (e) {
      if (e.key === 'Escape') menu.hidden = true;
    });

    if (existingToggle) existingToggle.parentNode.insertBefore(btn, existingToggle);
    else nav.appendChild(btn);
    document.body.appendChild(menu);
  }

  // ─── Inject component CSS once ────────────────────────────────────────
  function injectCss() {
    if (document.getElementById('docs-theme-picker-css')) return;
    var s = document.createElement('style');
    s.id = 'docs-theme-picker-css';
    s.textContent = [
      '.theme-picker-btn{background:transparent;border:1px solid var(--border,rgba(255,255,255,.12));',
      '  color:var(--text,#e2e8f0);width:38px;height:38px;border-radius:8px;cursor:pointer;',
      '  display:inline-flex;align-items:center;justify-content:center;margin-left:6px;transition:all .2s;}',
      '.theme-picker-btn:hover{border-color:var(--primary,#e6500a);color:var(--primary,#e6500a);}',
      '.theme-picker-menu{position:fixed;top:60px;right:16px;width:min(380px,calc(100vw - 32px));',
      '  max-height:calc(100vh - 80px);overflow-y:auto;background:var(--bg-card,#111827);',
      '  border:1px solid var(--border,rgba(255,255,255,.1));border-radius:12px;padding:14px;',
      '  z-index:9999;box-shadow:0 16px 48px rgba(0,0,0,.4);}',
      '.theme-picker-group{font-size:.7rem;font-weight:700;text-transform:uppercase;letter-spacing:.08em;',
      '  color:var(--text-muted,#94a3b8);margin:10px 4px 8px;}',
      '.theme-picker-group:first-child{margin-top:0;}',
      '.theme-picker-grid{display:grid;grid-template-columns:repeat(auto-fill,minmax(110px,1fr));gap:6px;}',
      '.theme-picker-item{display:flex;flex-direction:column;align-items:center;gap:6px;padding:8px 6px;',
      '  background:var(--bg-surface,#151d2e);border:1px solid var(--border,rgba(255,255,255,.08));',
      '  border-radius:8px;color:var(--text,#e2e8f0);cursor:pointer;transition:all .15s;font-size:.78rem;}',
      '.theme-picker-item:hover{border-color:var(--primary,#e6500a);transform:translateY(-1px);}',
      '.theme-picker-item.active{border-color:var(--primary,#e6500a);background:var(--bg-card-hover,#1a2332);}',
      '.theme-picker-item .sw{width:36px;height:36px;border-radius:50%;display:block;',
      '  border:2px solid var(--border-light,rgba(255,255,255,.1));}',
      '.theme-picker-item .lbl{display:block;text-align:center;line-height:1.2;}',
      '@media (max-width:540px){.theme-picker-menu{right:8px;left:8px;width:auto;}}'
    ].join('\n');
    document.head.appendChild(s);
  }

  // ─── Hook the existing Light/Dark toggle button so it cycles through
  //     the simple light/dark choice but keeps any picker theme intact
  //     when user explicitly chose one. ────────────────────────────────
  function rebindLightDarkToggle() {
    var t = document.querySelector('.theme-toggle');
    if (!t || t.dataset.docsBound) return;
    t.dataset.docsBound = '1';
    t.addEventListener('click', function (e) {
      e.stopImmediatePropagation();
      var cur = getStored() || 'light';
      // If user is on a fancy theme, light/dark toggle resets to the basic pair.
      var next = (cur === 'light') ? 'dark' : 'light';
      applyTheme(next);
    }, true);
  }

  function init() {
    injectCss();
    var saved = getStored();
    if (saved) applyTheme(saved);
    buildPicker();
    rebindLightDarkToggle();
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', init);
  } else {
    init();
  }
})();
