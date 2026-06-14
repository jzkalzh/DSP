const Mock = require('mockjs')
// 导出mock数据配置
module.exports = () => {
  return {
    // 用户列表接口
    users: Mock.mock({
      'list|10': [{
        'id|+1': 1,
        username: '@cname',
        phone: /1[3-9]\d{9}/,
        email: '@email',
        createTime: '@datetime'
      }]
    }).list,
    // 商品示例接口（可自行增删）
    goods: Mock.mock({
      'list|5': [{
        id: '@increment',
        title: '@ctitle(5,10)',
        price: '@float(10,999,2,2)',
        stock: '@integer(10,200)'
      }]
    }).list
  }
}