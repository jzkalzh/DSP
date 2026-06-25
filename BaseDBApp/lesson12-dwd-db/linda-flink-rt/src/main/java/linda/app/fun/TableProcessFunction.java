package linda.app.fun;

import com.alibaba.fastjson.JSONObject;
import linda.beans.TableProcessInfo;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.util.Collector;
import org.apache.flink.util.OutputTag;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TableProcessFunction extends ProcessFunction<JSONObject, JSONObject> {

    private final OutputTag<JSONObject> hbaseTag;

    private transient Connection phoenixConn;

    private final Map<String, TableProcessInfo> tableProcessMap = new HashMap<String, TableProcessInfo>();

    private final Set<String> existsTableSet = new HashSet<String>();

    public TableProcessFunction(OutputTag<JSONObject> hbaseTag) {
        this.hbaseTag = hbaseTag;
    }

    @Override
    public void open(Configuration parameters) throws Exception {
        Class.forName("org.apache.phoenix.jdbc.PhoenixDriver");
        phoenixConn = DriverManager.getConnection("jdbc:phoenix:hadoop101,hadoop102,hadoop103:2181");

        initMeta();
    }

    private void initMeta() throws Exception {
        Class.forName("com.mysql.jdbc.Driver");

        String url = "jdbc:mysql://hadoop101:3306/linda-mall-rt"
                + "?useUnicode=true&characterEncoding=utf8"
                + "&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai";

        Connection mysqlConn = DriverManager.getConnection(url, "flink", "Flink@123456");

        String sql = "select source_table, operate_type, sink_type, sink_table, sink_columns, sink_pk, sink_extend from table_process";

        PreparedStatement ps = mysqlConn.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            TableProcessInfo info = new TableProcessInfo(
                    rs.getString("source_table"),
                    rs.getString("operate_type"),
                    rs.getString("sink_type"),
                    rs.getString("sink_table"),
                    rs.getString("sink_columns"),
                    rs.getString("sink_pk"),
                    rs.getString("sink_extend")
            );

            String key = info.getSourceTable() + ":" + info.getOperateType();
            tableProcessMap.put(key, info);

            if ("hbase".equals(info.getSinkType())) {
                if (existsTableSet.add(info.getSinkTable())) {
                    createPhoenixTable(info);
                }
            }
        }

        rs.close();
        ps.close();
        mysqlConn.close();

        System.out.println("tableProcessMap size = " + tableProcessMap.size());
    }

    private void createPhoenixTable(TableProcessInfo info) {
        String sinkTable = info.getSinkTable();
        String sinkColumns = info.getSinkColumns();
        String sinkPk = info.getSinkPk();
        String sinkExtend = info.getSinkExtend();

        if (sinkPk == null || sinkPk.length() == 0) {
            sinkPk = "id";
        }

        if (sinkExtend == null) {
            sinkExtend = "";
        }

        StringBuilder sql = new StringBuilder();
        sql.append("create table if not exists ")
                .append(sinkTable)
                .append("(");

        String[] fields = sinkColumns.split(",");

        for (int i = 0; i < fields.length; i++) {
            String field = fields[i].trim();

            if (field.equals(sinkPk)) {
                sql.append(field).append(" varchar primary key");
            } else {
                sql.append("info.").append(field).append(" varchar");
            }

            if (i < fields.length - 1) {
                sql.append(",");
            }
        }

        sql.append(") ").append(sinkExtend);

        System.out.println("Phoenix create table sql: " + sql.toString());

        PreparedStatement ps = null;

        try {
            ps = phoenixConn.prepareStatement(sql.toString());
            ps.execute();
            phoenixConn.commit();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (ps != null) {
                try {
                    ps.close();
                } catch (Exception ignored) {
                }
            }
        }
    }

    @Override
    public void processElement(JSONObject obj, Context ctx, Collector<JSONObject> out) throws Exception {
        String table = obj.getString("table");
        String type = obj.getString("type");

        String key = table + ":" + type;
        TableProcessInfo tableInfo = tableProcessMap.get(key);

        if (tableInfo == null) {
            System.out.println("No table_process config for key: " + key);
            return;
        }

        obj.put("sink_table", tableInfo.getSinkTable());

        JSONObject data = obj.getJSONObject("data");
        filterColumns(data, tableInfo.getSinkColumns());

        if ("hbase".equals(tableInfo.getSinkType())) {
    System.out.println("分流到 HBase/Phoenix，source_table="
            + table
            + ", operate_type="
            + type
            + ", sink_table="
            + tableInfo.getSinkTable()
            + ", data="
            + data.toJSONString());

    ctx.output(hbaseTag, obj);
} else {
    System.out.println("分流到 Kafka，source_table="
            + table
            + ", operate_type="
            + type
            + ", sink_topic="
            + tableInfo.getSinkTable()
            + ", data="
            + data.toJSONString());

    out.collect(obj);
}
    }

    private void filterColumns(JSONObject data, String sinkColumns) {
    if (data == null || sinkColumns == null || sinkColumns.length() == 0) {
        return;
    }

    Set<String> columns = new HashSet<String>();

    String[] arr = sinkColumns.split(",");
    for (String col : arr) {
        if (col != null && col.trim().length() > 0) {
            columns.add(col.trim());
        }
    }

    Iterator<Map.Entry<String, Object>> iterator = data.entrySet().iterator();

    while (iterator.hasNext()) {
        Map.Entry<String, Object> entry = iterator.next();

        if (!columns.contains(entry.getKey())) {
            iterator.remove();
        }
    }
}
}
