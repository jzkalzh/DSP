package org.example.mockdb;

import org.example.mockdb.config.MockProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(MockProperties.class)
public class MockDbApplication {
    public static void main(String[] args) {
        SpringApplication.run(MockDbApplication.class, args);
    }
}
