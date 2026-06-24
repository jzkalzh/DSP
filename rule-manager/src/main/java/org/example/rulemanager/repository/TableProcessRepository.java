package org.example.rulemanager.repository;

import org.example.rulemanager.entity.TableProcess;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class TableProcessRepository {

    private final JdbcTemplate jdbcTemplate;

    public TableProcessRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<TableProcess> rowMapper = new RowMapper<TableProcess>() {
        @Override
        public TableProcess mapRow(ResultSet rs, int rowNum) throws SQLException {
            TableProcess rule = new TableProcess();
            rule.setSourceTable(rs.getString("source_table"));
            rule.setOperateType(rs.getString("operate_type"));
            rule.setSinkType(rs.getString("sink_type"));
            rule.setSinkTable(rs.getString("sink_table"));
            rule.setSinkColumns(rs.getString("sink_columns"));
            rule.setSinkPk(rs.getString("sink_pk"));
            rule.setSinkExtend(rs.getString("sink_extend"));
            return rule;
        }
    };

    public List<TableProcess> findAll(String keyword) {
        String baseSql = "select source_table, operate_type, sink_type, sink_table, sink_columns, sink_pk, sink_extend from table_process";

        if (StringUtils.hasText(keyword)) {
            String sql = baseSql + " where source_table like ? order by source_table, operate_type";
            return jdbcTemplate.query(sql, rowMapper, "%" + keyword.trim() + "%");
        }

        String sql = baseSql + " order by source_table, operate_type";
        return jdbcTemplate.query(sql, rowMapper);
    }

    public TableProcess findById(String sourceTable, String operateType) {
        String sql = "select source_table, operate_type, sink_type, sink_table, sink_columns, sink_pk, sink_extend " +
                "from table_process where source_table = ? and operate_type = ?";

        List<TableProcess> list = jdbcTemplate.query(sql, rowMapper, sourceTable, operateType);

        return list.isEmpty() ? null : list.get(0);
    }

    public void insert(TableProcess rule) {
        String sql = "insert into table_process " +
                "(source_table, operate_type, sink_type, sink_table, sink_columns, sink_pk, sink_extend) " +
                "values (?, ?, ?, ?, ?, ?, ?)";

        jdbcTemplate.update(sql,
                rule.getSourceTable(),
                rule.getOperateType(),
                rule.getSinkType(),
                rule.getSinkTable(),
                rule.getSinkColumns(),
                rule.getSinkPk(),
                rule.getSinkExtend()
        );
    }

    public void update(TableProcess rule, String oldSourceTable, String oldOperateType) {
        String sql = "update table_process set " +
                "source_table = ?, operate_type = ?, sink_type = ?, sink_table = ?, sink_columns = ?, sink_pk = ?, sink_extend = ? " +
                "where source_table = ? and operate_type = ?";

        jdbcTemplate.update(sql,
                rule.getSourceTable(),
                rule.getOperateType(),
                rule.getSinkType(),
                rule.getSinkTable(),
                rule.getSinkColumns(),
                rule.getSinkPk(),
                rule.getSinkExtend(),
                oldSourceTable,
                oldOperateType
        );
    }

    public void delete(String sourceTable, String operateType) {
        String sql = "delete from table_process where source_table = ? and operate_type = ?";
        jdbcTemplate.update(sql, sourceTable, operateType);
    }
}