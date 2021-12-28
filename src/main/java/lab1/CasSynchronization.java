package lab1;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.LockSupport;

/**
 * Implements synchronized, wait, notify and notifyAll
 */
public class CasSynchronization {
    // Holds 'true' if there is a thread inside the monitor, 'false' otherwise
    private final AtomicBoolean sync = new AtomicBoolean(false);
    // Read and write on a 'long' variable can be non-atomic when running on 32-bit processors, so  we use AtomicLong
    // On the other hand, an id of a thread does not change during it's lifetime, so 'getId()' read is thread-safe
    private final AtomicLong monitorOwnerId = new AtomicLong(-1); // -1 means no thread owns the monitor
    private final Queue<Thread> waitingThreads = new ConcurrentLinkedQueue<>();

    /**
     * @param section section of code to be executed inside the synchronized block
     */
    public void casSynchronized(Runnable section) {
        tryAcquireMonitor();
        // Lock is acquired, run the critical section
        section.run();
        sync.set(false); // release monitor
    }

    public void casWait() {
        tryReleaseMonitor();
        waitingThreads.offer(Thread.currentThread());
        LockSupport.park();
        tryAcquireMonitor();
    }

    public void casNotify() {
        checkMonitorOwner();
        LockSupport.unpark(waitingThreads.poll());
    }

    public void casNotifyAll() {
        checkMonitorOwner();
        Thread waitingThread;
        while (true) {
            waitingThread = waitingThreads.poll();
            if (waitingThread == null) break;
            LockSupport.unpark(waitingThread);
        }
    }

    private void tryAcquireMonitor() {
        while (sync.getAndSet(true)) { // If there is another thread inside monitor
            Thread.yield();
        }
        monitorOwnerId.set(Thread.currentThread().getId());
    }

    private void tryReleaseMonitor() {
        checkMonitorOwner();
        sync.set(false); // Release monitor
    }

    private void checkMonitorOwner() {
        if (monitorOwnerId.get() != Thread.currentThread().getId()) {
            throw new IllegalMonitorStateException("Thread does not own the monitor");
        }
    }
}
