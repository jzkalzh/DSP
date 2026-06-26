package linda.app.dwm;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import linda.utils.MyKafkaUtil;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.common.functions.RichFilterFunction;
import org.apache.flink.api.common.state.StateTtlConfig;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.time.Time;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import java.text.SimpleDateFormat;

public class UVApp {

    public static void main(String[] args) throws Exception {

        StreamExecutionEnvironment env =
                StreamExecutionEnvironment.getExecutionEnvironment();

        env.setParallelism(1);

        String sourceTopic = "dwd_page_log";
        String sinkTopic = "topic_dwm_uv";
        String groupId = "uv_app_group_01";

        // 1. 从 Kafka 读取页面日志
        DataStreamSource<String> kafkaDS = env.addSource(
                MyKafkaUtil.getKafkaSource(sourceTopic, groupId)
        );

        // 2. 过滤非法 JSON
        SingleOutputStreamOperator<String> validJsonDS =
                kafkaDS.filter(new FilterFunction<String>() {
                    @Override
                    public boolean filter(String value) {
                        try {
                            JSON.parseObject(value);
                            return true;
                        } catch (Exception e) {
                            return false;
                        }
                    }
                });

        // 3. String 转 JSONObject
        SingleOutputStreamOperator<JSONObject> jsonObjDS =
                validJsonDS.map(JSON::parseObject);

        jsonObjDS.print("UVApp-jsonObjDS>>>");

        // 4. 按设备 id mid 分组
        KeyedStream<JSONObject, String> keyedDS =
                jsonObjDS.keyBy(jsonObj ->
                        jsonObj.getJSONObject("common").getString("mid")
                );

        // 5. 独立访客过滤
        SingleOutputStreamOperator<JSONObject> uvDS =
                keyedDS.filter(new RichFilterFunction<JSONObject>() {

                    private transient ValueState<String> lastVisitDateState;
                    private transient SimpleDateFormat dateFormat;

                    @Override
                    public void open(Configuration parameters) throws Exception {
                        dateFormat = new SimpleDateFormat("yyyy-MM-dd");

                        ValueStateDescriptor<String> stateDescriptor =
                                new ValueStateDescriptor<String>(
                                        "last-visit-date-state",
                                        String.class
                                );

                        StateTtlConfig ttlConfig = StateTtlConfig
                                .newBuilder(Time.hours(24))
                                .setUpdateType(StateTtlConfig.UpdateType.OnCreateAndWrite)
                                .build();

                        stateDescriptor.enableTimeToLive(ttlConfig);

                        lastVisitDateState =
                                getRuntimeContext().getState(stateDescriptor);
                    }

                    @Override
                    public boolean filter(JSONObject jsonObj) throws Exception {

                        JSONObject common = jsonObj.getJSONObject("common");
                        JSONObject page = jsonObj.getJSONObject("page");

                        if (common == null || page == null) {
                            return false;
                        }

                        String mid = common.getString("mid");
                        Long ts = jsonObj.getLong("ts");

                        if (mid == null || ts == null) {
                            return false;
                        }

                        // last_page_id 不为空，说明不是一次新的访问入口
                        String lastPageId = page.getString("last_page_id");

                        if (!"home".equals(lastPageId)) {
                            return false;
                        }

                        String currentDate = dateFormat.format(ts);

                        String lastVisitDate = lastVisitDateState.value();

                        // 当前设备当天第一次访问
                        if (lastVisitDate == null || !lastVisitDate.equals(currentDate)) {
                            lastVisitDateState.update(currentDate);
                            return true;
                        }

                        return false;
                    }
                });

        uvDS.print("UVApp-uvDS>>>");

        // 6. 写入 Kafka：topic_dwm_uv
        uvDS.map(obj -> obj.toJSONString())
                .addSink(MyKafkaUtil.getKafkaSink(sinkTopic));

        env.execute("UVApp");
    }
}