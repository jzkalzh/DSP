const http = require('http')
const Mock = require('mockjs')
const os = require('os')
const fs = require('fs')
const path = require('path')

// 导入生成函数
const { generateOneLog } = require('./app-log-generator')

const PORT = 3006
// SpringBoot 服务地址
const SPRING_HOST = '127.0.0.1'
const SPRING_PORT = 8080
const SPRING_PATH = '/applog'

/**
 * 推送单条日志JSON字符串到SpringBoot接口
 * @param logObj 生成的日志对象
 */
function sendToSpringBoot(logObj) {
  const logStr = JSON.stringify(logObj)
  const postOpt = {
    hostname: SPRING_HOST,
    port: SPRING_PORT,
    path: SPRING_PATH,
    method: 'POST',
    headers: {
      'Content-Type': 'text/plain',
      'Content-Length': Buffer.byteLength(logStr)
    }
  }

  const req = http.request(postOpt, (res) => {
    let buf = ''
    res.on('data', chunk => buf += chunk)
    res.on('end', () => {
      console.log(`推送成功 -> SpringBoot响应: ${buf}`)
    })
  })

  req.on('error', err => {
    console.error(`推送SpringBoot失败: ${err.message}`)
  })

  req.write(logStr)
  req.end()
}

// 自动获取本机内网IPv4
function getLocalIp() {
  const netInterfaces = os.networkInterfaces()
  let targetIp = '127.0.0.1'
  Object.values(netInterfaces).forEach(interfaces => {
    interfaces.forEach(item => {
      if (item.family === 'IPv4' && item.address !== '127.0.0.1' && !item.internal) {
        targetIp = item.address
      }
    })
  })
  return targetIp
}
const localIP = getLocalIp()

const server = http.createServer((req, res) => {
  res.setHeader('Access-Control-Allow-Origin', '*')

  // 1. 前端Vue可视化页面路由
  if (req.url === '/' && req.method === 'GET') {
    res.setHeader('Content-Type', 'text/html;charset=utf-8')
    // 下面完整Vue单文件页面内容嵌入此处
    const html = `
<!DOCTYPE html>
<html lang="zh-CN">
<head>
  <meta charset="UTF-8">
  <title>Mock日志发送控制台</title>
  <script src="https://cdn.jsdelivr.net/npm/vue@2.6.14/dist/vue.js"></script>
  <style>
    * {margin: 0;padding:0;box-sizing: border-box;font-family: "Microsoft Yahei"}
    body {background:#f5f7fa;padding:40px}
    .container {max-width:800px;margin:0 auto;background:#fff;padding:30px;border-radius:12px;box-shadow:0 2px 12px #e4e7ed}
    .title {font-size:22px;margin-bottom:24px;color:#303133;text-align:center}
    .row {margin:16px 0;display:flex;align-items:center;gap:12px}
    label {width:120px;color:#606266}
    input[type="number"] {flex:1;padding:10px 14px;border:1px solid #dcdfe6;border-radius:6px;font-size:16px}
    .btn-group {margin-top:24px;display:flex;gap:16px}
    button {padding:12px 24px;border:none;border-radius:6px;font-size:15px;cursor:pointer}
    .btn-single {background:#409eff;color:#fff}
    .btn-batch {background:#67c23a;color:#fff}
    .log-box {margin-top:30px;padding:16px;background:#fafafa;border:1px solid #e4e7ed;border-radius:8px;min-height:180px;white-space:pre-wrap;font-size:13px;color:#303133}
    .tip {color:#909399;font-size:13px;margin-top:8px}
  </style>
</head>
<body>
<div id="app" class="container">
  <h2 class="title">Mock 日志发送控制台</h2>

  <div class="row">
    <label>单次生成条数：</label>
    <input v-model.number="sendNum" type="number" min="1" max="1000" placeholder="输入1~1000数字">
  </div>
  <div class="tip">批量模式将一次性生成对应条数并逐条发送至SpringBoot(127.0.0.1:8080/applog)</div>

  <div class="btn-group">
    <button class="btn-single" @click="sendSingle">发送单条日志</button>
    <button class="btn-batch" @click="sendBatch">批量发送 {{sendNum}} 条日志</button>
  </div>

  <div style="margin-top:30px;font-weight:bold">操作日志：</div>
  <div class="log-box">{{logText}}</div>
</div>

<script>
new Vue({
  el: '#app',
  data() {
    return {
      sendNum: 10,
      logText: ''
    }
  },
  methods: {
    // 单条发送
    async sendSingle() {
      this.logText += '【' + new Date().toLocaleString() + '】开始发送单条日志\\n'
      try {
        const res = await fetch('/one')
        const data = await res.json()
        this.logText += '✅ 单条日志推送完成，示例数据：' + JSON.stringify(data).slice(0,120) + '...\\n'
      } catch (e) {
        this.logText += '❌ 发送失败：' + e.message + '\\n'
      }
    },
    // 批量发送
    async sendBatch() {
      const num = this.sendNum
      this.logText += '【' + new Date().toLocaleString() + '】开始批量发送' + num + '条日志\\n'
      try {
        const res = await fetch('/batch?num=' + num)
        const list = await res.json()
        this.logText += '✅ 批量推送完成，共' + list.length + '条\\n'
      } catch (e) {
        this.logText += '❌ 批量发送失败：' + e.message + '\\n'
      }
    }
  }
})
</script>
</body>
</html>
    `
    res.end(html)
    return
  }

  // 单条日志接口
  if (req.url === '/one' && req.method === 'GET') {
    res.setHeader('Content-Type', 'application/json;charset=utf-8')
    const logData = generateOneLog()
    sendToSpringBoot(logData)
    res.end(JSON.stringify(logData, null, 2))
    return
  }

  // 批量日志接口
  if (req.url.startsWith('/batch')) {
    res.setHeader('Content-Type', 'application/json;charset=utf-8')
    const urlInfo = new URL(req.url, `http://${localIP}:${PORT}`)
    const count = parseInt(urlInfo.searchParams.get('num')) || 10
    const resultList = []
    for (let i = 0; i < count; i++) {
      const logItem = generateOneLog()
      resultList.push(logItem)
      sendToSpringBoot(logItem)
    }
    res.end(JSON.stringify(resultList, null, 2))
    return
  }

  res.writeHead(404)
  res.setHeader('Content-Type', 'application/json;charset=utf-8')
  res.end(JSON.stringify({ msg: "接口不存在，访问 / 打开可视化控制台页面" }))
})

server.listen(PORT, () => {
  console.log(`✅ Mock可视化控制台地址：http://localhost:${PORT}`)
  console.log(`✅ 局域网访问地址：http://${localIP}:${PORT}`)
  console.log(`✅ 日志推送目标SpringBoot：http://${SPRING_HOST}:${SPRING_PORT}${SPRING_PATH}`)
})