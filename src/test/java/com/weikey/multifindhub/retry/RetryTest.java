package com.weikey.multifindhub.retry;

import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.RetryerBuilder;
import com.github.rholder.retry.StopStrategies;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;

@SpringBootTest
public class RetryTest {
    @Test
    void test() {
        Retryer<Integer> retryer = RetryerBuilder.<Integer>newBuilder()
                // 非正数进行重试
                .retryIfRuntimeException()
                // 设置最大执行次数3次
                .withStopStrategy(StopStrategies.stopAfterAttempt(3)).build();


        try {
            retryer.call(() -> {
                System.out.println(new Date());
                Thread.sleep(1000);
                if (1 == 1) {
                    throw new RuntimeException("test");
                }
                return 0;
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
