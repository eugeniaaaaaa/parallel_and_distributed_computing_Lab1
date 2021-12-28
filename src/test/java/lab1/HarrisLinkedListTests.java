package lab1;

import org.junit.jupiter.api.Test;

public class HarrisLinkedListTests extends SimpleListAbstractTests {
    @Test
    void parallelAddAndContains() {
        parallelAddAndContains(new HarrisLinkedList<>());
    }

    @Test
    void addParallelRemoveAndContains() {
        addParallelRemoveAndContains(new HarrisLinkedList<>());
    }
}
