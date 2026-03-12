<template>
  <div>
    <a-card title="Gemini API Console" :bordered="false">
      <a-space direction="vertical" style="width: 100%">
        <a-select v-model:value="selectedModel" placeholder="Select Model" style="width: 300px">
          <a-select-option value="gemini-1.5-pro">Gemini 1.5 Pro</a-select-option>
          <a-select-option value="gemini-1.5-flash">Gemini 1.5 Flash</a-select-option>
          <a-select-option value="gemini-2.0-flash">Gemini 2.0 Flash</a-select-option>
          <a-select-option value="gemini-2.0-pro">Gemini 2.0 Pro</a-select-option>
        </a-select>

        <div class="chat-container" ref="chatContainer">
          <div v-for="(msg, index) in messages" :key="index" :class="['message', msg.role]">
            <div class="message-content">
              <strong>{{ msg.role === 'user' ? 'You' : 'Gemini' }}:</strong>
              <div v-html="msg.content" />
            </div>
          </div>
        </div>

        <a-input-group compact>
          <a-textarea
            v-model:value="prompt"
            placeholder="Enter your prompt..."
            :rows="3"
            style="width: calc(100% - 100px)"
            @keydown.enter.ctrl="sendMessage"
          />
          <a-button type="primary" @click="sendMessage" :loading="loading" style="height: 76px; width: 100px">
            Send
          </a-button>
        </a-input-group>

        <a-row :gutter="16">
          <a-col :span="8">
            <a-statistic title="Temperature" :value="temperature" />
            <a-slider v-model:value="temperature" :min="0" :max="2" :step="0.1" />
          </a-col>
          <a-col :span="8">
            <a-statistic title="Max Tokens" :value="maxTokens" />
            <a-slider v-model:value="maxTokens" :min="1" :max="8192" :step="256" />
          </a-col>
          <a-col :span="8">
            <a-statistic title="Messages" :value="messages.length" />
          </a-col>
        </a-row>
      </a-space>
    </a-card>
  </div>
</template>

<script>
export default {
  name: 'GcpGeminiConsole',
  data () {
    return {
      selectedModel: 'gemini-2.0-flash',
      prompt: '',
      messages: [],
      loading: false,
      temperature: 0.7,
      maxTokens: 2048
    }
  },
  methods: {
    async sendMessage () {
      if (!this.prompt.trim()) return
      const userMsg = this.prompt.trim()
      this.messages.push({ role: 'user', content: userMsg })
      this.prompt = ''
      this.loading = true
      try {
        // TODO: Integrate with actual Gemini API via CloudStack backend
        this.messages.push({
          role: 'assistant',
          content: `<em>[Gemini ${this.selectedModel} response will appear here when backend is connected]</em>`
        })
      } finally {
        this.loading = false
        this.$nextTick(() => {
          if (this.$refs.chatContainer) {
            this.$refs.chatContainer.scrollTop = this.$refs.chatContainer.scrollHeight
          }
        })
      }
    }
  }
}
</script>

<style scoped>
.chat-container {
  max-height: 400px;
  overflow-y: auto;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  padding: 12px;
  background: #fafafa;
}
.message {
  margin-bottom: 12px;
  padding: 8px 12px;
  border-radius: 8px;
}
.message.user {
  background: #e6f7ff;
  text-align: right;
}
.message.assistant {
  background: #f6ffed;
}
</style>
