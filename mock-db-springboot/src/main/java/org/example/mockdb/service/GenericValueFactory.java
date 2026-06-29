package org.example.mockdb.service;

import org.example.mockdb.config.MockProperties;
import org.example.mockdb.model.ColumnMeta;
import org.example.mockdb.util.RandomData;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;

@Component
public class GenericValueFactory {
    private final MockProperties properties;

    public GenericValueFactory(MockProperties properties) {
        this.properties = properties;
    }

    public LinkedHashMap<String, Object> createRow(List<ColumnMeta> columns) {
        LinkedHashMap<String, Object> row = new LinkedHashMap<String, Object>();
        for (ColumnMeta column : columns) {
            if (column.isAutoIncrement()) {
                continue;
            }
            row.put(column.getName(), valueFor(column));
        }
        return row;
    }

    private Object valueFor(ColumnMeta column) {
        String name = column.getName().toLowerCase();
        String type = column.getType().toLowerCase();

        if (type.contains("datetime") || type.contains("timestamp")) {
            return Timestamp.valueOf(LocalDateTime.now());
        }
        if (type.equals("date")) {
            return java.sql.Date.valueOf(LocalDate.now());
        }
        if (type.contains("char") || type.contains("text")) {
            return stringFor(name, maxLength(type));
        }
        if (type.contains("decimal") || type.contains("double") || type.contains("float")) {
            return decimalFor(name);
        }
        if (type.contains("bigint")) {
            return longFor(name);
        }
        if (type.contains("int") || type.contains("tinyint")) {
            return intFor(name);
        }
        return stringFor(name, 50);
    }

    private String stringFor(String name, int maxLen) {
        String value;
        if (name.contains("time")) {
            value = RandomData.now();
        } else if (name.contains("date")) {
            value = properties.getBusinessDate();
        } else if (name.contains("phone") || name.contains("tel")) {
            value = RandomData.phone();
        } else if (name.contains("email")) {
            value = "mock" + System.currentTimeMillis() % 100000 + "@example.com";
        } else if (name.contains("url") || name.contains("img") || name.contains("image") || name.contains("logo")) {
            value = RandomData.imageUrl(RandomData.longRange(1, properties.getMaxSkuId()));
        } else if (name.contains("name")) {
            value = RandomData.skuName(RandomData.longRange(1, properties.getMaxSkuId()));
        } else if (name.contains("status")) {
            value = String.valueOf(RandomData.intRange(1001, 1005));
        } else if (name.startsWith("is_") || name.startsWith("is")) {
            value = String.valueOf(RandomData.intRange(0, 1));
        } else if (name.contains("type")) {
            value = String.valueOf(RandomData.intRange(1, 3));
        } else if (name.equals("id") || name.endsWith("_id") || name.endsWith("code")) {
            value = String.valueOf(RandomData.longRange(1, 999));
        } else {
            value = "mock_" + name + "_" + System.currentTimeMillis() % 100000;
        }
        return RandomData.truncate(value, maxLen);
    }

    private BigDecimal decimalFor(String name) {
        if (name.contains("discount")) {
            return RandomData.money(0.50, 0.99);
        }
        if (name.contains("fee")) {
            return RandomData.money(0, 20);
        }
        return RandomData.money(10, 999);
    }

    private Long longFor(String name) {
        if (name.contains("user")) {
            return RandomData.longRange(1, properties.getMaxUserId());
        }
        if (name.contains("sku")) {
            return RandomData.longRange(1, properties.getMaxSkuId());
        }
        if (name.contains("spu")) {
            return RandomData.longRange(1, properties.getMaxSkuId() * 2L);
        }
        if (name.startsWith("is_")) {
            return (long) RandomData.intRange(0, 1);
        }
        return RandomData.longRange(1, 9999);
    }

    private Integer intFor(String name) {
        if (name.contains("num") || name.contains("count")) {
            return RandomData.intRange(1, 5);
        }
        if (name.contains("level")) {
            return RandomData.intRange(1, 3);
        }
        if (name.startsWith("is_")) {
            return RandomData.intRange(0, 1);
        }
        return RandomData.intRange(1, 100);
    }

    private int maxLength(String type) {
        int left = type.indexOf('(');
        int right = type.indexOf(')');
        if (left > 0 && right > left) {
            try {
                String number = type.substring(left + 1, right).split(",")[0].trim();
                return Math.max(1, Integer.parseInt(number));
            } catch (Exception ignored) {
                return 100;
            }
        }
        if (type.contains("text")) {
            return 500;
        }
        return 100;
    }
}
