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

<template>
  <a-select
    v-model:value="themeMode"
    size="small"
    class="theme-mode-toggle"
    @change="onThemeModeChange">
    <a-select-option value="light">Light</a-select-option>
    <a-select-option value="dark">Dark</a-select-option>
    <a-select-option value="system">System</a-select-option>
  </a-select>
</template>

<script>
import { applyThemeMode, getSavedThemeMode, watchSystemTheme } from '@/utils/themeMode'

export default {
  name: 'ThemeModeToggle',
  data () {
    return {
      themeMode: 'light',
      cleanupSystemWatcher: null
    }
  },
  mounted () {
    this.themeMode = applyThemeMode(getSavedThemeMode(this.$config), this.$store, this.$config)
    this.bindSystemWatcher()
  },
  beforeUnmount () {
    if (this.cleanupSystemWatcher) {
      this.cleanupSystemWatcher()
      this.cleanupSystemWatcher = null
    }
  },
  methods: {
    bindSystemWatcher () {
      if (this.cleanupSystemWatcher) {
        this.cleanupSystemWatcher()
      }

      if (this.themeMode !== 'system') {
        this.cleanupSystemWatcher = null
        return
      }

      this.cleanupSystemWatcher = watchSystemTheme(() => {
        applyThemeMode('system', this.$store, this.$config)
      })
    },
    onThemeModeChange (value) {
      this.themeMode = applyThemeMode(value, this.$store, this.$config)
      this.bindSystemWatcher()
      this.$emit('changed', this.themeMode)
    }
  }
}
</script>

<style lang="less" scoped>
.theme-mode-toggle {
  width: 110px;
}
</style>
