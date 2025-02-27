import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.*;

// Implementation for PrioritySamplingForkScan Lock-Free List
class PriorityForkLockFreeList implements Runnable {
    // Number of iterations each thread will execute
    private static final int NUM_ITERATIONS = 100;

    // Number of threads to be used in the program
    private int numThreads;

    // Initialize PrioritySamplingForkScan for memory management and garbage collection
    private static final PrioritySamplingForkScan forkScan = new PrioritySamplingForkScan();

    // Lock-free list using ConcurrentLinkedQueue to store objects safely across multiple threads
    private static final ConcurrentLinkedQueue<Object> lockFreeList = new ConcurrentLinkedQueue<>();
    
    // Constructor to initialize the number of threads
    public PriorityForkLockFreeList(int numThreads) {
        this.numThreads = numThreads;
    }
    
    public static void reset() {
        // Perform garbage collection and memory reclamation before reset
        forkScan.forkAndScan();
        
        // Clear the lock-free queue for a fresh start
        lockFreeList.clear();
    }
    
    public static void main(String[] args) {
        // Read the number of threads from the command-line argument
        int N = Integer.parseInt(args[0]);

        // Create an instance of PriorityForkLockFreeList with N threads
        PriorityForkLockFreeList instance = new PriorityForkLockFreeList(N);

        // Create an array to hold thread instances
        Thread[] threads = new Thread[N];

        // Record the start time of execution
        long before = System.currentTimeMillis();

        // Initialize and assign threads to execute the instance of PriorityForkLockFreeList
        for (int i = 0; i < N; i++) {
            threads[i] = new Thread(instance);
        }

        // Start each thread
        for (int i = 0; i < N; i++) {
            threads[i].start();
        }

        // Wait for all threads to complete execution
        for (int i = 0; i < N; i++) {
            try {
                threads[i].join(); // Ensure main thread waits for all child threads
            } catch (InterruptedException ie) {
                System.err.println("Thread interrupted: " + ie.getMessage()); // Handle interruption errors
            }
        }

        // Record the end time of execution
        long after = System.currentTimeMillis();

        // Calculate memory usage by subtracting free memory from total memory
        long memory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

        // Calculate execution time
        long latency = after - before;

        // Print the results of execution
        System.out.println("(" + N + ") Lock-Free List, Method: PrioritySamplingForkScan, Time: " + latency + " ms, Memory: " + memory + " KB");
    }
    
    @Override
    public void run() {
        // Each thread performs NUM_ITERATIONS of insert and delete operations on the lock-free list
        for (int i = 0; i < NUM_ITERATIONS; i++) {
            // Create a new object for hazard pointer tracking
            Object obj = new Object();

            // Register the object for memory management
            PrioritySamplingForkScan.registerObject(obj);

            // Insert the object into the Lock-Free List (ConcurrentLinkedQueue)
            lockFreeList.add(obj); // Simulated lock-free insertion

            // Retire the object (mark it for garbage collection)
            forkScan.retire(obj);

            // Remove the object from the Lock-Free List (ConcurrentLinkedQueue)
            lockFreeList.remove(obj); // Simulated lock-free deletion

            // Unregister the object after it has been safely retired
            PrioritySamplingForkScan.unregisterObject(obj);
        }
    }
}
