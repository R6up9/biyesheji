<template>
  <div class="ai-chat">
    <!-- 悬浮按钮 -->
    <div v-if="!showChat" class="chat-button" @click="toggleChat">
      <el-icon :size="28"><ChatDotRound /></el-icon>
    </div>

    <!-- 聊天窗口 -->
    <el-card v-else class="chat-window" shadow="hover">
      <template #header>
        <div class="chat-header">
          <span>AI 智能客服</span>
          <el-icon class="close-icon" @click="toggleChat"><Close /></el-icon>
        </div>
      </template>

      <!-- 消息列表 -->
      <div class="message-list" ref="messageListRef">
        <div v-for="(msg, index) in messages" :key="index" class="message-item" :class="msg.role">
          <div v-if="msg.role === 'ai'" class="message-avatar">
            <el-avatar :size="36" :src="aiAvatar" />
          </div>
          <div class="message-content">
            <div class="message-bubble">{{ msg.content }}</div>
          </div>
          <div v-if="msg.role === 'user'" class="message-avatar">
            <el-avatar :size="36" :src="userAvatar" />
          </div>
        </div>
        <div v-if="typing" class="message-item ai">
          <div class="message-avatar">
            <el-avatar :size="36" :src="aiAvatar" />
          </div>
          <div class="message-content">
            <div class="message-bubble typing">
              <span></span>
              <span></span>
              <span></span>
            </div>
          </div>
        </div>
      </div>

      <!-- 输入框 -->
      <div class="input-area">
        <el-input
          v-model="inputMessage"
          type="textarea"
          :rows="2"
          placeholder="请输入您的问题..."
          @keyup.enter.ctrl="sendMessage"
          resize="none"
        />
        <el-button type="primary" @click="sendMessage" :loading="sending">
          发送
        </el-button>
      </div>

      <!-- 快捷问题 -->
      <div class="quick-questions">
        <el-tag v-for="(q, index) in quickQuestions" :key="index" @click="sendQuickQuestion(q)">
          {{ q }}
        </el-tag>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, nextTick } from 'vue'
import { ChatDotRound, Close } from '@element-plus/icons-vue'

defineOptions({ name: 'AIChat' })

const showChat = ref(false)
const inputMessage = ref('')
const messages = ref<{ role: 'user' | 'ai'; content: string }[]>([])
const typing = ref(false)
const sending = ref(false)
const messageListRef = ref<HTMLElement>()

const aiAvatar = 'data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHZpZXdCb3g9IjAgMCAxMDAgMTAwIj48Y2lyY2xlIGN4PSI1MCIgY3k9IjUwIiByPSI1MCIgZmlsbD0iIzQwOWVmZiIvPjx0ZXh0IHg9IjUwJSIgeT0iNTAlIiBkb21pbmFudC1iYXNlbGluZT0ibWlkZGxlIiB0ZXh0LWFuY2hvcj0ibWlkZGxlIiBmaWxsPSJ3aGl0ZSIgZm9udC1zaXplPSI0MCI+QUk8L3RleHQ+PC9zdmc+'
const userAvatar = 'data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHZpZXdCb3g9IjAgMCAxMDAgMTAwIj48Y2lyY2xlIGN4PSI1MCIgY3k9IjUwIiByPSI1MCIgZmlsbD0iIzY3YzIzYSIvPjx0ZXh0IHg9IjUwJSIgeT0iNTAlIiBkb21pbmFudC1iYXNlbGluZT0ibWlkZGxlIiB0ZXh0LWFuY2hvcj0ibWlkZGxlIiBmaWxsPSJ3aGl0ZSIgZm9udC1zaXplPSI0MCI+VTwvdGV4dD48L3N2Zz4='

const quickQuestions = [
  '如何预约场地？',
  '会员有什么权益？',
  '教练怎么预约？',
  '课程安排在哪里看？'
]

// 切换聊天窗口
const toggleChat = () => {
  showChat.value = !showChat.value
  if (showChat.value && messages.value.length === 0) {
    addMessage('ai', '您好！我是羽毛球俱乐部的 AI 智能客服，很高兴为您服务！有什么可以帮助您的吗？')
  }
}

// 添加消息
const addMessage = (role: 'user' | 'ai', content: string) => {
  messages.value.push({ role, content })
  scrollToBottom()
}

// 滚动到底部
const scrollToBottom = async () => {
  await nextTick()
  if (messageListRef.value) {
    messageListRef.value.scrollTop = messageListRef.value.scrollHeight
  }
}

// 发送快捷问题
const sendQuickQuestion = (question: string) => {
  inputMessage.value = question
  sendMessage()
}

// AI 回复（模拟）
const getAIResponse = (question: string): string => {
  const lowerQuestion = question.toLowerCase()
  
  if (lowerQuestion.includes('预约') && lowerQuestion.includes('场地')) {
    return '预约场地很简单！您可以在用户端的"场地预约"页面选择心仪的场地和时间段，填写预约信息后提交即可。预约成功后会有短信通知您。'
  }
  if (lowerQuestion.includes('会员') || lowerQuestion.includes('权益')) {
    return '我们的会员享有以下权益：1. 场地预订8折优惠；2. 免费使用淋浴设施；3. 优先预约热门时间段；4. 赠送专业教练体验课1节；5. 积分兑换精美礼品。'
  }
  if (lowerQuestion.includes('教练')) {
    return '预约教练请在"教练预约"页面选择您想预约的教练，查看教练的资质和可预约时间，选择合适的时间段提交预约即可。'
  }
  if (lowerQuestion.includes('课程')) {
    return '课程安排可以在"课程预约"页面查看，我们有初级班、中级班、高级班以及私教课程供您选择。'
  }
  if (lowerQuestion.includes('你好') || lowerQuestion.includes('您好')) {
    return '您好！很高兴为您服务！请问有什么可以帮助您的吗？'
  }
  if (lowerQuestion.includes('价格') || lowerQuestion.includes('费用')) {
    return '我们的场地价格从50元/小时起，会员享受8折优惠。具体价格请查看场地详情页面。'
  }
  if (lowerQuestion.includes('开放时间') || lowerQuestion.includes('营业时间')) {
    return '我们俱乐部的开放时间是每天上午8:00至晚上22:00，全年无休！'
  }
  
  return '感谢您的咨询！如需更多帮助，请联系人工客服，电话：400-123-4567。'
}

// 发送消息
const sendMessage = async () => {
  if (!inputMessage.value.trim() || sending.value) return
  
  const message = inputMessage.value.trim()
  inputMessage.value = ''
  sending.value = true
  
  // 添加用户消息
  addMessage('user', message)
  
  // AI 打字中
  typing.value = true
  
  // 模拟 AI 思考延迟
  await new Promise(resolve => setTimeout(resolve, 1000 + Math.random() * 1000))
  
  // 停止打字，添加 AI 回复
  typing.value = false
  addMessage('ai', getAIResponse(message))
  
  sending.value = false
}
</script>

<style scoped lang="scss">
.ai-chat {
  position: fixed;
  bottom: 30px;
  right: 30px;
  z-index: 9999;
  
  .chat-button {
    width: 60px;
    height: 60px;
    border-radius: 50%;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
    color: white;
    transition: all 0.3s;
    
    &:hover {
      transform: scale(1.1);
      box-shadow: 0 6px 20px rgba(0, 0, 0, 0.25);
    }
  }
  
  .chat-window {
    width: 420px;
    height: 600px;
    display: flex;
    flex-direction: column;
    
    .chat-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      font-weight: bold;
      
      .close-icon {
        cursor: pointer;
        color: #909399;
        transition: color 0.3s;
        
        &:hover {
          color: #409eff;
        }
      }
    }
    
    .message-list {
      flex: 1;
      overflow-y: auto;
      padding: 15px;
      
      .message-item {
        display: flex;
        margin-bottom: 15px;
        align-items: flex-start;
        
        &.user {
          flex-direction: row-reverse;
          
          .message-content {
            margin-right: 10px;
            
            .message-bubble {
              background: #409eff;
              color: white;
              border-radius: 12px 12px 0 12px;
            }
          }
          
          .message-avatar {
            margin-left: 0;
            margin-right: 0;
          }
        }
        
        &.ai {
          .message-content {
            margin-left: 10px;
            
            .message-bubble {
              background: #f5f7fa;
              color: #303133;
              border-radius: 12px 12px 12px 0;
            }
          }
        }
        
        .message-avatar {
          flex-shrink: 0;
        }
        
        .message-content {
          max-width: 70%;
          
          .message-bubble {
            padding: 12px 16px;
            line-height: 1.6;
            
            &.typing {
              display: flex;
              gap: 4px;
              padding: 8px 16px;
              
              span {
                width: 8px;
                height: 8px;
                border-radius: 50%;
                background: #909399;
                animation: typing 1.4s infinite ease-in-out both;
                
                &:nth-child(1) { animation-delay: -0.32s; }
                &:nth-child(2) { animation-delay: -0.16s; }
              }
            }
          }
        }
      }
    }
    
    .input-area {
      padding: 15px;
      border-top: 1px solid #e4e7ed;
      display: flex;
      gap: 10px;
      
      .el-textarea {
        flex: 1;
      }
    }
    
    .quick-questions {
      padding: 10px 15px;
      border-top: 1px solid #e4e7ed;
      display: flex;
      flex-wrap: wrap;
      gap: 8px;
      
      .el-tag {
        cursor: pointer;
        transition: all 0.3s;
        
        &:hover {
          background: #ecf5ff;
          color: #409eff;
        }
      }
    }
  }
}

@keyframes typing {
  0%, 80%, 100% { transform: scale(0); }
  40% { transform: scale(1); }
}
</style>
