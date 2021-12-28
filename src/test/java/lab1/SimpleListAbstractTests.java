package lab1;

import org.junit.jupiter.api.Assertions;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.function.IntConsumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class SimpleListAbstractTests {
    private final int maxAdded = 100_000;
    private final int maxAddedQuarter = maxAdded / 4;

    void parallelAddAndContains(SimpleList<Integer> list) {
        final ForkJoinPool pool = new ForkJoinPool(4);
        submitQuartersAndWait(list, pool, list::add);
        IntStream.range(0, maxAdded).forEach(i -> Assertions.assertTrue(list.contains(i), "Does not contain " + i));
    }

    void addParallelRemoveAndContains(SimpleList<Integer> list) {
        final ForkJoinPool pool = new ForkJoinPool(4);
        IntStream.range(0, maxAdded).forEach(list::add);
        submitQuartersAndWait(list, pool, list::remove);
        IntStream.range(0, maxAdded).forEach(i -> Assertions.assertFalse(list.contains(i)));
    }

    private void submitQuartersAndWait(SimpleList<Integer> list, ForkJoinPool pool, IntConsumer action) {
        Stream.of(listActionRunnable(list, 0, maxAddedQuarter, action),
                        listActionRunnable(list, maxAddedQuarter, maxAddedQuarter * 2, action),
                        listActionRunnable(list, maxAddedQuarter * 2, maxAddedQuarter * 3, action),
                        listActionRunnable(list, maxAddedQuarter * 3, maxAdded, action))
                .map(pool::submit) // Submit Runnable's for each range in the pool
                .collect(Collectors.toList()) // wait until all the Runnable's are submitted
                .forEach(ForkJoinTask::join); // wait until the end of the execution
    }

    private Runnable listActionRunnable(SimpleList<Integer> list, int from, int toExclusive, IntConsumer action) {
        return () -> IntStream.range(from, toExclusive).forEach(action);
    }
}
