package org.example.mockdb.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Arrays;
import java.util.List;

@ConfigurationProperties(prefix = "mock")
public class MockProperties {
    /** 业务日期，默认用于生成 create_time。 */
    private String businessDate = "2026-06-17";
    private int maxUserId = 500;
    private int maxSkuId = 10;
    private long sleepMs = 200;
    private List<String> defaultTables = Arrays.asList(
            "base_trademark", "base_dic", "favor_info", "cart_info",
            "comment_info", "order_info", "order_detail", "payment_info"
    );

    public String getBusinessDate() {
        return businessDate;
    }

    public void setBusinessDate(String businessDate) {
        this.businessDate = businessDate;
    }

    public int getMaxUserId() {
        return maxUserId;
    }

    public void setMaxUserId(int maxUserId) {
        this.maxUserId = maxUserId;
    }

    public int getMaxSkuId() {
        return maxSkuId;
    }

    public void setMaxSkuId(int maxSkuId) {
        this.maxSkuId = maxSkuId;
    }

    public long getSleepMs() {
        return sleepMs;
    }

    public void setSleepMs(long sleepMs) {
        this.sleepMs = sleepMs;
    }

    public List<String> getDefaultTables() {
        return defaultTables;
    }

    public void setDefaultTables(List<String> defaultTables) {
        this.defaultTables = defaultTables;
    }
}
