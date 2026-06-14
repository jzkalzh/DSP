package nefu.flink.study;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Properties;

public class MockHttpToKafkaProducer {

    public static void main(String[] args) throws Exception {

        /*
         * args[0]：mock 接口地址
         * args[1]：写入的 Kafka topic
         * args[2]：请求间隔，单位毫秒
         *
         * IDEA 里 Program arguments 可以填：
         * http://localhost:3006/batch?num=10 app_log 3000
         */
        String mockUrl = args.length > 0 ? args[0] : "http://localhost:3006/batch?num=10";
        String topic = args.length > 1 ? args[1] : "app_log";
        long intervalMs = args.length > 2 ? Long.parseLong(args[2]) : 3000L;

        Properties props = new Properties();

        props.put(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                "hadoop101:9092,hadoop102:9092,hadoop103:9092"
        );

        props.put(
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                StringSerializer.class.getName()
        );

        props.put(
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                StringSerializer.class.getName()
        );

        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.RETRIES_CONFIG, 3);
        props.put(ProducerConfig.LINGER_MS_CONFIG, 5);

        KafkaProducer<String, String> producer = new KafkaProducer<String, String>(props);

        System.out.println("========== Mock HTTP To Kafka Producer Started ==========");
        System.out.println("mockUrl = " + mockUrl);
        System.out.println("topic   = " + topic);
        System.out.println("intervalMs = " + intervalMs);

        try {
            while (true) {

                String response = httpGet(mockUrl);

                if (response == null || response.trim().length() == 0) {
                    System.out.println("HTTP response is empty");
                    Thread.sleep(intervalMs);
                    continue;
                }

                int sendCount = sendJsonToKafka(response, topic, producer);

                producer.flush();

                System.out.println("Send " + sendCount + " records to Kafka topic: " + topic);

                Thread.sleep(intervalMs);
            }
        } finally {
            producer.close();
        }
    }

    /**
     * 请求 mock 接口
     */
    private static String httpGet(String urlStr) throws Exception {

        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setRequestMethod("GET");
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(10000);

        int code = conn.getResponseCode();

        BufferedReader reader;

        if (code >= 200 && code < 300) {
            reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8)
            );
        } else {
            reader = new BufferedReader(
                    new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8)
            );
        }

        StringBuilder sb = new StringBuilder();
        String line;

        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }

        reader.close();
        conn.disconnect();

        if (code < 200 || code >= 300) {
            throw new RuntimeException("HTTP request failed, code=" + code + ", body=" + sb);
        }

        return sb.toString();
    }

    /**
     * 兼容三种返回格式：
     *
     * 1. batch 直接返回数组：
     *    [{"common":...},{"common":...}]
     *
     * 2. batch 返回对象，里面有 data/list/logs 数组：
     *    {"data":[...]}
     *
     * 3. one 返回单个对象：
     *    {"common":...}
     */
    private static int sendJsonToKafka(String response,
                                       String topic,
                                       KafkaProducer<String, String> producer) {

        Object parsed = JSON.parse(response);
        int count = 0;

        if (parsed instanceof JSONArray) {

            JSONArray arr = (JSONArray) parsed;

            for (int i = 0; i < arr.size(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                sendOne(topic, producer, obj);
                count++;
            }

        } else if (parsed instanceof JSONObject) {

            JSONObject obj = (JSONObject) parsed;

            JSONArray arr = null;

            if (obj.containsKey("data") && obj.get("data") instanceof JSONArray) {
                arr = obj.getJSONArray("data");
            } else if (obj.containsKey("list") && obj.get("list") instanceof JSONArray) {
                arr = obj.getJSONArray("list");
            } else if (obj.containsKey("logs") && obj.get("logs") instanceof JSONArray) {
                arr = obj.getJSONArray("logs");
            }

            if (arr != null) {
                for (int i = 0; i < arr.size(); i++) {
                    JSONObject item = arr.getJSONObject(i);
                    sendOne(topic, producer, item);
                    count++;
                }
            } else {
                sendOne(topic, producer, obj);
                count++;
            }

        } else {
            throw new RuntimeException("Unsupported mock response format: " + response);
        }

        return count;
    }

    private static void sendOne(String topic,
                                KafkaProducer<String, String> producer,
                                JSONObject obj) {

        String key = null;

        JSONObject common = obj.getJSONObject("common");

        if (common != null) {
            key = common.getString("mid");
        }

        String value = obj.toJSONString();

        ProducerRecord<String, String> record =
                new ProducerRecord<String, String>(topic, key, value);

        producer.send(record);
    }
}