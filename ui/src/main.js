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

const mountSharedThemePicker = () => {
  if (document.getElementById('vnso-theme-picker')) return

  const host = document.createElement('div')
  host.id = 'vnso-theme-picker'
  host.style.cssText =
    'position:fixed;right:16px;bottom:16px;z-index:9999;display:flex;gap:8px;align-items:center;padding:8px 10px;border:1px solid var(--border,rgba(148,163,184,.25));border-radius:10px;background:var(--bg-secondary,rgba(15,23,42,.85));backdrop-filter:blur(8px);box-shadow:0 10px 24px rgba(2,6,23,.28);'

  const mode = document.createElement('select')
  mode.setAttribute('aria-label', 'Color mode')
  mode.innerHTML =
    '<option value="system">Auto</option><option value="dark">Dark</option><option value="light">Light</option>'

  const theme = document.createElement('select')
  theme.setAttribute('aria-label', 'Theme family')
  theme.innerHTML =
    '<option value="anthropic">anthropic</option><option value="v0">v0</option><option value="github-dim">github</option><option value="midnight">midnight</option><option value="linear">linear</option><option value="stripe">stripe</option><option value="notion">notion</option><option value="figma">figma</option><option value="raycast">raycast</option><option value="supabase">supabase</option><option value="railway">railway</option><option value="light">light</option><option value="system">system</option>'

  const controlStyle =
    'height:32px;padding:0 10px;border-radius:8px;border:1px solid var(--border,rgba(148,163,184,.3));background:var(--card-bg,#0f172a);color:var(--text,#e2e8f0);font:600 12px/1.2 ui-sans-serif,system-ui,sans-serif;'
  mode.style.cssText = controlStyle
  theme.style.cssText = controlStyle

  const prefs = typeof window.__getPrefs === 'function' ? window.__getPrefs() : {}
  mode.value = (prefs && prefs.color_scheme) || 'system'
  theme.value = (prefs && prefs.theme) || 'midnight'

  mode.addEventListener('change', () => {
    if (typeof window.__setColorScheme === 'function') window.__setColorScheme(mode.value)
  })
  theme.addEventListener('change', () => {
    if (typeof window.__setTheme === 'function') window.__setTheme(theme.value)
  })

  host.append(mode, theme)
  document.body.appendChild(host)
}

if (document.readyState === 'loading') {
  document.addEventListener('DOMContentLoaded', mountSharedThemePicker, { once: true })
} else {
  mountSharedThemePicker()
}

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
