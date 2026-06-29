const ENDPOINTS = {
    visitor: "/api/visitor/total",
    product: "/api/product/top",
    province: "/api/province/top",
    keyword: "/api/keyword/top"
};

const charts = {};
let refreshTimer = null;

document.addEventListener("DOMContentLoaded", async () => {
    initClock();
    initCharts();
    await loadAll();
    refreshTimer = setInterval(loadAll, 30000);
});

function initClock() {
    const clock = document.getElementById("clock");
    const update = () => {
        const now = new Date();
        const pad = (n) => String(n).padStart(2, "0");
        clock.innerText = `${now.getFullYear()}-${pad(now.getMonth() + 1)}-${pad(now.getDate())} ${pad(now.getHours())}:${pad(now.getMinutes())}:${pad(now.getSeconds())}`;
    };
    update();
    setInterval(update, 1000);
}

function initCharts() {
    const ids = [
        "provinceMap",
        "visitorBar",
        "provinceRank",
        "productBar",
        "productLine",
        "keywordBar",
        "keywordSource",
        "visitorGauge"
    ];

    ids.forEach(id => {
        const dom = document.getElementById(id);
        if (dom) {
            charts[id] = echarts.init(dom);
        }
    });

    window.addEventListener("resize", () => {
        Object.values(charts).forEach(chart => chart.resize());
    });
}

async function loadAll() {
    try {
        setStatus(true, "数据状态：刷新中...");
        const [visitorRes, productRes, provinceRes, keywordRes] = await Promise.all([
            fetchJson(ENDPOINTS.visitor),
            fetchJson(ENDPOINTS.product),
            fetchJson(ENDPOINTS.province),
            fetchJson(ENDPOINTS.keyword)
        ]);

        const visitor = normalizeVisitor(visitorRes);
        const products = normalizeProducts(productRes);
        const provinces = normalizeProvinces(provinceRes);
        const keywords = normalizeKeywords(keywordRes);

        renderKpis(visitor);
        renderVisitorBar(visitor);
        renderVisitorGauge(visitor);

        renderProvinceMap(provinces);
        renderProvinceRank(provinces);

        renderProductBar(products);
        renderProductLine(products);

        renderKeywordBar(keywords);
        renderKeywordSource(keywords);

        setStatus(true, "数据状态：正常");
    } catch (e) {
        console.error(e);
        setStatus(false, "数据状态：接口加载失败");
    }
}

async function fetchJson(url) {
    const resp = await fetch(url, { method: "GET" });
    if (!resp.ok) {
        throw new Error("接口请求失败: " + url);
    }
    return await resp.json();
}

function setStatus(ok, text) {
    const el = document.getElementById("refreshStatus");
    el.innerText = text;
    el.className = ok ? "status ok" : "status error";
}

function pickData(res) {
    if (Array.isArray(res)) return res;
    if (Array.isArray(res?.data)) return res.data;
    if (Array.isArray(res?.rows)) return res.rows;
    if (Array.isArray(res?.list)) return res.list;
    return res?.data ?? res ?? {};
}

function num(v) {
    if (v === null || v === undefined || v === "") return 0;
    const n = Number(v);
    return Number.isNaN(n) ? 0 : n;
}

function formatNum(v) {
    return num(v).toLocaleString("zh-CN");
}

function formatPercent(v) {
    return `${(num(v) * 100).toFixed(2)}%`;
}

function normalizeVisitor(res) {
    const d = pickData(res);
    return {
        uvCt: num(d.uvCt ?? d.uv_ct ?? d.uv ?? 0),
        pvCt: num(d.pvCt ?? d.pv_ct ?? d.pv ?? 0),
        svCt: num(d.svCt ?? d.sv_ct ?? d.sv ?? 0),
        ujCt: num(d.ujCt ?? d.uj_ct ?? d.uj ?? 0),
        durSum: num(d.durSum ?? d.dur_sum ?? d.dur ?? 0)
    };
}

function normalizeProducts(res) {
    const arr = pickData(res);
    return (Array.isArray(arr) ? arr : []).map(item => ({
        skuName: item.skuName ?? item.sku_name ?? item.name ?? "未知商品",
        orderAmount: num(item.orderAmount ?? item.order_amount ?? item.paymentAmount ?? item.payment_amount ?? item.amount ?? 0),
        orderCt: num(item.orderCt ?? item.order_ct ?? item.order_count ?? 0),
        cartCt: num(item.cartCt ?? item.cart_ct ?? 0),
        favorCt: num(item.favorCt ?? item.favor_ct ?? 0),
        commentCt: num(item.commentCt ?? item.comment_ct ?? 0),
        refundCt: num(item.refundCt ?? item.refund_ct ?? 0)
    }));
}

function normalizeProvinceName(name) {
    if (!name) return "未知";
    const map = {
        "北京市": "北京",
        "天津市": "天津",
        "上海市": "上海",
        "重庆市": "重庆",
        "内蒙古自治区": "内蒙古",
        "广西壮族自治区": "广西",
        "西藏自治区": "西藏",
        "宁夏回族自治区": "宁夏",
        "新疆维吾尔自治区": "新疆",
        "香港特别行政区": "香港",
        "澳门特别行政区": "澳门"
    };
    if (map[name]) return map[name];
    return name
        .replace("省", "")
        .replace("市", "")
        .replace("壮族自治区", "")
        .replace("回族自治区", "")
        .replace("维吾尔自治区", "")
        .replace("自治区", "")
        .replace("特别行政区", "");
}

function normalizeProvinces(res) {
    const arr = pickData(res);
    return (Array.isArray(arr) ? arr : []).map(item => ({
        name: normalizeProvinceName(item.provinceName ?? item.province_name ?? item.name ?? "未知"),
        rawName: item.provinceName ?? item.province_name ?? item.name ?? "未知",
        orderAmount: num(item.orderAmount ?? item.order_amount ?? item.amount ?? 0),
        orderCount: num(item.orderCount ?? item.order_count ?? item.ct ?? 0)
    }));
}

function normalizeKeywords(res) {
    const arr = pickData(res);
    return (Array.isArray(arr) ? arr : []).map(item => ({
        keyword: item.keyword ?? item.word ?? "未知关键词",
        ct: num(item.ct ?? item.count ?? item.keyword_count ?? 0),
        source: item.source ?? "unknown"
    }));
}

function renderKpis(v) {
    const avgDur = v.svCt === 0 ? 0 : v.durSum / v.svCt;
    const bounceRate = v.svCt === 0 ? 0 : v.ujCt / v.svCt;

    document.getElementById("uvCt").innerText = formatNum(v.uvCt);
    document.getElementById("pvCt").innerText = formatNum(v.pvCt);
    document.getElementById("svCt").innerText = formatNum(v.svCt);
    document.getElementById("ujCt").innerText = formatNum(v.ujCt);
    document.getElementById("avgDur").innerText = avgDur.toFixed(2);
    document.getElementById("bounceRate").innerText = formatPercent(bounceRate);
}

function renderVisitorBar(v) {
    charts.visitorBar.setOption({
        backgroundColor: "transparent",
        tooltip: { trigger: "axis" },
        xAxis: {
            type: "category",
            data: ["UV", "PV", "SV", "UJ"],
            axisLine: { lineStyle: { color: "#7ea5d9" } },
            axisLabel: { color: "#cfe2ff" }
        },
        yAxis: {
            type: "value",
            axisLine: { lineStyle: { color: "#7ea5d9" } },
            splitLine: { lineStyle: { color: "rgba(126,165,217,0.15)" } },
            axisLabel: { color: "#cfe2ff" }
        },
        series: [{
            type: "bar",
            data: [v.uvCt, v.pvCt, v.svCt, v.ujCt],
            barWidth: 40,
            itemStyle: {
                borderRadius: [8, 8, 0, 0],
                color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                    { offset: 0, color: "#4fd2ff" },
                    { offset: 1, color: "#3a6cff" }
                ])
            },
            label: {
                show: true,
                position: "top",
                color: "#fff"
            }
        }]
    });
}

function renderVisitorGauge(v) {
    const bounceRate = v.svCt === 0 ? 0 : (v.ujCt / v.svCt * 100);
    charts.visitorGauge.setOption({
        tooltip: {
            formatter: "{a}<br/>{b}: {c}%"
        },
        series: [{
            name: "跳出率",
            type: "gauge",
            min: 0,
            max: 100,
            progress: { show: true, width: 14 },
            axisLine: { lineStyle: { width: 14 } },
            detail: {
                valueAnimation: true,
                formatter: "{value}%",
                color: "#fff",
                fontSize: 26
            },
            axisLabel: { color: "#cfe2ff" },
            title: { color: "#8fb7e3" },
            data: [{ value: Number(bounceRate.toFixed(2)), name: "跳出率" }]
        }]
    });
}

function renderProvinceMap(list) {
    const mapData = list.map(item => ({
        name: item.name,
        value: item.orderAmount,
        orderCount: item.orderCount,
        rawName: item.rawName
    }));

    charts.provinceMap.setOption({
        tooltip: {
            trigger: "item",
            backgroundColor: "rgba(10,20,40,0.95)",
            borderColor: "#3b76ff",
            textStyle: { color: "#fff" },
            formatter: params => {
                const d = params.data || {};
                return `
                    <div style="font-size:14px;line-height:1.8;">
                        <div><b>${d.rawName || params.name}</b></div>
                        <div>交易额：${formatNum(d.value || 0)}</div>
                        <div>订单数：${formatNum(d.orderCount || 0)}</div>
                    </div>
                `;
            }
        },
        visualMap: {
            min: 0,
            max: Math.max(...mapData.map(i => i.value), 1),
            text: ["高", "低"],
            realtime: false,
            calculable: true,
            inRange: {
                color: ["#163a7a", "#2f7df6", "#4fd2ff"]
            },
            textStyle: { color: "#cfe2ff" }
        },
        series: [{
            name: "省份交易额",
            type: "map",
            map: "china",
            roam: true,
            emphasis: {
                label: { show: true, color: "#fff" },
                itemStyle: {
                    areaColor: "#ffb347"
                }
            },
            itemStyle: {
                areaColor: "#1b4c94",
                borderColor: "#9bd8ff"
            },
            data: mapData
        }]
    });
}

function renderProvinceRank(list) {
    const top10 = [...list]
        .sort((a, b) => b.orderAmount - a.orderAmount)
        .slice(0, 10)
        .reverse();

    charts.provinceRank.setOption({
        tooltip: { trigger: "axis" },
        grid: { left: 80, right: 20, top: 20, bottom: 20 },
        xAxis: {
            type: "value",
            axisLine: { lineStyle: { color: "#7ea5d9" } },
            splitLine: { lineStyle: { color: "rgba(126,165,217,0.15)" } },
            axisLabel: { color: "#cfe2ff" }
        },
        yAxis: {
            type: "category",
            data: top10.map(i => i.name),
            axisLine: { lineStyle: { color: "#7ea5d9" } },
            axisLabel: { color: "#cfe2ff" }
        },
        series: [{
            type: "bar",
            data: top10.map(i => i.orderAmount),
            itemStyle: {
                borderRadius: [0, 8, 8, 0],
                color: "#4fd2ff"
            },
            label: {
                show: true,
                position: "right",
                color: "#fff"
            }
        }]
    });
}

function renderProductBar(list) {
    const top10 = [...list]
        .sort((a, b) => b.orderAmount - a.orderAmount)
        .slice(0, 10);

    charts.productBar.setOption({
        tooltip: { trigger: "axis" },
        grid: { left: 50, right: 20, top: 20, bottom: 80 },
        xAxis: {
            type: "category",
            data: top10.map(i => i.skuName),
            axisLine: { lineStyle: { color: "#7ea5d9" } },
            axisLabel: {
                color: "#cfe2ff",
                rotate: 35
            }
        },
        yAxis: {
            type: "value",
            axisLine: { lineStyle: { color: "#7ea5d9" } },
            splitLine: { lineStyle: { color: "rgba(126,165,217,0.15)" } },
            axisLabel: { color: "#cfe2ff" }
        },
        series: [{
            name: "成交额",
            type: "bar",
            data: top10.map(i => i.orderAmount),
            barWidth: 28,
            itemStyle: {
                borderRadius: [8, 8, 0, 0],
                color: "#63e6be"
            }
        }]
    });
}

function renderProductLine(list) {
    const top8 = [...list]
        .sort((a, b) => b.orderAmount - a.orderAmount)
        .slice(0, 8);

    charts.productLine.setOption({
        tooltip: { trigger: "axis" },
        legend: {
            data: ["购物车", "收藏", "评论"],
            textStyle: { color: "#cfe2ff" }
        },
        grid: { left: 50, right: 20, top: 50, bottom: 70 },
        xAxis: {
            type: "category",
            data: top8.map(i => i.skuName),
            axisLine: { lineStyle: { color: "#7ea5d9" } },
            axisLabel: {
                color: "#cfe2ff",
                rotate: 25
            }
        },
        yAxis: {
            type: "value",
            axisLine: { lineStyle: { color: "#7ea5d9" } },
            splitLine: { lineStyle: { color: "rgba(126,165,217,0.15)" } },
            axisLabel: { color: "#cfe2ff" }
        },
        series: [
            {
                name: "购物车",
                type: "line",
                smooth: true,
                data: top8.map(i => i.cartCt),
                areaStyle: {},
            },
            {
                name: "收藏",
                type: "line",
                smooth: true,
                data: top8.map(i => i.favorCt),
                areaStyle: {},
            },
            {
                name: "评论",
                type: "line",
                smooth: true,
                data: top8.map(i => i.commentCt),
                areaStyle: {},
            }
        ]
    });
}

function renderKeywordBar(list) {
    const top10 = [...list]
        .sort((a, b) => b.ct - a.ct)
        .slice(0, 10)
        .reverse();

    charts.keywordBar.setOption({
        tooltip: { trigger: "axis" },
        grid: { left: 100, right: 20, top: 20, bottom: 20 },
        xAxis: {
            type: "value",
            axisLine: { lineStyle: { color: "#7ea5d9" } },
            splitLine: { lineStyle: { color: "rgba(126,165,217,0.15)" } },
            axisLabel: { color: "#cfe2ff" }
        },
        yAxis: {
            type: "category",
            data: top10.map(i => i.keyword),
            axisLine: { lineStyle: { color: "#7ea5d9" } },
            axisLabel: { color: "#cfe2ff" }
        },
        series: [{
            type: "bar",
            data: top10.map(i => i.ct),
            itemStyle: {
                borderRadius: [0, 8, 8, 0],
                color: "#f59f00"
            },
            label: {
                show: true,
                position: "right",
                color: "#fff"
            }
        }]
    });
}

function renderKeywordSource(list) {
    const sourceMap = {};
    list.forEach(item => {
        const key = item.source || "unknown";
        sourceMap[key] = (sourceMap[key] || 0) + item.ct;
    });

    const pieData = Object.keys(sourceMap).map(k => ({
        name: k,
        value: sourceMap[k]
    }));

    charts.keywordSource.setOption({
        tooltip: {
            trigger: "item"
        },
        legend: {
            bottom: 0,
            textStyle: { color: "#cfe2ff" }
        },
        series: [{
            name: "来源分布",
            type: "pie",
            radius: ["35%", "68%"],
            center: ["50%", "45%"],
            avoidLabelOverlap: false,
            label: {
                color: "#fff",
                formatter: "{b}\n{d}%"
            },
            data: pieData
        }]
    });
}
