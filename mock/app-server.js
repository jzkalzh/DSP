const http = require('http')
const Mock = require('mockjs')
const os = require('os')
// 导入生成函数
const { generateOneLog } = require('./app-log-generator')

const PORT = 3006

// 自动获取本机内网IPv4
function getLocalIp() {
  const netInterfaces = os.networkInterfaces()
  let targetIp = '127.0.0.1'
  Object.values(netInterfaces).forEach(interfaces => {
    interfaces.forEach(item => {
      // 筛选IPv4、非本地回环、内网地址
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
  res.setHeader('Content-Type', 'application/json;charset=utf-8')

  // 单条日志接口
  if (req.url === '/one' && req.method === 'GET') {
    const logData = generateOneLog()
    res.end(JSON.stringify(logData, null, 2))
    return
  }

  // 批量日志接口
  if (req.url.startsWith('/batch')) {
    const urlInfo = new URL(req.url, `http://${localIP}:${PORT}`)
    const count = parseInt(urlInfo.searchParams.get('num')) || 10
    const resultList = []
    for (let i = 0; i < count; i++) {
      resultList.push(generateOneLog())
    }
    res.end(JSON.stringify(resultList, null, 2))
    return
  }

  // 首页引导HTML
  if (req.url === '/') {
    res.setHeader('Content-Type', 'text/html;charset=utf-8')
    const html = `
    <h2>APP模拟日志服务</h2>
    <p>批量示例：<a href="/batch?num=15">/batch?num=15</a></p>
    `
    res.end(html)
    return
  }

  res.writeHead(404)
  res.end(JSON.stringify({msg:"接口不存在，访问 / 查看指引"}))
})

server.listen(PORT, () => {
  console.log(`✅ 浏览器服务启动成功，本机地址：http://localhost:${PORT}`)
  console.log(`✅ 局域网共享地址：http://${localIP}:${PORT}`)
})