package hk.core;

import hk.config.AppConfig;
import hk.util.ConfigUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;


@Component
public class MockTask {


    @Qualifier("getPoolExecutor")
    @Autowired
    ThreadPoolTaskExecutor poolExecutor;

    public void mainTask( ) {

        for (int i = 0; i < AppConfig.mock_count; i++) {
            //poolExecutor.execute(new Mocker());
            System.out.println("active+" + poolExecutor.getActiveCount());
              new Mocker().run();
        }
    }
}
