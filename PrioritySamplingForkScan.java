import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

class PrioritySamplingForkScan {
	// 'deleteBuffer' stores retired objects before they garbage collected
    // uses AtomicReference, ensuring thread safe updates
    private static final AtomicReference<Node> deleteBuffer = new AtomicReference<>(null);
    // 'scanRequested' scan atomic flag indicating whether a 'garbage collection' scan is requested
    // trigger scan to check which objects are still in use
    private static final AtomicBoolean scanRequested = new AtomicBoolean(false);
    // 'Thread-Local<Set<>>' to store objects that each thread has registered
    // each thread maintains its own reference set to track objects in use
    private static final ThreadLocal<Set<Object>> threadLocalObjects = ThreadLocal.withInitial(HashSet::new);
    // 'snapshotSignal' uses 'CountDownLatch' to synchronize the memory snapshot process
    private static final CountDownLatch snapshotSignal = new CountDownLatch(1);
    // 'reclaimerLock' used for synchronization memory reclamation process
    private static final Object reclaimerLock = new Object();
    // 'accessCounters' 'Map' for tracking access counts of objects for sampling
    private static final Map<Object, Integer> accessCounters = new ConcurrentHashMap<>();
    // map to store priority scores for objects based on access patterns
    private static final Map<Object, Double> priorityScores = new ConcurrentHashMap<>();
    // constants for priority-based garbage collection
    private static final double DECAY_FACTOR = 0.95; // Controls how priority decreases over time
    private static final int MAX_PRIORITY = 100; // Maximum priority for objects
    private static final int MIN_PRIORITY = 1; // Minimum priority for objects
    private static final int ACCESS_WEIGHT = 5; // Weight given to access frequency in priority calculations
  // sampling interval and sample size for tracking memory access
    private static final int SAMPLING_INTERVAL = 100;
    private static final int SAMPLE_SIZE = 100;

    // node for Lock-Free Deferred Reclamation
    private static class Node {
        final Object obj;
        final Node next;
        Node(Object obj, Node next) {
            this.obj = obj;		// object marked to be retired
            this.next = next; 	// retires object to 'deleteBuffer'
        }
    }

    // marks object as actively referenced by current thread
    public static void registerObject(Object obj) {
        threadLocalObjects.get().add(obj);
    }
    // removes object from the thread's active references
    public static void unregisterObject(Object obj) {
        threadLocalObjects.get().remove(obj);
    }
    
    // 'retire' adds the object to the 'deleteBuffer'
    public void retire(Object obj) {
        Node oldHead;
        Node newHead;
        do {	// do atomic compare and swap node to insert into `deleteBuffer' to ensure lock-free insertion
            oldHead = deleteBuffer.get(); // get current head of 'deletebuffer'
            newHead = new Node(obj, oldHead);
        } while (!deleteBuffer.compareAndSet(oldHead, newHead));
        // signal scan requested to check if objects can be freed
        scanRequested.set(true);
    }
    
    // initiates the fork-and-scan phase by starting a separate process.
    public void forkAndScan() {
        startForkProcess();
    }

    // 'startForkProcess' for memory 'Snapshot' and reclamation
    // creates a 'child process' to take a memory snapshot, which scans the memory state to determine which objects are still reachable
    // parent process waits for scan, after scanning the parent process can safely reclaim unreferenced objects
    // returns a simulated PID.
    private static int startForkProcess() {
        try {	// 'new' creates 'ForkscanScanner' 'child process' 
            ProcessBuilder pb = new ProcessBuilder("java", "ForkscanScanner");
            Process child = pb.start(); // start the 'child process' 
            child.waitFor();	// wait for 'child process' 
            return 1; // Simulated process ID
        } catch (IOException | InterruptedException e) {
            //e.printStackTrace();
        	return -1;
        }
    }
    // 'consolidatePtrs' consolidates hazard pointers from all threads before triggering a scan
    //  ensures memory tracking remains up-to-date
    private static void consolidatePtrs() {
        synchronized (reclaimerLock) {
            deleteBuffer.set(null); // reset delete buffer
            // aggregate pointers from all threads 
            for (Thread t : Thread.getAllStackTraces().keySet()) {
                deleteBuffer.getAndUpdate(old -> {
                    Set<Object> threadPtrs = threadLocalObjects.get();
                    for (Object ptr : threadPtrs) {
                        old = new Node(ptr, old);
                    }
                    return old;
                });
            }
            scanRequested.set(true); // request a scan after consolidating
        }
    }

    // Reclaimer thread that waits for scan requests and triggers garbage collection
    private static void reclaimerThread() {
        while (true) {
            synchronized (reclaimerLock) {
                try {
                    reclaimerLock.wait(); // Wait until notified that a scan is needed
                } catch (InterruptedException ignored) {}
            }
            
            consolidatePtrs(); // Gather pointers before starting the scan
            
            // Interrupt all threads to ensure they reach a safe point before scanning
            for (Thread t : Thread.getAllStackTraces().keySet()) {
                t.interrupt();
            }

            // Start the forked scan process
            int pid = startForkProcess();
            if (pid == 0) {
                scanMemory();
                System.exit(0);
            }

            try {
                Thread.sleep(1000); // Delay before next scan
            } catch (InterruptedException ignored) {}
        }
    }
 
    // periodically samples memory usage by tracking object access counts
    // uses a randomized sampling strategy for efficiency and updates priority scores
    private static void sampleMemoryUsage() {
        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(SAMPLING_INTERVAL);
                } catch (InterruptedException ignored) {}
                // randomly select a subset of objects from the thread-local set

                List<Object> sampledRanges = new ArrayList<>(threadLocalObjects.get());
                Collections.shuffle(sampledRanges);
                sampledRanges = sampledRanges.subList(0, Math.min(SAMPLE_SIZE, sampledRanges.size()));
                // track memory access counts for sampled objects
                // update access counts and priority scores for sampled objects
                for (Object range : sampledRanges) {
                    int accessCount = accessCounters.getOrDefault(range, 0) + 1;
                    accessCounters.put(range, accessCount);
                    double newPriority = calculateNewPriority(priorityScores.getOrDefault(range, (double) MAX_PRIORITY), accessCount);
                    priorityScores.put(range, newPriority);
                }
            }
        }).start();
    }


    // Calculates the new priority of an object based on its access count and decay factor
    private static double calculateNewPriority(double currentPriority, int accessCount) {
        double newPriority = currentPriority + (accessCount * ACCESS_WEIGHT);
        newPriority *= DECAY_FACTOR; // Apply decay to reduce priority over time
        
        if (newPriority > MAX_PRIORITY) return MAX_PRIORITY;
        if (newPriority < MIN_PRIORITY) return MIN_PRIORITY;
        return newPriority;
    }

    // Scans memory and removes objects based on priority scores
    private static void scanMemory() {
        // Sort objects by priority, highest priority first
        List<Object> memoryRanges = new ArrayList<>(priorityScores.keySet());
        memoryRanges.sort(Comparator.comparingDouble(priorityScores::get).reversed());

        // Remove objects that are no longer needed
        for (Object range : memoryRanges) {
            if (deleteBuffer.get() != null && threadLocalObjects.get().contains(range)) {
                threadLocalObjects.get().remove(range);
            }
        }
    }
}