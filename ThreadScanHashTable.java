// Implementation for ThreadScan HashTable

import java.util.concurrent.ConcurrentHashMap; // Import thread-safe hash table

// Class implementing a hash table with ThreadScan memory management
class ThreadScanHashTable implements Runnable {
    // Define the number of operations each thread will perform
    private static final int NUM_ITERATIONS = 100;

    // Number of threads to be used
    private int numThreads;

    // ThreadScan garbage collection system instance
    private static final ThreadScan threadScan = new ThreadScan();

    // ConcurrentHashMap to store key-value pairs in a thread-safe manner
    private static final ConcurrentHashMap<Object, Object> hashTable = new ConcurrentHashMap<>();

    // Constructor to initialize the number of threads
    public ThreadScanHashTable(int numThreads) {
        this.numThreads = numThreads;
    }

    // Method to reset memory management and clear the hash table
    public static void reset() {
        // Perform garbage collection using ThreadScan
        threadScan.collectGarbage();

        // Clear all entries from the hash table
        hashTable.clear();
    }

    // Main method to run the benchmark
    public static void main(String[] args) {
        // Parse the number of threads from the command-line argument
        int N = Integer.parseInt(args[0]);

        // Create an instance of the ThreadScanHashTable with the given thread count
        ThreadScanHashTable instance = new ThreadScanHashTable(N);

        // Create an array to store thread objects
        Thread[] threads = new Thread[N];

        // Record the start time before execution
        long before = System.currentTimeMillis();

        // Initialize and create threads
        for (int i = 0; i < N; i++) {
            threads[i] = new Thread(instance);
        }

        // Start all threads
        for (int i = 0; i < N; i++) {
            threads[i].start();
        }

        // Wait for all threads to finish execution
        for (int i = 0; i < N; i++) {
            try {
                threads[i].join();
            } catch (InterruptedException ie) {
                // Handle exceptions when a thread is interrupted
                System.err.println("Thread interrupted: " + ie.getMessage());
            }
        }

        // Record the end time after execution
        long after = System.currentTimeMillis();

        // Calculate memory usage by subtracting free memory from total allocated memory
        long memory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

        // Compute the total execution time in milliseconds
        long latency = after - before;

        // Print performance results including execution time and memory usage
        System.out.println("(" + N + ") HashTable, Method: ThreadScan, Time: " + latency + " ms, Memory: " + memory + " KB");
    }

    // Run method executed by each thread
    @Override
    public void run() {
        // Perform multiple iterations per thread
        for (int i = 0; i < NUM_ITERATIONS; i++) {
            // Create a new object to be inserted into the hash table
            Object obj = new Object();

            // Register the object with ThreadScan for garbage collection tracking
            threadScan.registerObject(obj);

            // Insert the object into the hash table as both key and value
            hashTable.put(obj, obj);

            // Mark the object for retirement (deletion)
            threadScan.retire(obj);

            // Remove the object from the hash table
            hashTable.remove(obj);

            // Unregister the object after its usage
            threadScan.unregisterObject(obj);
        }
    }
}
