// Implementation for HazardPointers Lock-Free List
class HazardPointersLockFreeList implements Runnable {
    // Define the number of iterations each thread will execute
    private static final int NUM_ITERATIONS = 100;

    // Number of threads to be used in the program
    private int numThreads;

    // Initialize HazardPointers for safe memory management in concurrent environments
    private static final HazardPointers hazardPointers = new HazardPointers();

    // Constructor to initialize the number of threads
    public HazardPointersLockFreeList(int numThreads) {
        this.numThreads = numThreads;
    }

    // Method to reset memory management for a fresh execution
    public static void reset() {
        hazardPointers.scanAndCollect(); // Perform garbage collection on retired objects
    }

    public static void main(String[] args) {
        // Read the number of threads from the command-line argument
        int N = Integer.parseInt(args[0]);

        // Create an instance of HazardPointersLockFreeList with N threads
        HazardPointersLockFreeList instance = new HazardPointersLockFreeList(N);

        // Create an array to hold thread instances
        Thread[] threads = new Thread[N];

        // Record the start time of execution
        long before = System.currentTimeMillis();

        // Initialize threads and assign the HazardPointersLockFreeList instance to each
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
                System.err.println("Thread interrupted: " + ie.getMessage()); // Handle thread interruptions
            }
        }

        // Record the end time of execution
        long after = System.currentTimeMillis();

        // Calculate memory usage by subtracting free memory from total memory
        long memory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

        // Calculate execution time
        long latency = after - before;

        // Print the results of execution
        System.out.println("(" + N + ") Lock-Free List, Method: HazardPointers, Time: " + latency + " ms, Memory: " + memory + " KB");
    }

    @Override
    public void run() {
        // Each thread performs NUM_ITERATIONS of object registration and retirement
        for (int i = 0; i < NUM_ITERATIONS; i++) {
            // Create a new object for hazard pointer tracking
            Object obj = new Object();

            // Protect the object using hazard pointers to prevent unsafe deallocation
            hazardPointers.protect(obj);

            // Mark the object for retirement (garbage collection)
            hazardPointers.retire(obj);

            // Unprotect the object after it has been safely retired
            hazardPointers.unprotect(obj);
        }
    }
}
