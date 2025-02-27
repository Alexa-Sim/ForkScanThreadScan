// Implementation for ForkScan SkipList 
class ForkScanSkipList implements Runnable {
    // Define the number of iterations each thread will execute
    private static final int NUM_ITERATIONS = 50; 
    
    // Number of threads to be used in the program
    private int numThreads; 
    
    // Initialize Forkscan for memory management
    private static final Forkscan forkScan = new Forkscan(); 
    
    // Create a shared SkipList instance for all threads
    private static final SkipList skipList = new SkipList(); 
    
    // Constructor to initialize the number of threads
    public ForkScanSkipList(int numThreads) { 
        this.numThreads = numThreads;
    }
    
    // Method to reset Forkscan memory management before running a new test
    public static void reset() { 
        forkScan.forkAndScan();
    }
    
    public static void main(String[] args) {
        // Read the number of threads from the command-line argument
        int N = Integer.parseInt(args[0]); 
        
        // Create an instance of ForkScanSkipList with N threads
        ForkScanSkipList instance = new ForkScanSkipList(N); 
        
        // Create an array to store thread instances
        Thread[] threads = new Thread[N]; 
        
        // Record the start time of execution
        long before = System.currentTimeMillis(); 
        
        // Initialize threads and assign the ForkScanSkipList instance to each
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
        System.out.println("(" + N + ") SkipList, Method: ForkScan, Time: " + latency + " ms, Memory: " + memory + " KB");
    }
    
    @Override
    public void run() {
        // Execute NUM_ITERATIONS of inserting and removing elements in the SkipList
        for (int i = 0; i < NUM_ITERATIONS; i++) { 
            // Create a new object to be inserted into the SkipList
            Object obj = new Object(); 
            
            // Generate a unique key using the object's hash code
            int key = obj.hashCode(); 

            // Register the object with Forkscan for memory management
            Forkscan.registerObject(obj); 
            
            // Insert the generated key into the SkipList
            skipList.insert(key); 
            
            // Mark the object for retirement (garbage collection)
            forkScan.retire(obj); 
            
            // Search for the key in the SkipList before attempting to remove it
            if (skipList.search(key)) {  
                skipList.remove(key); // Remove the key if it exists
            }

            // Unregister the object from Forkscan after use
            Forkscan.unregisterObject(obj); 
        }
    }

}
