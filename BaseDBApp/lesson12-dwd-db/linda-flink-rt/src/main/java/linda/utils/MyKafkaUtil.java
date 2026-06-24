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

        // 新消费组第一次启动时，只消费启动之后的新数据，避免读到之前手动测试的脏数据
        settings.put("auto.offset.reset", "latest");

        return new FlinkKafkaConsumer<>(
                topic,
                new SimpleStringSchema(),
                settings
        );
    }

    public static FlinkKafkaProducer<String> getKafkaSink(String topic) {
        Properties settings = new Properties();

        settings.put("bootstrap.servers", KAFKA_BROKERS);

        return new FlinkKafkaProducer<>(
                topic,
                new SimpleStringSchema(),
                settings
        );
    }
}
