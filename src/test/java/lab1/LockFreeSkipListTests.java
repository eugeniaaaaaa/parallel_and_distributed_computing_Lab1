package lab1;

import org.junit.jupiter.api.Test;

public class LockFreeSkipListTests extends SimpleListAbstractTests {
    @Test
    void parallelAddAndContains() {
        parallelAddAndContains(new LockFreeSkipList<>());
    }

    @Test
    void addParallelRemoveAndContains() {
        addParallelRemoveAndContains(new LockFreeSkipList<>());
    }
}
