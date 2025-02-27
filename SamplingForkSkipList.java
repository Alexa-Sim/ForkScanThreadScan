// Implementation of SamplingForkScan SkipList
class SamplingForkSkipList implements Runnable {
    // Define the number of iterations each thread will execute
    private static final int NUM_ITERATIONS = 50;

    // Number of threads running this instance
    private int numThreads;

    // Initialize SamplingForkScan for memory management and safe object retirement
    private static final SamplingForkScan forkScan = new SamplingForkScan();

    // Using a SkipList for efficient concurrent operations
    private static final SkipList skipList = new SkipList();
    
    // Constructor to initialize the number of threads
    public SamplingForkSkipList(int numThreads) {
        this.numThreads = numThreads;
    }
    
    public static void reset() {
        // Perform a scan to reclaim retired objects and manage memory efficiently
        forkScan.forkAndScan();
    }
    
    public static void main(String[] args) {
        // Read the number of threads from the command-line argument
        int N = Integer.parseInt(args[0]);

        // Create an instance of SamplingForkSkipList with N threads
        SamplingForkSkipList instance = new SamplingForkSkipList(N);

        // Create an array to store thread instances
        Thread[] threads = new Thread[N];

        // Record the start time of execution
        long before = System.currentTimeMillis();

        // Initialize each thread with the instance of SamplingForkSkipList
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
        System.out.println("(" + N + ") SkipList, Method: SamplingForkScan, Time: " + latency + " ms, Memory: " + memory + " KB");
    }
    
    @Override
    public void run() {
        // Each thread performs NUM_ITERATIONS insert and delete operations on the SkipList
        for (int i = 0; i < NUM_ITERATIONS; i++) {
            // Create a new object to be tracked by the memory management system
            Object obj = new Object();

            // Generate a unique integer key based on the object's hashCode
            int key = obj.hashCode(); 

            // Register the object for safe memory management
            SamplingForkScan.registerObject(obj);

            // Insert the generated key into the SkipList
            skipList.insert(key);

            // Mark the object for memory reclamation
            forkScan.retire(obj);

            // Remove the key from the SkipList
            skipList.remove(key);

            // Unregister the object after it has been retired
            SamplingForkScan.unregisterObject(obj);
        }
    }
}
