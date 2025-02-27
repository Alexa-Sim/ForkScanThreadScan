import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.*;

// Implementation of PrioritySamplingForkScan SkipList
class PriorityForkSkipList implements Runnable {
    // Define the number of iterations each thread will execute
    private static final int NUM_ITERATIONS = 50;

    // Number of threads running this instance
    private int numThreads;

    // Initialize PrioritySamplingForkScan for memory management and safe memory reclamation
    private static final PrioritySamplingForkScan forkScan = new PrioritySamplingForkScan();

    // ConcurrentSkipListSet is used as a thread-safe SkipList implementation
    private static final ConcurrentSkipListSet<Integer> skipList = new ConcurrentSkipListSet<>();
    
    // Constructor to initialize the number of threads
    public PriorityForkSkipList(int numThreads) {
        this.numThreads = numThreads;
    }
    
    public static void reset() {
        // Perform a scan to reclaim retired objects and manage memory efficiently
        forkScan.forkAndScan();
        
        // Clear the SkipList for a fresh start
        skipList.clear();
    }
    
    public static void main(String[] args) {
        // Read the number of threads from the command-line argument
        int N = Integer.parseInt(args[0]);

        // Create an instance of PriorityForkSkipList with N threads
        PriorityForkSkipList instance = new PriorityForkSkipList(N);

        // Create an array to store thread instances
        Thread[] threads = new Thread[N];

        // Record the start time of execution
        long before = System.currentTimeMillis();

        // Initialize each thread with the instance of PriorityForkSkipList
        for (int i = 0; i < N; i++) {
            threads[i] = new Thread(instance);
        }

        // Start all threads
        for (int i = 0; i < N; i++) {
            threads[i].start();
        }

        // Wait for all threads to complete execution
        for (int i = 0; i < N; i++) {
            try {
                threads[i].join(); // Ensure the main thread waits for all child threads
            } catch (InterruptedException ie) {
                System.err.println("Thread interrupted: " + ie.getMessage()); // Handle thread interruption
            }
        }

        // Record the end time of execution
        long after = System.currentTimeMillis();

        // Calculate memory usage by subtracting free memory from total memory
        long memory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

        // Calculate execution time
        long latency = after - before;

        // Print the execution results with the number of threads, execution time, and memory usage
        System.out.println("(" + N + ") SkipList, Method: PrioritySamplingForkScan, Time: " + latency + " ms, Memory: " + memory + " KB");
    }
    
    @Override
    public void run() {
        // Each thread performs NUM_ITERATIONS insert and delete operations on the SkipList
        for (int i = 0; i < NUM_ITERATIONS; i++) {
            // Create a new object to be tracked by the memory management system
            Object obj = new Object();
            
            // Generate a unique key using the hash code of the object
            int key = obj.hashCode();

            // Register the object for safe memory management
            PrioritySamplingForkScan.registerObject(obj);

            // Insert the key into the SkipList (concurrent lock-free insertion)
            skipList.add(key);

            // Mark the object for memory reclamation
            forkScan.retire(obj);

            // Remove the key from the SkipList (concurrent lock-free deletion)
            skipList.remove(key);

            // Unregister the object after it has been retired
            PrioritySamplingForkScan.unregisterObject(obj);
        }
    }
}
