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

const THEME_MODE_KEY = 'UI_THEME_MODE'
const SYSTEM_QUERY = '(prefers-color-scheme: dark)'

export function normalizeThemeMode (mode) {
  if (mode === 'dark' || mode === 'system') {
    return mode
  }
  return 'light'
}

export function getSavedThemeMode (config) {
  const savedMode = window.localStorage.getItem(THEME_MODE_KEY)
  if (savedMode) {
    return normalizeThemeMode(savedMode)
  }
  return normalizeThemeMode(config?.theme?.['@layout-mode'])
}

export function isDarkModeActive (mode) {
  const normalized = normalizeThemeMode(mode)
  if (normalized === 'dark') {
    return true
  }
  if (normalized === 'system') {
    return window.matchMedia?.(SYSTEM_QUERY)?.matches ?? false
  }
  return false
}

export function applyThemeMode (mode, store, config) {
  const normalized = normalizeThemeMode(mode)
  window.localStorage.setItem(THEME_MODE_KEY, normalized)

  if (config?.theme) {
    config.theme['@layout-mode'] = normalized
  }

  const darkMode = isDarkModeActive(normalized)
  if (store) {
    store.dispatch('SetDarkMode', darkMode)
  }
  document.body.classList.toggle('dark-mode', darkMode)

  return normalized
}

export function watchSystemTheme (callback) {
  const media = window.matchMedia?.(SYSTEM_QUERY)
  if (!media || !callback) {
    return () => {}
  }

  if (media.addEventListener) {
    media.addEventListener('change', callback)
    return () => media.removeEventListener('change', callback)
  }

  media.addListener(callback)
  return () => media.removeListener(callback)
}
