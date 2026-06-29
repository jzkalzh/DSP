package org.example.mockdb.util;

public final class SqlNameUtil {
    private SqlNameUtil() {
    }

    public static String safeName(String name) {
        if (name == null || !name.matches("[A-Za-z0-9_]+")) {
            throw new IllegalArgumentException("非法表名或字段名: " + name);
        }
        return "`" + name + "`";
    }
}
