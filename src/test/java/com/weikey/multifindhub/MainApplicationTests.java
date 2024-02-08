package com.weikey.multifindhub;

import com.weikey.multifindhub.datasource.PictureDataSource;
import com.weikey.multifindhub.datasource.PostDataSource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.StopWatch;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.*;

/**
 * 主类测试
 *
 * @author wei-key
 */
@SpringBootTest
class MainApplicationTests {

    @Resource
    private PostDataSource postDataSource;

    @Resource
    private PictureDataSource pictureDataSource;

    //io线程池
    private static final ThreadPoolExecutor executor = new ThreadPoolExecutor(30, 60, 10, TimeUnit.MINUTES, new LinkedBlockingQueue<>(300));


    @Test
    void timeCalculate() {
        List<String> postInputs = Arrays.asList("java", "大厂", "程序员", "理解", "线程", "语言", "原码", "ElasticSearch", "框架", "go");
        List<String> pictureInputs = Arrays.asList("java", "大厂", "程序员", "理解", "线程", "语言", "原码", "ElasticSearch", "框架", "go");

        long sum1 = 0l;
        long sum2 = 0l;

        for (int i = 0; i < 10; i++) {
            StopWatch stopWatch = new StopWatch();
            // 开始时间
            stopWatch.start();
            postDataSource.doSearch(postInputs.get(i), 1l, 10l);
            // 结束时间
            stopWatch.stop();
            // 统计执行时间（秒）
            System.out.printf(i + ":   执行时长：%d ms.%n", stopWatch.getTotalTimeMillis()); // %n 为换行
            sum1 += stopWatch.getTotalTimeMillis();
        }

        System.out.println("avg1: " + sum1 / 10);

        System.out.println("======================================");

        for (int i = 0; i < 10; i++) {
            StopWatch stopWatch = new StopWatch();
            // 开始时间
            stopWatch.start();
            pictureDataSource.doSearch(pictureInputs.get(i), 1l, 10l);
            // 结束时间
            stopWatch.stop();
            // 统计执行时间（秒）
            System.out.printf(i + ":   执行时长：%d ms.%n", stopWatch.getTotalTimeMillis()); // %n 为换行
            sum2 += stopWatch.getTotalTimeMillis();
        }
        System.out.println("avg2: " + sum2 / 10);
    }


    @Test
    void time() {
        StopWatch stopWatch = new StopWatch();
        // 开始时间
        stopWatch.start();
        // 执行时间（1s）
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // 结束时间
        stopWatch.stop();


        // 统计执行时间（秒）
        System.out.printf("执行时长：%d 秒.%n", stopWatch.getTotalTimeMillis()); // %n 为换行

        // 开始时间
        stopWatch.start();
        // 执行时间（1s）
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // 结束时间
        stopWatch.stop();
        // 统计执行时间（秒）
        System.out.printf("执行时长：%d 秒.%n", stopWatch.getTotalTimeMillis()); // %n 为换行

    }

    @Test
    void testThreadPoolExecutor() {
        System.out.println(new Date());
        Future<String> future2 = executor.submit(() -> {
            Thread.sleep(5000);
            return new Date().toString();
        });

        try {
            System.out.println(future2.get());
            System.out.println(new Date());
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

    }



}
