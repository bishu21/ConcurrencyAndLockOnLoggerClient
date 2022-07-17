import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class LoggerClientTest {
    @Test
    public void testSingle() {

        // {3} process started at {7} and ended {15}
        // {2} process started at {8} and ended {12}
        // {1} process started at {12} and ended {19}

        final LoggerClient logger = new LoggerClientImpl();
        logger.start("3", 7);
        System.out.println(logger.poll());
        logger.start("2", 8);
        System.out.println(logger.poll());
        logger.start("1", 12);
        System.out.println(logger.poll());
        logger.end("2", 12);
        System.out.println(logger.poll());
        logger.end("3", 15);
        System.out.println(logger.poll());
        logger.end("1", 19);
        System.out.println(logger.poll());
        System.out.println(logger.poll());
    }

    @Test
    void testMulti() throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(100);
        final LoggerClient logger = new LoggerClientImpl();
        logger.start("3", 7);
        logger.start("2", 8);

        executorService.submit(() -> logger.poll());
        executorService.submit(() -> logger.end("3", 10));

        executorService.shutdown();
        executorService.awaitTermination(10, TimeUnit.SECONDS);
    }

    @Test
    void testOrder() throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(100);
        final LoggerClient logger = new LoggerClientImpl();

        executorService.submit(() -> logger.start("3", 7));
        Thread.sleep(1* 1000);
        executorService.submit(() -> logger.poll());
        executorService.submit(() -> logger.end("3", 10));

//        executorService.shutdown();
        executorService.awaitTermination(10, TimeUnit.SECONDS);
    }

}
