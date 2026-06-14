package nefu.flink.study;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import org.apache.flink.api.common.functions.AggregateFunction;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.api.java.tuple.Tuple;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.core.fs.FileSystem;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.datastream.WindowedStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;
import org.apache.flink.streaming.api.functions.windowing.WindowFunction;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer;
import org.apache.flink.util.Collector;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.Properties;

public class AppLogAggWithSink {

    public static void main(String[] args) throws Exception {

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        // 源 Kafka 主题名：你后面自己填
        String sourceTopic = "app_log";

        // 处理结果写入的新 Kafka 主题
        String resultTopic = "app_log_agg_result";

        Properties kafkaProps = new Properties();
        kafkaProps.setProperty(
                "bootstrap.servers",
                "hadoop101:9092,hadoop102:9092,hadoop103:9092"
        );
        kafkaProps.setProperty("group.id", "app-log-agg-group");
        kafkaProps.setProperty("auto.offset.reset", "latest");

        FlinkKafkaConsumer<String> kafkaConsumer =
                new FlinkKafkaConsumer<String>(
                        sourceTopic,
                        new SimpleStringSchema(),
                        kafkaProps
                );

        DataStreamSource<String> kafkaSource = env.addSource(kafkaConsumer);

        SingleOutputStreamOperator<MetricEvent> metricStream =
                kafkaSource.flatMap((String line, Collector<MetricEvent> out) -> {

                    if (line == null || line.trim().length() == 0) {
                        return;
                    }

                    try {
                        JSONObject json = JSON.parseObject(line);

                        JSONObject common = json.getJSONObject("common");
                        if (common != null) {
                            String channel = common.getString("ch");
                            String brand = common.getString("ba");
                            String area = common.getString("ar");
                            String isNew = common.getString("is_new");

                            if (channel != null) {
                                out.collect(new MetricEvent("channel_count", channel, 1L, 0L));
                            }

                            if (brand != null) {
                                out.collect(new MetricEvent("brand_count", brand, 1L, 0L));
                            }

                            if (area != null) {
                                out.collect(new MetricEvent("area_count", area, 1L, 0L));
                            }

                            if (isNew != null) {
                                out.collect(new MetricEvent("new_user_count", isNew, 1L, 0L));
                            }
                        }

                        JSONObject page = json.getJSONObject("page");
                        if (page != null) {
                            String pageId = page.getString("page_id");
                            Long duringTime = page.getLong("during_time");

                            if (pageId != null) {
                                out.collect(new MetricEvent("page_pv", pageId, 1L, 0L));

                                if (duringTime != null) {
                                    out.collect(new MetricEvent("page_duration", pageId, 1L, duringTime));
                                }
                            }
                        }

                        JSONArray actions = json.getJSONArray("actions");
                        if (actions != null) {
                            for (int i = 0; i < actions.size(); i++) {
                                JSONObject action = actions.getJSONObject(i);
                                String actionId = action.getString("action_id");

                                if (actionId != null) {
                                    out.collect(new MetricEvent("action_count", actionId, 1L, 0L));
                                }
                            }
                        }

                        JSONArray displays = json.getJSONArray("displays");
                        if (displays != null) {
                            for (int i = 0; i < displays.size(); i++) {
                                JSONObject display = displays.getJSONObject(i);
                                String displayType = display.getString("display_type");

                                if (displayType != null) {
                                    out.collect(new MetricEvent("display_count", displayType, 1L, 0L));
                                }
                            }
                        }

                        JSONObject start = json.getJSONObject("start");
                        if (start != null) {
                            String entry = start.getString("entry");
                            Long loadingTime = start.getLong("loading_time");

                            if (entry != null) {
                                out.collect(new MetricEvent("start_count", entry, 1L, 0L));

                                if (loadingTime != null) {
                                    out.collect(new MetricEvent("start_loading_time", entry, 1L, loadingTime));
                                }
                            }
                        }

                        JSONObject err = json.getJSONObject("err");
                        if (err != null) {
                            String errorCode = err.getString("error_code");

                            if (errorCode != null) {
                                out.collect(new MetricEvent("error_count", errorCode, 1L, 0L));
                            }
                        }

                    } catch (Exception e) {
                        out.collect(new MetricEvent("dirty_data", "json_parse_error", 1L, 0L));
                    }

                }).returns(MetricEvent.class);

        WindowedStream<MetricEvent, Tuple, TimeWindow> windowedStream =
                metricStream
                        .keyBy("metricName", "metricKey")
                        .timeWindow(Time.seconds(10));

        SingleOutputStreamOperator<MetricResult> resultStream =
                windowedStream.aggregate(
                        new MetricAggFunction(),
                        new MetricWindowFunction()
                );

        // 控制台打印
        resultStream.print("agg_result>>");

        // 转成 JSON 字符串，方便写 Kafka 和文件
        SingleOutputStreamOperator<String> resultJsonStream =
                resultStream.map(MetricResult::toJsonString);

        // Sink 1：写入新的 Kafka 主题
        Properties producerProps = new Properties();
        producerProps.setProperty(
                "bootstrap.servers",
                "hadoop101:9092,hadoop102:9092,hadoop103:9092"
        );

        FlinkKafkaProducer<String> kafkaProducer =
                new FlinkKafkaProducer<String>(
                        resultTopic,
                        new SimpleStringSchema(),
                        producerProps
                );

        resultJsonStream.addSink(kafkaProducer);

        // Sink 2：写入 MySQL
        resultStream.addSink(new MySQLMetricSink());

        // Sink 3：写入本地文件
        // 注意：三节点集群时，文件会写到执行该 Sink 的 TaskManager 节点上
        resultJsonStream.writeAsText(
                "/opt/module/flink/output/app_log_agg_result.txt",
                FileSystem.WriteMode.OVERWRITE
        ).setParallelism(1);

        env.execute("App Log Aggregation With Kafka MySQL File Sink");
    }

    public static class MetricEvent {
        public String metricName;
        public String metricKey;
        public Long count;
        public Long value;

        public MetricEvent() {
        }

        public MetricEvent(String metricName, String metricKey, Long count, Long value) {
            this.metricName = metricName;
            this.metricKey = metricKey;
            this.count = count;
            this.value = value;
        }
    }

    public static class MetricAccumulator {
        public Long count = 0L;
        public Long valueSum = 0L;
    }

    public static class MetricResult {
        public String metricName;
        public String metricKey;
        public Long count;
        public Long valueSum;
        public Double avgValue;
        public Long windowStart;
        public Long windowEnd;

        public MetricResult() {
        }

        public MetricResult(String metricName,
                            String metricKey,
                            Long count,
                            Long valueSum,
                            Double avgValue,
                            Long windowStart,
                            Long windowEnd) {
            this.metricName = metricName;
            this.metricKey = metricKey;
            this.count = count;
            this.valueSum = valueSum;
            this.avgValue = avgValue;
            this.windowStart = windowStart;
            this.windowEnd = windowEnd;
        }

        public String toJsonString() {
            return JSON.toJSONString(this);
        }

        @Override
        public String toString() {
            return "MetricResult{" +
                    "metricName='" + metricName + '\'' +
                    ", metricKey='" + metricKey + '\'' +
                    ", count=" + count +
                    ", valueSum=" + valueSum +
                    ", avgValue=" + avgValue +
                    ", windowStart=" + windowStart +
                    ", windowEnd=" + windowEnd +
                    '}';
        }
    }

    public static class MetricAggFunction
            implements AggregateFunction<MetricEvent, MetricAccumulator, MetricAccumulator> {

        @Override
        public MetricAccumulator createAccumulator() {
            return new MetricAccumulator();
        }

        @Override
        public MetricAccumulator add(MetricEvent value, MetricAccumulator accumulator) {
            accumulator.count += value.count;
            accumulator.valueSum += value.value;
            return accumulator;
        }

        @Override
        public MetricAccumulator getResult(MetricAccumulator accumulator) {
            return accumulator;
        }

        @Override
        public MetricAccumulator merge(MetricAccumulator a, MetricAccumulator b) {
            MetricAccumulator result = new MetricAccumulator();
            result.count = a.count + b.count;
            result.valueSum = a.valueSum + b.valueSum;
            return result;
        }
    }

    public static class MetricWindowFunction
            implements WindowFunction<MetricAccumulator, MetricResult, Tuple, TimeWindow> {

        @Override
        public void apply(Tuple key,
                          TimeWindow window,
                          Iterable<MetricAccumulator> input,
                          Collector<MetricResult> out) {

            MetricAccumulator acc = input.iterator().next();

            String metricName = key.getField(0);
            String metricKey = key.getField(1);

            double avgValue = 0.0;
            if (acc.count != null && acc.count > 0) {
                avgValue = acc.valueSum * 1.0 / acc.count;
            }

            out.collect(new MetricResult(
                    metricName,
                    metricKey,
                    acc.count,
                    acc.valueSum,
                    avgValue,
                    window.getStart(),
                    window.getEnd()
            ));
        }
    }

    public static class MySQLMetricSink extends RichSinkFunction<MetricResult> {

        private Connection connection;
        private PreparedStatement ps;

        @Override
        public void open(Configuration parameters) throws Exception {
            super.open(parameters);

            Class.forName("com.mysql.cj.jdbc.Driver");

            connection = DriverManager.getConnection(
                    "jdbc:mysql://hadoop101:3306/flink_study?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai",
                    "flink",
                    "Flink@123456"
            );

            String sql = "INSERT INTO app_log_agg_result " +
                    "(metric_name, metric_key, cnt, value_sum, avg_value, window_start, window_end) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";

            ps = connection.prepareStatement(sql);
        }

        @Override
        public void invoke(MetricResult value, Context context) throws Exception {
            ps.setString(1, value.metricName);
            ps.setString(2, value.metricKey);
            ps.setLong(3, value.count);
            ps.setLong(4, value.valueSum);
            ps.setDouble(5, value.avgValue);
            ps.setLong(6, value.windowStart);
            ps.setLong(7, value.windowEnd);

            ps.executeUpdate();
        }

        @Override
        public void close() throws Exception {
            if (ps != null) {
                ps.close();
            }

            if (connection != null) {
                connection.close();
            }

            super.close();
        }
    }
}