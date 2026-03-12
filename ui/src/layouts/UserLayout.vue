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
  <div id="userLayout" :class="['user-layout', device]">
    <a-button
      v-if="showClear"
      type="default"
      size="small"
      class="button-clear-notification"
      @click="onClearNotification">{{ $t('label.clear.notification') }}</a-button>
    <div class="theme-toggle-container">
      <theme-mode-toggle />
    </div>
    <div class="user-layout-container">
      <div class="user-layout-header">
        <img
          v-if="$config.banner"
          :style="{
            width: $config.theme['@banner-width'],
            height: $config.theme['@banner-height']
          }"
          :src="$config.banner"
          class="user-layout-logo"
          alt="logo">
      </div>
      <route-view></route-view>
    </div>
    <div class="user-layout-footer" v-if="$config.loginFooter || $config.resetPasswordFooter">
      <label v-if="$route.name === 'resetPassword' && $config.resetPasswordFooter" v-html="$config.resetPasswordFooter"></label>
      <label v-else v-html="$config.loginFooter"></label>
    </div>
  </div>
</template>

<script>
import RouteView from '@/layouts/RouteView'
import ThemeModeToggle from '@/components/widgets/ThemeModeToggle.vue'
import { mixinDevice } from '@/utils/mixin.js'
import notification from 'ant-design-vue/es/notification'
import { applyThemeMode, getSavedThemeMode, watchSystemTheme } from '@/utils/themeMode'

export default {
  name: 'UserLayout',
  components: { RouteView, ThemeModeToggle },
  mixins: [mixinDevice],
  data () {
    return {
      showClear: false,
      cleanupSystemWatcher: null
    }
  },
  watch: {
    '$store.getters.countNotify' (countNotify) {
      this.showClear = false
      if (countNotify && countNotify > 0) {
        this.showClear = true
      }
    }
  },
  mounted () {
    document.body.classList.add('userLayout')

    const themeMode = applyThemeMode(getSavedThemeMode(this.$config), this.$store, this.$config)
    if (themeMode === 'system') {
      this.cleanupSystemWatcher = watchSystemTheme(() => {
        applyThemeMode('system', this.$store, this.$config)
      })
    }

    const countNotify = this.$store.getters.countNotify
    this.showClear = false
    if (countNotify && countNotify > 0) {
      this.showClear = true
    }
  },
  beforeUnmount () {
    if (this.cleanupSystemWatcher) {
      this.cleanupSystemWatcher()
      this.cleanupSystemWatcher = null
    }

    document.body.classList.remove('userLayout')
    document.body.classList.remove('dark-mode')
  },
  methods: {
    onClearNotification () {
      notification.destroy()
      this.$store.commit('SET_COUNT_NOTIFY', 0)
    }
  }
}
</script>

<style lang="less" scoped>
.theme-toggle-container {
  position: absolute;
  top: 16px;
  right: 16px;
  z-index: 10;
}

.user-layout {
  height: 100%;

  &-container {
    padding: 3rem 0;
    width: 100%;

    @media (min-height:600px) {
      padding: 0;
      position: relative;
      top: 50%;
      transform: translateY(-50%);
      margin-top: -50px;
    }
  }

  &-logo {
    border-style: none;
    margin: 0 auto 2rem;
    display: block;

    .mobile & {
      max-width: 300px;
      margin-bottom: 1rem;
    }
  }

  &-footer {
    display: flex;
    flex-direction: column;
    position: absolute;
    bottom: 20px;
    text-align: center;
    width: 100%;

    @media (max-height: 600px) {
      position: relative;
      margin-top: 50px;
    }

    label {
      width: 368px;
      font-weight: 500;
      margin: 0 auto;
    }
  }
}
</style>
