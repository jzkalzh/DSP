package org.example.mockdb.repository;

import org.example.mockdb.util.SqlNameUtil;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Repository
public class MockJdbcRepository {
    private final JdbcTemplate jdbcTemplate;

    public MockJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Number insert(String table, LinkedHashMap<String, Object> values) {
        if (values == null || values.isEmpty()) {
            throw new IllegalArgumentException("插入字段不能为空");
        }

        List<String> columns = new ArrayList<String>(values.keySet());
        StringBuilder sql = new StringBuilder();
        sql.append("INSERT INTO ").append(SqlNameUtil.safeName(table)).append(" (");
        for (int i = 0; i < columns.size(); i++) {
            if (i > 0) {
                sql.append(",");
            }
            sql.append(SqlNameUtil.safeName(columns.get(i)));
        }
        sql.append(") VALUES (");
        for (int i = 0; i < columns.size(); i++) {
            if (i > 0) {
                sql.append(",");
            }
            sql.append("?");
        }
        sql.append(")");

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql.toString(), Statement.RETURN_GENERATED_KEYS);
            int index = 1;
            for (String column : columns) {
                ps.setObject(index++, values.get(column));
            }
            return ps;
        }, keyHolder);
        return keyHolder.getKey();
    }

    public int update(String sql, Object... args) {
        return jdbcTemplate.update(sql, args);
    }
}
