package linda.app.fun;

import com.alibaba.fastjson.JSONObject;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Properties;

public class DynamicKafkaSinkFunction extends RichSinkFunction<JSONObject> {

    private transient KafkaProducer<String, String> producer;

    @Override
    public void open(Configuration parameters) {
        Properties props = new Properties();
        props.put("bootstrap.servers", "hadoop101:9092,hadoop102:9092,hadoop103:9092");
        props.put("acks", "all");
        props.put("retries", "3");
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        producer = new KafkaProducer<String, String>(props);
    }

    @Override
    public void invoke(JSONObject value, Context context) {
        String topic = value.getString("sink_table");

        if (topic == null || topic.length() == 0) {
            System.out.println("fact data has no sink_table: " + value.toJSONString());
            return;
        }

        String data = value.getJSONObject("data").toJSONString();

        producer.send(new ProducerRecord<String, String>(topic, data));

        System.out.println("send fact data to kafka topic " + topic + " : " + data);
    }

    @Override
    public void close() {
        if (producer != null) {
            producer.close();
        }
    }
}
