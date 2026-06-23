package hk;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import hk.core.MockTask;
@SpringBootApplication
public class MockApp {
    public static void main(String[] args) {
      ConfigurableApplicationContext ctx = SpringApplication.run(MockApp.class, args);
      MockTask mockTask = ctx.getBean(MockTask.class);
      mockTask.mainTask();
    }
}
