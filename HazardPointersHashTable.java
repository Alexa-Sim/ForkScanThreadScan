// Implementation for HazardPointers HashTable

import java.util.concurrent.ConcurrentHashMap;

class HazardPointersHashTable implements Runnable {
    // Define the number of iterations each thread will execute
    private static final int NUM_ITERATIONS = 100;
    
    // Number of threads to be used in the program
    private int numThreads;
    
    // Initialize HazardPointers for safe memory reclamation in concurrent environments
    private static final HazardPointers hazardPointers = new HazardPointers();
    
    // Create a concurrent hash table to store objects in a thread-safe manner
    private static final ConcurrentHashMap<Object, Object> hashTable = new ConcurrentHashMap<>();
    
    // Constructor to initialize the number of threads
    public HazardPointersHashTable(int numThreads) {
        this.numThreads = numThreads;
    }
    
    // Method to reset the memory management and clear the hash table
    public static void reset() {
        hazardPointers.scanAndCollect(); // Perform garbage collection for retired objects
        hashTable.clear(); // Clear all stored objects from the hash table
    }
    
    public static void main(String[] args) {
        // Read the number of threads from the command-line argument
        int N = Integer.parseInt(args[0]);
        
        // Create an instance of HazardPointersHashTable with N threads
        HazardPointersHashTable instance = new HazardPointersHashTable(N);
        
        // Create an array to store thread instances
        Thread[] threads = new Thread[N];
        
        // Record the start time of execution
        long before = System.currentTimeMillis();
        
        // Initialize threads and assign the HazardPointersHashTable instance to each
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
                threads[i].join(); // Ensure the main thread waits for all child threads to finish
            } catch (InterruptedException ie) {
                System.err.println("Thread interrupted: " + ie.getMessage()); // Handle interruptions
            }
        }
        
        // Record the end time of execution
        long after = System.currentTimeMillis();
        
        // Calculate memory usage by subtracting free memory from total memory
        long memory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        
        // Calculate execution time
        long latency = after - before;
        
        // Print the results of execution
        System.out.println("(" + N + ") HashTable, Method: HazardPointers, Time: " + latency + " ms, Memory: " + memory + " KB");
    }
    
    @Override
    public void run() {
        // Execute NUM_ITERATIONS of inserting and removing elements in the hash table
        for (int i = 0; i < NUM_ITERATIONS; i++) {
            // Create a new object to be inserted into the hash table
            Object obj = new Object();
            
            // Protect the object using hazard pointers to prevent unsafe deallocation
            hazardPointers.protect(obj);
            
            // Insert the object into the concurrent hash table
            hashTable.put(obj, obj);
            
            // Mark the object for retirement (garbage collection)
            hazardPointers.retire(obj);
            
            // Remove the object from the hash table
            hashTable.remove(obj);
            
            // Unprotect the object after it has been safely removed
            hazardPointers.unprotect(obj);
        }
    }
}
