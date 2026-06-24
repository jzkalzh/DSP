package linda.app.fun;

import com.alibaba.fastjson.JSONObject;
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;
import org.apache.flink.configuration.Configuration;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.Map;
import java.util.Set;

public class DimSinkFunction extends RichSinkFunction<JSONObject> {

    private transient Connection conn;

    @Override
    public void open(Configuration parameters) throws Exception {
        Class.forName("org.apache.phoenix.jdbc.PhoenixDriver");
        conn = DriverManager.getConnection("jdbc:phoenix:hadoop101,hadoop102,hadoop103:2181");
    }

    @Override
    public void invoke(JSONObject value, Context context) throws Exception {
        String sinkTable = value.getString("sink_table");
        JSONObject data = value.getJSONObject("data");

        if (sinkTable == null || data == null || data.isEmpty()) {
            return;
        }

        String upsertSql = buildUpsertSql(sinkTable, data);

        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(upsertSql);

            int index = 1;
            for (Map.Entry<String, Object> entry : data.entrySet()) {
                Object v = entry.getValue();
                ps.setString(index++, v == null ? null : String.valueOf(v));
            }

            ps.executeUpdate();
            conn.commit();

            System.out.println("Phoenix upsert success: " + upsertSql);
        } finally {
            if (ps != null) {
                ps.close();
            }
        }
    }

    private String buildUpsertSql(String sinkTable, JSONObject data) {
        Set<String> columns = data.keySet();

        StringBuilder fields = new StringBuilder();
        StringBuilder values = new StringBuilder();

        int i = 0;
        for (String column : columns) {
            fields.append(column);
            values.append("?");

            if (i < columns.size() - 1) {
                fields.append(",");
                values.append(",");
            }

            i++;
        }

        return "upsert into " + sinkTable + "(" + fields + ") values(" + values + ")";
    }

    @Override
    public void close() throws Exception {
        if (conn != null) {
            conn.close();
        }
    }
}
