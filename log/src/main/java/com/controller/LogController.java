package com.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.net.InetAddress;
import java.net.UnknownHostException;

@RestController
@Slf4j
public class LogController {

    // 注入kafka生产者
    @Resource
    private KafkaTemplate<String, String> kafkaTemplate;
    // ODS层主题
    private static final String TOPIC_NAME = "ods_base_log";

    // GET方式接收参数（浏览器直接访问测试）
    @RequestMapping("/applog")
    public String getLog(String param) throws UnknownHostException {
        log.info(param);
        // 发送日志到kafka ods层
        kafkaTemplate.send(TOPIC_NAME, param);
        String ip = InetAddress.getLocalHost().getHostAddress();
        return ip + ":" + param;
    }

    // POST接收JSON格式日志（生产环境推荐）
    @PostMapping("/applog")
    public String postLog(@RequestBody String param) throws UnknownHostException {
        log.info(param);
        kafkaTemplate.send(TOPIC_NAME, param);
        String ip = InetAddress.getLocalHost().getHostAddress();
        return ip + ":" + param;
    }
}