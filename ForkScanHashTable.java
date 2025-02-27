// Implementation for ForkScan HashTable

import java.util.concurrent.ConcurrentHashMap; // Importing concurrent hashmap for thread-safe operations

class ForkScanHashTable implements Runnable {
    private static final int NUM_ITERATIONS = 100; // Number of iterations each thread will execute
    private int numThreads; // Number of threads to be used
    private static final Forkscan forkScan = new Forkscan(); // ForkScan garbage collector instance
    private static final ConcurrentHashMap<Object, Object> hashTable = new ConcurrentHashMap<>(); // Thread-safe hash table

    // Constructor to initialize number of threads
    public ForkScanHashTable(int numThreads) {
        this.numThreads = numThreads;
    }
    
    public static void reset() {
        // Reset memory management for a clean run
        forkScan.forkAndScan(); // Triggers memory reclamation process
        hashTable.clear(); // Clears the hash table for a fresh start
    }
    
    public static void main(String[] args) {
        int N = Integer.parseInt(args[0]); // Parse the number of threads from command line arguments
        ForkScanHashTable instance = new ForkScanHashTable(N); // Create an instance of the hash table
        Thread[] threads = new Thread[N]; // Array to hold the threads
        
        long before = System.currentTimeMillis(); // Start measuring execution time
        
        // Initialize the threads
        for (int i = 0; i < N; i++) {
            threads[i] = new Thread(instance); // Assign the runnable instance to each thread
        }
        
        // Start all the threads
        for (int i = 0; i < N; i++) {
            threads[i].start();
        }
        
        // Wait for all threads to complete execution
        for (int i = 0; i < N; i++) {
            try {
                threads[i].join(); // Ensures main thread waits for child threads to finish
            } catch (InterruptedException ie) {
                System.err.println("Thread interrupted: " + ie.getMessage()); // Handle thread interruption
            }
        }
        
        long after = System.currentTimeMillis(); // Stop measuring execution time
        long memory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory(); // Calculate memory usage
        long latency = after - before; // Compute total execution time
        
        // Output performance metrics
        System.out.println("(" + N + ") HashTable, Method: ForkScan, Time: " + latency + " ms, Memory: " + memory + " KB");
    }
    
    @Override
    public void run() {
        // Each thread executes this method
        for (int i = 0; i < NUM_ITERATIONS; i++) {
            Object obj = new Object(); // Create a new object
            
            Forkscan.registerObject(obj); // Register object for memory tracking
            hashTable.put(obj, obj); // Insert the object into the concurrent hash table
            
            forkScan.retire(obj); // Mark object for deletion
            hashTable.remove(obj); // Remove the object from the hash table
            
            Forkscan.unregisterObject(obj); // Unregister object from ForkScan memory management
        }
    }
}
