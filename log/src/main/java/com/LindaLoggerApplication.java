package com;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com")
public class LindaLoggerApplication {
    public static void main(String[] args) {
        SpringApplication.run(LindaLoggerApplication.class, args);
    }
}