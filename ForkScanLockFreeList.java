// Implementation for ForkScan Lock-Free List
class ForkScanLockFreeList implements Runnable {
    private static final int NUM_ITERATIONS = 100; // Number of iterations each thread performs
    private int numThreads; // Number of threads to be used
    private static final Forkscan forkScan = new Forkscan(); // Forkscan garbage collection instance
    
    public ForkScanLockFreeList(int numThreads) {
        // Constructor to initialize the number of threads
        this.numThreads = numThreads;
    }
    
    public static void reset() {
        // Reset memory management for a clean run
        forkScan.forkAndScan();
    }
    
    public static void main(String[] args) {
        // Parse the number of threads from the command line argument
        int N = Integer.parseInt(args[0]); 
        ForkScanLockFreeList instance = new ForkScanLockFreeList(N);
        Thread[] threads = new Thread[N]; // Create an array to store N threads
        
        long before = System.currentTimeMillis(); // Record the start time
        
        // Initialize and assign each thread to run the instance of ForkScanLockFreeList
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
                threads[i].join(); // Ensure the main thread waits for this thread to finish
            } catch (InterruptedException ie) {
                System.err.println("Thread interrupted: " + ie.getMessage()); // Handle potential interruptions
            }
        }
        
        long after = System.currentTimeMillis(); // Record the end time
        long memory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory(); // Calculate memory usage
        long latency = after - before; // Calculate execution time
        
        // Print the results: number of threads, execution time, and memory usage
        System.out.println("(" + N + ") Lock-Free List, Method: ForkScan, Time: " + latency + " ms, Memory: " + memory + " KB");
    }
    
    @Override
    public void run() {
        // Each thread performs NUM_ITERATIONS of the following operations
        for (int i = 0; i < NUM_ITERATIONS; i++) {
            Object obj = new Object(); // Create a new object
            Forkscan.registerObject(obj); // Register the object with Forkscan
            forkScan.retire(obj); // Retire the object, indicating it's no longer needed
            Forkscan.unregisterObject(obj); // Unregister the object to free memory
        }
    }
}
