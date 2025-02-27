import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.*;

// Implementation of SamplingForkScan HashTable
class SamplingForkHashTable implements Runnable {
    // Define the number of iterations each thread will execute
    private static final int NUM_ITERATIONS = 100;

    // Number of threads running this instance
    private int numThreads;

    // Initialize SamplingForkScan for memory management and safe object retirement
    private static final SamplingForkScan forkScan = new SamplingForkScan();

    // ConcurrentHashMap is used as a thread-safe HashTable implementation
    private static final ConcurrentHashMap<Integer, Object> hashTable = new ConcurrentHashMap<>();
    
    // Constructor to initialize the number of threads
    public SamplingForkHashTable(int numThreads) {
        this.numThreads = numThreads;
    }
    
    public static void reset() {
        // Perform a scan to reclaim retired objects and manage memory efficiently
        forkScan.forkAndScan();
        
        // Clear the HashTable for a fresh start
        hashTable.clear();
    }
    
    public static void main(String[] args) {
        // Read the number of threads from the command-line argument
        int N = Integer.parseInt(args[0]);

        // Create an instance of SamplingForkHashTable with N threads
        SamplingForkHashTable instance = new SamplingForkHashTable(N);

        // Create an array to store thread instances
        Thread[] threads = new Thread[N];

        // Record the start time of execution
        long before = System.currentTimeMillis();

        // Initialize each thread with the instance of SamplingForkHashTable
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
        System.out.println("(" + N + ") HashTable, Method: SamplingForkScan, Time: " + latency + " ms, Memory: " + memory + " KB");
    }
    
    @Override
    public void run() {
        // Each thread performs NUM_ITERATIONS insert and delete operations on the HashTable
        for (int i = 0; i < NUM_ITERATIONS; i++) {
            // Create a new object to be tracked by the memory management system
            Object obj = new Object();
            
            // Register the object for safe memory management
            SamplingForkScan.registerObject(obj);

            // Insert the object into the HashTable using its hash code as the key (concurrent lock-free insertion)
            hashTable.put(obj.hashCode(), obj);

            // Mark the object for memory reclamation
            forkScan.retire(obj);

            // Remove the object from the HashTable using its hash code as the key (concurrent lock-free deletion)
            hashTable.remove(obj.hashCode());

            // Unregister the object after it has been retired
            SamplingForkScan.unregisterObject(obj);
        }
    }
}
