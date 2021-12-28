package lab1;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class MichaelScottQueueTests {
    private static final int MAX_ELEMENT = 10_000;
    private static final int HALF_MAX_ELEMENT = MAX_ELEMENT / 2;

    @Test
    void parallelOfferAndParallelPoll() {
        final MichaelScottQueue<Integer> queue = new MichaelScottQueue<>();
        ForkJoinPool pool = new ForkJoinPool(4);

        Stream.<Runnable>of(() -> IntStream.range(0, HALF_MAX_ELEMENT).forEach(queue::offer),
                        () -> IntStream.range(HALF_MAX_ELEMENT, MAX_ELEMENT).forEach(queue::offer))
                .map(pool::submit)
                .collect(Collectors.toList())
                .forEach(ForkJoinTask::join);

        AtomicInteger counter = new AtomicInteger();
        Stream.<Runnable>of(() -> IntStream.range(0, HALF_MAX_ELEMENT).forEach(i -> {
                    if (queue.poll() != null) counter.incrementAndGet();
                }),
                () -> IntStream.range(HALF_MAX_ELEMENT, MAX_ELEMENT).forEach(i -> {
                    if (queue.poll() != null) counter.incrementAndGet();
                }))
                .map(pool::submit)
                .collect(Collectors.toList())
                .forEach(ForkJoinTask::join);

        Assertions.assertEquals(MAX_ELEMENT, counter.get());
    }
}
