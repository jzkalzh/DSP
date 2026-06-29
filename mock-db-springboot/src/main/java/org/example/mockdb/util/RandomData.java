package org.example.mockdb.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

public final class RandomData {
    private static final Random RANDOM = new Random();
    private static final String[] BRANDS = {"华为", "小米", "苹果", "OPPO", "vivo", "联想", "荣耀", "三星", "美的", "海尔"};
    private static final String[] GOODS = {"手机", "耳机", "平板", "电脑", "电视", "键盘", "鼠标", "手表", "冰箱", "洗衣机"};
    private static final String[] NAMES = {"张三", "李四", "王五", "赵六", "小明", "小红", "阿强", "小美", "老周", "小白"};
    private static final String[] ADDRESSES = {"北京市朝阳区", "上海市浦东新区", "广州市天河区", "深圳市南山区", "杭州市西湖区", "南京市鼓楼区"};
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private RandomData() {
    }

    public static int intRange(int min, int max) {
        if (max < min) {
            return min;
        }
        return RANDOM.nextInt(max - min + 1) + min;
    }

    public static long longRange(long min, long max) {
        if (max < min) {
            return min;
        }
        return min + Math.abs(RANDOM.nextLong()) % (max - min + 1);
    }

    public static BigDecimal money(double min, double max) {
        double value = min + RANDOM.nextDouble() * (max - min);
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP);
    }

    public static String now() {
        return LocalDateTime.now().format(DATE_TIME);
    }

    public static String plusMinutes(int minutes) {
        return LocalDateTime.now().plusMinutes(minutes).format(DATE_TIME);
    }

    public static String brand() {
        return BRANDS[intRange(0, BRANDS.length - 1)] + "_mock_" + System.currentTimeMillis() % 100000;
    }

    public static String skuName(long skuId) {
        return GOODS[(int) ((skuId - 1) % GOODS.length)] + " SKU-" + skuId;
    }

    public static String personName() {
        return NAMES[intRange(0, NAMES.length - 1)];
    }

    public static String phone() {
        return "13" + intRange(100000000, 999999999);
    }

    public static String address() {
        return ADDRESSES[intRange(0, ADDRESSES.length - 1)] + " mock路" + intRange(1, 999) + "号";
    }

    public static String imageUrl(long id) {
        return "/static/mock/sku_" + id + ".jpg";
    }

    public static String outTradeNo() {
        return "MOCK" + System.currentTimeMillis() + intRange(100, 999);
    }

    public static String dicId() {
        return "M" + (System.currentTimeMillis() % 1000000000L);
    }

    public static String truncate(String value, int maxLen) {
        if (value == null || maxLen <= 0 || value.length() <= maxLen) {
            return value;
        }
        return value.substring(0, maxLen);
    }
}
