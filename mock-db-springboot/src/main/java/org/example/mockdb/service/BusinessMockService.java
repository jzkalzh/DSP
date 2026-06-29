package org.example.mockdb.service;

import org.example.mockdb.config.MockProperties;
import org.example.mockdb.model.InsertSummary;
import org.example.mockdb.repository.MockJdbcRepository;
import org.example.mockdb.util.RandomData;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.LinkedHashMap;

@Service
public class BusinessMockService {
    private final MockJdbcRepository repository;
    private final MockProperties properties;

    public BusinessMockService(MockJdbcRepository repository, MockProperties properties) {
        this.repository = repository;
        this.properties = properties;
    }

    public InsertSummary mockBaseTrademark(int count) {
        InsertSummary summary = new InsertSummary();
        for (int i = 0; i < count; i++) {
            LinkedHashMap<String, Object> row = new LinkedHashMap<String, Object>();
            row.put("tm_name", RandomData.brand());
            row.put("logo_url", "/static/mock/logo_" + System.currentTimeMillis() % 100000 + ".png");
            repository.insert("base_trademark", row);
            summary.add("base_trademark", 1);
        }
        return summary;
    }

    public InsertSummary mockBaseDic(int count) {
        InsertSummary summary = new InsertSummary();
        for (int i = 0; i < count; i++) {
            LinkedHashMap<String, Object> row = new LinkedHashMap<String, Object>();
            row.put("id", RandomData.dicId());
            row.put("dic_name", "mock字典_" + System.currentTimeMillis() % 100000);
            row.put("parent_code", "ROOT");
            row.put("create_time", RandomData.now());
            row.put("operate_time", RandomData.now());
            repository.insert("base_dic", row);
            summary.add("base_dic", 1);
        }
        return summary;
    }

    public InsertSummary mockFavorInfo(int count) {
        InsertSummary summary = new InsertSummary();
        for (int i = 0; i < count; i++) {
            long skuId = RandomData.longRange(1, properties.getMaxSkuId());
            LinkedHashMap<String, Object> row = new LinkedHashMap<String, Object>();
            row.put("user_id", RandomData.longRange(1, properties.getMaxUserId()));
            row.put("sku_id", skuId);
            row.put("spu_id", skuId + 1000);
            row.put("is_cancel", "0");
            row.put("create_time", RandomData.now());
            row.put("cancel_time", null);
            repository.insert("favor_info", row);
            summary.add("favor_info", 1);
        }
        return summary;
    }

    public InsertSummary mockCartInfo(int count) {
        InsertSummary summary = new InsertSummary();
        for (int i = 0; i < count; i++) {
            long skuId = RandomData.longRange(1, properties.getMaxSkuId());
            BigDecimal price = RandomData.money(20, 2000);
            LinkedHashMap<String, Object> row = new LinkedHashMap<String, Object>();
            row.put("user_id", String.valueOf(RandomData.longRange(1, properties.getMaxUserId())));
            row.put("sku_id", skuId);
            row.put("cart_price", price);
            row.put("sku_num", RandomData.intRange(1, 5));
            row.put("img_url", RandomData.imageUrl(skuId));
            row.put("sku_name", RandomData.skuName(skuId));
            row.put("is_checked", 1);
            row.put("create_time", RandomData.now());
            row.put("operate_time", RandomData.now());
            row.put("is_ordered", 0L);
            row.put("order_time", null);
            row.put("source_type", "2401");
            row.put("source_id", RandomData.longRange(1, 100));
            repository.insert("cart_info", row);
            summary.add("cart_info", 1);
        }
        return summary;
    }

    public InsertSummary mockCommentInfo(int count) {
        InsertSummary summary = new InsertSummary();
        for (int i = 0; i < count; i++) {
            long skuId = RandomData.longRange(1, properties.getMaxSkuId());
            LinkedHashMap<String, Object> row = new LinkedHashMap<String, Object>();
            row.put("user_id", RandomData.longRange(1, properties.getMaxUserId()));
            row.put("nick_name", RandomData.personName());
            row.put("head_img", "/static/mock/head.png");
            row.put("sku_id", skuId);
            row.put("spu_id", skuId + 1000);
            row.put("order_id", RandomData.longRange(1, 99999));
            row.put("appraise", String.valueOf(RandomData.intRange(1, 3)));
            row.put("comment_txt", "这是一条 mock 评论，商品还不错 " + System.currentTimeMillis() % 100000);
            row.put("create_time", RandomData.now());
            row.put("operate_time", RandomData.now());
            repository.insert("comment_info", row);
            summary.add("comment_info", 1);
        }
        return summary;
    }

    @Transactional
    public InsertSummary mockOrderWithDetailAndPayment(int count) {
        InsertSummary summary = new InsertSummary();
        for (int i = 0; i < count; i++) {
            long userId = RandomData.longRange(1, properties.getMaxUserId());
            long skuId = RandomData.longRange(1, properties.getMaxSkuId());
            int skuNum = RandomData.intRange(1, 5);
            BigDecimal price = RandomData.money(50, 3000);
            BigDecimal total = price.multiply(BigDecimal.valueOf(skuNum)).setScale(2, BigDecimal.ROUND_HALF_UP);
            String outTradeNo = RandomData.outTradeNo();

            LinkedHashMap<String, Object> order = new LinkedHashMap<String, Object>();
            order.put("consignee", RandomData.personName());
            order.put("consignee_tel", RandomData.phone());
            order.put("total_amount", total);
            order.put("order_status", "1001");
            order.put("user_id", userId);
            order.put("payment_way", "1101");
            order.put("delivery_address", RandomData.address());
            order.put("order_comment", "mock订单");
            order.put("out_trade_no", outTradeNo);
            order.put("trade_body", "mock交易订单");
            order.put("create_time", RandomData.now());
            order.put("operate_time", RandomData.now());
            order.put("expire_time", RandomData.plusMinutes(30));
            order.put("process_status", "1001");
            order.put("tracking_no", "TRACK" + System.currentTimeMillis() % 1000000);
            order.put("parent_order_id", null);
            order.put("img_url", RandomData.imageUrl(skuId));
            order.put("province_id", RandomData.intRange(1, 34));
            order.put("activity_reduce_amount", BigDecimal.ZERO);
            order.put("coupon_reduce_amount", BigDecimal.ZERO);
            order.put("original_total_amount", total);
            order.put("feight_fee", RandomData.money(0, 10));
            order.put("feight_fee_reduce", BigDecimal.ZERO);
            order.put("refundable_time", RandomData.plusMinutes(60 * 24 * 30));
            order.put("final_total_amount", total);
            order.put("benefit_reduce_amount", BigDecimal.ZERO);
            Number orderIdNumber = repository.insert("order_info", order);
            long orderId = orderIdNumber == null ? RandomData.longRange(100000, 999999) : orderIdNumber.longValue();
            summary.add("order_info", 1);

            LinkedHashMap<String, Object> detail = new LinkedHashMap<String, Object>();
            detail.put("order_id", orderId);
            detail.put("sku_id", skuId);
            detail.put("sku_name", RandomData.skuName(skuId));
            detail.put("img_url", RandomData.imageUrl(skuId));
            detail.put("order_price", price);
            detail.put("sku_num", String.valueOf(skuNum));
            detail.put("create_time", RandomData.now());
            detail.put("source_type", "2401");
            detail.put("source_id", RandomData.longRange(1, 100));
            detail.put("split_total_amount", total);
            detail.put("split_activity_amount", BigDecimal.ZERO);
            detail.put("split_coupon_amount", BigDecimal.ZERO);
            repository.insert("order_detail", detail);
            summary.add("order_detail", 1);

            LinkedHashMap<String, Object> log = new LinkedHashMap<String, Object>();
            log.put("order_id", orderId);
            log.put("order_status", "1001");
            log.put("operate_time", RandomData.now());
            repository.insert("order_status_log", log);
            summary.add("order_status_log", 1);

            LinkedHashMap<String, Object> payment = new LinkedHashMap<String, Object>();
            payment.put("out_trade_no", outTradeNo);
            payment.put("order_id", orderId);
            payment.put("user_id", userId);
            payment.put("payment_type", "1101");
            payment.put("trade_no", "TRADE" + System.currentTimeMillis() % 1000000);
            payment.put("total_amount", total);
            payment.put("subject", "mock支付订单" + orderId);
            payment.put("payment_status", "1602");
            payment.put("create_time", RandomData.now());
            payment.put("callback_time", RandomData.now());
            payment.put("callback_content", "success");
            payment.put("alipay_trade_no", "ALI" + System.currentTimeMillis() % 1000000);
            payment.put("payment_time", new Timestamp(System.currentTimeMillis()));
            repository.insert("payment_info", payment);
            summary.add("payment_info", 1);
        }
        return summary;
    }

    public InsertSummary mockCoreAll(int count) {
        InsertSummary summary = new InsertSummary();
        merge(summary, mockBaseTrademark(count));
        merge(summary, mockBaseDic(count));
        merge(summary, mockFavorInfo(count));
        merge(summary, mockCartInfo(count));
        merge(summary, mockCommentInfo(count));
        merge(summary, mockOrderWithDetailAndPayment(count));
        return summary;
    }

    private void merge(InsertSummary target, InsertSummary source) {
        for (String table : source.getTableCounts().keySet()) {
            target.add(table, source.getTableCounts().get(table));
        }
    }
}
