package org.example.mockdb.repository;

import org.example.mockdb.model.ColumnMeta;
import org.example.mockdb.util.SqlNameUtil;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Repository
public class MetadataRepository {
    private final JdbcTemplate jdbcTemplate;

    public MetadataRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<String> listTables() {
        return jdbcTemplate.query("SHOW TABLES", (rs, rowNum) -> rs.getString(1));
    }

    public boolean tableExists(String table) {
        return new HashSet<String>(listTables()).contains(table);
    }

    public List<ColumnMeta> describe(String table) {
        String sql = "SHOW COLUMNS FROM " + SqlNameUtil.safeName(table);
        return jdbcTemplate.query(sql, (rs, rowNum) -> new ColumnMeta(
                rs.getString("Field"),
                rs.getString("Type"),
                "YES".equalsIgnoreCase(rs.getString("Null")),
                "PRI".equalsIgnoreCase(rs.getString("Key")),
                rs.getString("Extra") != null && rs.getString("Extra").toLowerCase().contains("auto_increment")
        ));
    }

    public Map<String, Object> count(String table) {
        String sql = "SELECT COUNT(*) AS cnt FROM " + SqlNameUtil.safeName(table);
        return jdbcTemplate.queryForMap(sql);
    }
}
