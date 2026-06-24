package linda.utils;

import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer;

import java.util.Properties;

public class MyKafkaUtil {

    private static final String KAFKA_BROKERS =
            "hadoop101:9092,hadoop102:9092,hadoop103:9092";

    public static FlinkKafkaConsumer<String> getKafkaSource(String topic, String groupId) {
        Properties settings = new Properties();
        settings.put("bootstrap.servers", KAFKA_BROKERS);
        settings.put("group.id", groupId);
        settings.put("auto.offset.reset", "latest");

        return new FlinkKafkaConsumer<String>(
                topic,
                new SimpleStringSchema(),
                settings
        );
    }

    public static FlinkKafkaProducer<String> getKafkaSink(String topic) {
        return new FlinkKafkaProducer<String>(
                KAFKA_BROKERS,
                topic,
                new SimpleStringSchema()
        );
    }
}
