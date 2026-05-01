// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The ASF licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

import { createApp, h } from 'vue'
import { vueApp, vueProps } from './vue-app'
import router from './router'
import store from './store'
import { i18n, loadLanguageAsync } from './locales'

import bootstrap from './core/bootstrap'
import './core/lazy_use'
import extensions from './core/ext'
import './permission' // permission control
import './utils/filter' // global filter
import {
  pollJobPlugin,
  notifierPlugin,
  toLocaleDatePlugin,
  configUtilPlugin,
  apiMetaUtilPlugin,
  showIconPlugin,
  resourceTypePlugin,
  fileSizeUtilPlugin,
  genericUtilPlugin,
  localesPlugin,
  dialogUtilPlugin,
  cpuArchitectureUtilPlugin,
  imagesUtilPlugin,
  extensionsUtilPlugin,
  backupUtilPlugin
} from './utils/plugins'
import { VueAxios } from './utils/request'
import directives from './utils/directives'
import Cookies from 'js-cookie'
import { getAPI } from '@/api'
import { applyCustomGuiTheme } from './utils/guiTheme'

// VNSO floating theme picker — full swatch picker (served via public/theme-kit/theme.js)
;(function mountVNSOThemePicker () {
  const PREFS_KEY = 'proxmoxai_prefs'
  const LIGHT_VARIANTS = { anthropic: 'anthropic-light', v0: 'vercel-light', 'github-dim': 'github-light' }
  const GROUPS = [
    { label: 'AI Themes',    themes: ['midnight','cursor','anthropic','v0','perplexity','synthwave','aurora'] },
    { label: 'Dark',         themes: ['nebula','nord','dracula','tokyo-night','github-dim','monokai','catppuccin','rosepine'] },
    { label: 'Light',        themes: ['light','solarized','sakura','sepia','paper'] },
    { label: 'SaaS Premium', themes: ['linear','stripe','notion','figma','raycast','supabase','railway'] },
  ]
  const LABELS = { midnight:'Midnight',cursor:'Cursor IDE',anthropic:'Anthropic',v0:'Vercel v0',perplexity:'Perplexity',synthwave:'Synthwave',aurora:'Aurora',nebula:'Nebula',nord:'Nord',dracula:'Dracula','tokyo-night':'Tokyo Night','github-dim':'GitHub Dim',monokai:'Monokai',catppuccin:'Catppuccin',rosepine:'Rosé Pine',light:'Light',solarized:'Solarized',sakura:'Sakura',sepia:'Sepia',paper:'Paper',linear:'Linear',stripe:'Stripe',notion:'Notion',figma:'Figma',raycast:'Raycast',supabase:'Supabase',railway:'Railway' }

  function loadPrefs () { try { return JSON.parse(localStorage.getItem(PREFS_KEY) || '{}') } catch { return {} } }
  function savePrefs (p) { try { localStorage.setItem(PREFS_KEY, JSON.stringify(p)) } catch {} }
  function applyTheme (themeId) {
    let eff = themeId || 'midnight'
    const p = loadPrefs()
    if (eff === 'system') eff = (window.matchMedia && window.matchMedia('(prefers-color-scheme: light)').matches) ? 'light' : 'midnight'
    if ((p.color_scheme === 'light') && LIGHT_VARIANTS[eff]) eff = LIGHT_VARIANTS[eff]
    document.documentElement.setAttribute('data-theme', eff)
    document.querySelectorAll('.vnso-tp-menu [role="option"]').forEach(function (li) {
      li.setAttribute('aria-selected', li.dataset.theme === themeId ? 'true' : 'false')
    })
    var lbl = document.querySelector('.vnso-tp-label')
    if (lbl) lbl.textContent = LABELS[themeId] || themeId
    savePrefs(Object.assign({}, p, { theme: themeId }))
  }

  function mount () {
    if (document.getElementById('vnso-theme-picker')) return
    var prefs = loadPrefs()
    var cur = prefs.theme || 'midnight'
    var menuHtml = GROUPS.map(function (g) {
      var sec = '<li class="vnso-tp-section" aria-hidden="true">' + g.label + '</li>'
      var items = g.themes.map(function (id) {
        var sw = id.replace(/[^a-z0-9]/g, '-')
        var sel = cur === id ? ' aria-selected="true"' : ''
        return '<li role="option" data-theme="' + id + '" tabindex="0"' + sel + '><span class="vnso-sw vnso-sw-' + sw + '" aria-hidden="true"></span>' + (LABELS[id] || id) + '</li>'
      }).join('')
      return sec + items
    }).join('')
    var host = document.createElement('div')
    host.id = 'vnso-theme-picker'
    host.className = 'vnso-theme-picker'
    host.innerHTML = '<button type="button" class="vnso-tp-btn" aria-haspopup="listbox" aria-expanded="false" title="Chọn giao diện"><span class="vnso-tp-swatch" aria-hidden="true"></span><span class="vnso-tp-label">' + (LABELS[cur] || cur) + '</span><span class="vnso-tp-caret" aria-hidden="true">▾</span></button><ul class="vnso-tp-menu" role="listbox" hidden>' + menuHtml + '</ul>'
    var btn = host.querySelector('.vnso-tp-btn')
    var menu = host.querySelector('.vnso-tp-menu')
    btn.addEventListener('click', function (e) { e.stopPropagation(); var o = menu.hidden; menu.hidden = !o; btn.setAttribute('aria-expanded', String(!o)) })
    menu.addEventListener('click', function (e) { var li = e.target.closest('[role="option"]'); if (!li) return; e.stopPropagation(); applyTheme(li.dataset.theme); menu.hidden = true; btn.setAttribute('aria-expanded', 'false') })
    document.addEventListener('click', function () { menu.hidden = true; btn.setAttribute('aria-expanded', 'false') })
    document.body.appendChild(host)
    applyTheme(cur)
  }

  if (document.readyState === 'loading') document.addEventListener('DOMContentLoaded', mount, { once: true })
  else mount()
})()



vueApp.use(VueAxios, router)
vueApp.use(pollJobPlugin)
vueApp.use(notifierPlugin)
vueApp.use(toLocaleDatePlugin)
vueApp.use(configUtilPlugin)
vueApp.use(apiMetaUtilPlugin)
vueApp.use(showIconPlugin)
vueApp.use(resourceTypePlugin)
vueApp.use(fileSizeUtilPlugin)
vueApp.use(localesPlugin)
vueApp.use(genericUtilPlugin)
vueApp.use(dialogUtilPlugin)
vueApp.use(cpuArchitectureUtilPlugin)
vueApp.use(imagesUtilPlugin)
vueApp.use(extensionsUtilPlugin)
vueApp.use(backupUtilPlugin)
vueApp.use(extensions)
vueApp.use(directives)

const renderError = (err) => {
  console.error('Fatal error during app initialization: ', err)
  const ErrorComponent = {
    render: () => h(
      'div',
      { style: 'font-family: sans-serif; text-align: center; padding: 2rem;' },
      [
        h('h2', { style: 'color: #ff4d4f;' }, 'We\'re experiencing a problem'),
        h('p', 'The application could not be loaded due to a configuration issue. Please try again later.'),
        h('details', { style: 'margin-top: 20px;' }, [
          h('summary', { style: 'cursor: pointer;' }, 'Technical details'),
          h('pre', {
            style: 'text-align: left; display: inline-block; margin-top: 10px;'
          }, 'Missing or malformed config.json. Please ensure the file is present, accessible, and contains valid JSON. Check the browser console for more information.')
        ])
      ]
    )
  }
  createApp(ErrorComponent).mount('#app')
}

fetch('config.json?ts=' + Date.now())
  .then(response => {
    if (!response.ok) {
      throw new Error(`Failed to fetch config.json: ${response.status} ${response.statusText}`)
    }
    return response.json()
  })
  .then(async config => {
    vueProps.$config = config
    let baseUrl = config.apiBase
    if (config.multipleServer) {
      baseUrl = (config.servers[0].apiHost || '') + config.servers[0].apiBase
    }

    vueProps.axios.defaults.baseURL = baseUrl

    const userid = Cookies.get('userid')
    let accountid = null
    let domainid = null

    if (userid !== undefined && Cookies.get('sessionkey')) {
      await getAPI('listUsers', { userid: userid }).then(response => {
        accountid = response.listusersresponse.user[0].accountid
        domainid = response.listusersresponse.user[0].domainid
      })
    }

    await applyCustomGuiTheme(accountid, domainid)

    loadLanguageAsync().then(() => {
      vueApp.use(store)
        .use(router)
        .use(i18n)
        .use(bootstrap)
        .mount('#app')
    })
  }).catch(error => {
    renderError(error)
  })
