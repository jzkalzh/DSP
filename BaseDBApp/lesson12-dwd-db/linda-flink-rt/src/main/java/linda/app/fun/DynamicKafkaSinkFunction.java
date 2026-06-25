package linda.app.fun;

import com.alibaba.fastjson.JSONObject;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

import java.util.Properties;

public class DynamicKafkaSinkFunction extends RichSinkFunction<JSONObject> {

    private transient KafkaProducer<String, String> producer;

    @Override
    public void open(Configuration parameters) {
        System.out.println("DynamicKafkaSinkFunction open");

        Properties props = new Properties();
        props.put("bootstrap.servers", "hadoop101:9092,hadoop102:9092,hadoop103:9092");
        props.put("acks", "1");
        props.put("retries", "3");
        props.put("linger.ms", "0");
        props.put("batch.size", "16384");
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        producer = new KafkaProducer<String, String>(props);
    }

    @Override
    public void invoke(JSONObject value, Context context) {
        System.out.println("DynamicKafkaSinkFunction invoke: " + value.toJSONString());

        String topic = value.getString("sink_table");

        if (topic == null || topic.trim().length() == 0) {
            System.out.println("fact data has no sink_table: " + value.toJSONString());
            return;
        }

        JSONObject dataObj = value.getJSONObject("data");

        if (dataObj == null) {
            System.out.println("fact data has no data field: " + value.toJSONString());
            return;
        }

        String data = dataObj.toJSONString();

        producer.send(new ProducerRecord<String, String>(topic, data), new Callback() {
            @Override
            public void onCompletion(RecordMetadata metadata, Exception exception) {
                if (exception != null) {
                    System.out.println("send fact data failed, topic=" + topic + ", data=" + data);
                    exception.printStackTrace();
                } else {
                    System.out.println("send fact data success, topic=" + metadata.topic()
                            + ", partition=" + metadata.partition()
                            + ", offset=" + metadata.offset()
                            + ", data=" + data);
                }
            }
        });

        producer.flush();
    }

    @Override
    public void close() {
        if (producer != null) {
            producer.close();
        }
    }
}
