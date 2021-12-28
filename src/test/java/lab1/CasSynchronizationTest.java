package lab1;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@TestMethodOrder(MethodOrderer.Random.class)
public class CasSynchronizationTest {
    private final Runnable singleThreadSection = new Runnable() {
        private final AtomicBoolean threadInsideSection = new AtomicBoolean(false);

        @Override
        public void run() {
            if (threadInsideSection.getAndSet(true)) {
                throw new IllegalStateException("There is already a thread inside the section");
            }

            threadInsideSection.set(false);
        }
    };
    private final CasSynchronization monitor = new CasSynchronization();


    @Test
    void testSynchronized() {
        final int threadCount = 8;
        final Runnable testTask = () -> {
            for (int i = 0; i < 1_000_000; i++) {
                monitor.casSynchronized(singleThreadSection);
            }
        };

        final ForkJoinPool pool = new ForkJoinPool(threadCount);

        Stream.generate(() -> testTask)
                .limit(threadCount)
                .map(pool::submit)
                .collect(Collectors.toList())
                .forEach(ForkJoinTask::join);

        pool.shutdown();
    }

    @Test
    void waitError() {
        Assertions.assertThrows(IllegalMonitorStateException.class, monitor::casWait);
    }

    @Test
    void notifyError() {
        Assertions.assertThrows(IllegalMonitorStateException.class, monitor::casNotify);
    }

    @Test
    void notifyAllError() {
        Assertions.assertThrows(IllegalMonitorStateException.class, monitor::casNotifyAll);
    }

    @Test
    void producerAndConsumer() {
        final int iterations = 1_000;
        final AtomicLong available = new AtomicLong(0);
        final AtomicLong counter = new AtomicLong(0);
        final Runnable consumer = () -> {
            for (int i = 0; i < iterations; i++) {
                monitor.casSynchronized(() -> {
                    if (available.get() <= 0) {
                        monitor.casWait();
                        if (available.getAndDecrement() <= 0) {
                            throw new IllegalStateException("Cannot get");
                        }
                    }
                });
            }
        };

        final Runnable producer = () -> {
            for (int i = 0; i < iterations; i++) {
                monitor.casSynchronized(() -> {
                    available.incrementAndGet();
                    monitor.casNotify();
                });
            }
        };

        final ForkJoinTask<?> producerTask = ForkJoinPool.commonPool().submit(producer);
        final ForkJoinTask<?> consumerTask = ForkJoinPool.commonPool().submit(consumer);

        producerTask.join();
        consumerTask.join();
    }
}
