package linda.app.dwd;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import linda.utils.DateFormatUtil;
import linda.utils.MyKafkaUtil;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.util.Collector;
import org.apache.flink.util.OutputTag;

public class BaseLogApp {

    public static void main(String[] args) throws Exception {

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        String sourceTopic = "topic_ods_base_log";
        String groupId = "topic_ods_base_log_group_fix_03";

        String pageTopic = "dwd_page_log";
        String startTopic = "dwd_start_log";
        String displayTopic = "dwd_display_log";
        String errorTopic = "dwd_error_log";

        DataStreamSource<String> kafkaDS = env.addSource(
                MyKafkaUtil.getKafkaSource(sourceTopic, groupId)
        );

        SingleOutputStreamOperator<JSONObject> jsonObjDS = kafkaDS
                .filter(new FilterFunction<String>() {
                    @Override
                    public boolean filter(String value) {
                        try {
                            JSONObject obj = JSON.parseObject(value);

                            if (obj == null) {
                                return false;
                            }

                            JSONObject common = obj.getJSONObject("common");
                            if (common == null) {
                                return false;
                            }

                            String mid = common.getString("mid");
                            if (mid == null || mid.length() == 0) {
                                return false;
                            }

                            Long ts = obj.getLong("ts");
                            if (ts == null) {
                                return false;
                            }

                            JSONObject start = obj.getJSONObject("start");
                            JSONObject page = obj.getJSONObject("page");

                            if (start == null && page == null) {
                                return false;
                            }

                            return true;
                        } catch (Exception e) {
                            return false;
                        }
                    }
                })
                .map(value -> JSON.parseObject(value));

        SingleOutputStreamOperator<JSONObject> fixedDS = jsonObjDS
                .keyBy(jsonObj -> jsonObj.getJSONObject("common").getString("mid"))
                .map(new RichMapFunction<JSONObject, JSONObject>() {

                    private transient ValueState<String> firstVisitDateState;

                    @Override
                    public void open(Configuration parameters) {
                        ValueStateDescriptor<String> descriptor =
                                new ValueStateDescriptor<String>("firstVisitDateState", String.class);
                        firstVisitDateState = getRuntimeContext().getState(descriptor);
                    }

                    @Override
                    public JSONObject map(JSONObject jsonObj) throws Exception {
                        JSONObject common = jsonObj.getJSONObject("common");

                        String isNew = common.getString("is_new");
                        Long ts = jsonObj.getLong("ts");
                        String curDate = DateFormatUtil.toDate(ts);

                        String firstVisitDate = firstVisitDateState.value();

                        if ("1".equals(isNew)) {
                            if (firstVisitDate == null) {
                                firstVisitDateState.update(curDate);
                            } else if (!firstVisitDate.equals(curDate)) {
                                common.put("is_new", "0");
                            }
                        }

                        return jsonObj;
                    }
                });

        OutputTag<String> startTag = new OutputTag<String>("start") {};
        OutputTag<String> displayTag = new OutputTag<String>("display") {};
        OutputTag<String> errorTag = new OutputTag<String>("error") {};

        SingleOutputStreamOperator<String> pageDS = fixedDS.process(
                new ProcessFunction<JSONObject, String>() {
                    @Override
                    public void processElement(JSONObject jsonObj,
                                               Context ctx,
                                               Collector<String> out) {

                        JSONObject start = jsonObj.getJSONObject("start");
                        JSONObject page = jsonObj.getJSONObject("page");
                        JSONObject err = jsonObj.getJSONObject("err");
                        JSONArray displays = jsonObj.getJSONArray("displays");

                        if (err != null) {
                            ctx.output(errorTag, jsonObj.toJSONString());
                            jsonObj.remove("err");
                        }

                        if (start != null) {
                            ctx.output(startTag, jsonObj.toJSONString());
                            return;
                        }

                        if (page != null && displays != null && displays.size() > 0) {
                            String pageId = page.getString("page_id");
                            JSONObject common = jsonObj.getJSONObject("common");
                            Long ts = jsonObj.getLong("ts");

                            for (int i = 0; i < displays.size(); i++) {
                                JSONObject displayObj = displays.getJSONObject(i);
                                displayObj.put("common", common);
                                displayObj.put("page_id", pageId);
                                displayObj.put("ts", ts);
                                ctx.output(displayTag, displayObj.toJSONString());
                            }

                            jsonObj.remove("displays");
                        }

                        if (page != null) {
                            out.collect(jsonObj.toJSONString());
                        }
                    }
                }
        );

        DataStream<String> startDS = pageDS.getSideOutput(startTag);
        DataStream<String> displayDS = pageDS.getSideOutput(displayTag);
        DataStream<String> errorDS = pageDS.getSideOutput(errorTag);

        pageDS.print("pageDS");
        startDS.print("startDS");
        displayDS.print("displayDS");
        errorDS.print("errorDS");

        pageDS.addSink(MyKafkaUtil.getKafkaSink(pageTopic));
        startDS.addSink(MyKafkaUtil.getKafkaSink(startTopic));
        displayDS.addSink(MyKafkaUtil.getKafkaSink(displayTopic));
        errorDS.addSink(MyKafkaUtil.getKafkaSink(errorTopic));

        env.execute("BaseLogApp");
    }
}
