const Mock = require('mockjs')
const fs = require('fs')
const path = require('path')

// ====================== 1. 定义枚举字典（完全对照文档）======================
// 手机品牌
const BA_LIST = ["Xiaomi", "iPhone", "Redmi", "Huawei", "vivo", "Oneplus", "Honor"]
// 下载渠道
const CH_LIST = ["xiaomi", "360", "oppo", "vivo", "wandoujia", "Appstore", "web"]
// 新老用户
const IS_NEW = ["0", "1"]
// 手机型号（对应品牌）
const MD_MAP = {
  "Xiaomi": ["Xiaomi 10 Pro", "Xiaomi 9", "Xiaomi Mix2"],
  "iPhone": ["iPhone Xs", "iPhone Xs Max", "iPhone X", "iPhone 8"],
  "Redmi": ["Redmi k30"],
  "Huawei": ["Huawei Mate 30", "Huawei P30"],
  "vivo": ["vivo iqoo3"],
  "Oneplus": ["Oneplus 7"],
  "Honor": ["Honor 20s"]
}
// 系统版本
const OS_LIST = ["Android 11.0", "Android 10.0", "Android 9.0", "Android 8.1", "iOS 13.2.9", "iOS 13.3.1", "iOS 12.4.1"]
// APP版本
const VC_LIST = ["v2.1.134", "v2.1.132", "v2.1.111", "v2.0.1"]
// 地区编码
const AR_LIST = ["370000", "110000", "230000", "310000", "440000", "500000", "420000", "530000"]
// 页面ID
const PAGE_ID_LIST = [
  "home", "mine", "good_list", "good_detail", "good_spec",
  "cart", "trade", "payment", "search", "login",
  "comment", "orders_unpaid"
]
// 页面来源类型
const SOURCE_TYPE = ["activity", "promotion", "query", "recommend"]
// 曝光类型
const DISPLAY_TYPE = ["activity", "promotion", "query", "recommend"]
// 行为action_id
const ACTION_LIST = ["favor_add", "get_coupon", "cart_add", "cart_remove", "trade_add_address"]
// 启动入口
const START_ENTRY = ["icon", "notice", "install"]
// 错误固定内容
const ERR_MSG = " Exception in thread  java.net.SocketTimeoutException\n \tat com.atgugu.gmall2020.mock.log.bean.AppError.main(AppError.java:xxxxxx)"

// ====================== 2. 生成单条APP行为日志（严格匹配结构）======================
function generateOneLog() {
  const brand = Mock.Random.pick(BA_LIST)
  const model = Mock.Random.pick(MD_MAP[brand])
  const ar = Mock.Random.pick(AR_LIST)
  const ch = Mock.Random.pick(CH_LIST)
  const isNew = Mock.Random.pick(IS_NEW)
  const os = Mock.Random.pick(OS_LIST)
  const vc = Mock.Random.pick(VC_LIST)
  const pageId = Mock.Random.pick(PAGE_ID_LIST)
  const lastPageId = Mock.Random.pick(PAGE_ID_LIST)

  // 1. 公共字段 common（必选）
  const common = {
    ar: ar,
    ba: brand,
    ch: ch,
    is_new: isNew,
    md: model,
    mid: "mid_" + Mock.Random.integer(1, 30),
    os: os,
    uid: String(Mock.Random.integer(1, 50)),
    vc: vc
  }

  // 2. page页面字段（必选）
  const page = {
    page_id: pageId,
    last_page_id: lastPageId,
    during_time: Mock.Random.integer(1000, 20000)
  }
  // 随机增加 item / item_type
  const hasItem = Mock.Random.boolean()
  if (hasItem) {
    const itemType = Mock.Random.pick(["sku_id", "sku_ids", "keyword"])
    page.item_type = itemType
    if (itemType === "keyword") {
      page.item = Mock.Random.cword(2, 6)
    } else if (itemType === "sku_id") {
      page.item = String(Mock.Random.integer(1, 10))
    } else {
      page.item = [1, 2, 3].map(() => Mock.Random.integer(1, 10)).join(",")
    }
    page.source_type = Mock.Random.pick(SOURCE_TYPE)
  }

  // 3. displays 商品曝光（可选，随机出现）
  let displays = null
  if (Mock.Random.boolean()) {
    const displayCount = Mock.Random.integer(1, 10)
    displays = []
    for (let i = 0; i < displayCount; i++) {
      displays.push({
        display_type: Mock.Random.pick(DISPLAY_TYPE),
        item: String(Mock.Random.integer(1, 10)),
        item_type: Mock.Random.pick(["activity_id", "sku_id"]),
        order: i + 1,
        pos_id: Mock.Random.integer(1, 5)
      })
    }
  }

  // 4. actions 用户行为（可选，随机出现）
  let actions = null
  if (Mock.Random.boolean()) {
    const actionCount = Mock.Random.integer(1, 3)
    actions = []
    for (let i = 0; i < actionCount; i++) {
      actions.push({
        action_id: Mock.Random.pick(ACTION_LIST),
        item: String(Mock.Random.integer(1, 3)),
        item_type: Mock.Random.pick(["sku_id", "coupon_id"]),
        ts: Mock.Random.now()
      })
    }
  }

  // 5. start 应用启动（可选，随机出现）
  let start = null
  if (Mock.Random.boolean()) {
    start = {
      entry: Mock.Random.pick(START_ENTRY),
      loading_time: Mock.Random.integer(1000, 20000),
      open_ad_id: Mock.Random.integer(1, 20),
      open_ad_ms: Mock.Random.integer(1000, 10000),
      open_ad_skip_ms: Mock.Random.integer(0, 5000)
    }
  }

  // 6. err 异常报错（可选，随机出现）
  let err = null
  if (Mock.Random.boolean()) {
    err = {
      error_code: Mock.Random.integer(1000, 4000),
      msg: ERR_MSG
    }
  }

  // 7. 全局时间戳（必选）
  const ts = Mock.Random.now()

  // 组装最终日志对象，剔除值为null的可选字段
  const log = { common, page, ts }
  if (displays) log.displays = displays
  if (actions) log.actions = actions
  if (start) log.start = start
  if (err) log.err = err

  return log
}

// ====================== 3. 批量生成 & 写入日志文件 ======================
/**
 * @param total 生成日志总条数
 * @param filePath 输出文件路径
 */
function generateBatchLog(total = 100, filePath = "./output/output.log") {
  const writeStream = fs.createWriteStream(path.resolve(__dirname, filePath), { encoding: "utf-8" })

  for (let i = 0; i < total; i++) {
    const logObj = generateOneLog()
    // 按要求：每行一条标准JSON字符串
    writeStream.write(JSON.stringify(logObj) + "\n")
  }

  writeStream.end(() => {
    console.log(`✅ 生成完成！共 ${total} 条日志，文件路径：${filePath}`)
  })
}

// ========== 执行入口 ==========
// 修改数字可调整生成条数，示例：生成200条
generateBatchLog(200)

module.exports = {
  generateOneLog
}