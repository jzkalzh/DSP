package linda.app.dwd;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import linda.app.fun.DimSinkFunction;
import linda.app.fun.TableProcessFunction;
import linda.app.fun.DynamicKafkaSinkFunction;
import linda.utils.MyKafkaUtil;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.util.OutputTag;

public class BaseDBApp {

    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        String topic = "topic_ods_base_db";
        String groupId = "base_db_app_group_dynamic_favor_01";;

        DataStreamSource<String> kafkaDS = env.addSource(
                MyKafkaUtil.getKafkaSource(topic, groupId)
        );

        SingleOutputStreamOperator<JSONObject> jsonObjDS = kafkaDS
                .filter(new FilterFunction<String>() {
                    @Override
                    public boolean filter(String value) {
                        try {
                            JSON.parseObject(value);
                            return true;
                        } catch (Exception e) {
                            return false;
                        }
                    }
                })
                .map(JSON::parseObject);

        SingleOutputStreamOperator<JSONObject> filterDS = jsonObjDS
                .filter(new FilterFunction<JSONObject>() {
                    @Override
                    public boolean filter(JSONObject obj) {
                        return obj.getJSONObject("data") != null
                                && obj.getString("table") != null
                                && obj.getString("type") != null;
                    }
                });

        OutputTag<JSONObject> hbaseTag = new OutputTag<JSONObject>("hbase") {};

        SingleOutputStreamOperator<JSONObject> factDS =
                filterDS.process(new TableProcessFunction(hbaseTag));

        DataStream<JSONObject> dimDS = factDS.getSideOutput(hbaseTag);

        factDS.print("fact-kafka");
        dimDS.print("dim-hbase");

        factDS.addSink(new DynamicKafkaSinkFunction());

        dimDS.addSink(new DimSinkFunction());

        env.execute("BaseDBApp");
    }
}
