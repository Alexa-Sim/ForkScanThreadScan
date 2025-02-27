// Implementation for HazardPointers Lock-Free List
class HazardPointersLockFreeList implements Runnable {
    private static final int NUM_ITERATIONS = 100;
    private int numThreads;
    private static final HazardPointers hazardPointers = new HazardPointers();
    
    public HazardPointersLockFreeList(int numThreads) {
        this.numThreads = numThreads;
    }
    
    public static void reset() {
        // Reset memory management for a clean run
        hazardPointers.scanAndCollect();
    }
    
    public static void main(String[] args) {
        int N = Integer.parseInt(args[0]);
        HazardPointersLockFreeList instance = new HazardPointersLockFreeList(N);
        Thread[] threads = new Thread[N];
        
        long before = System.currentTimeMillis();
        
        for (int i = 0; i < N; i++) {
            threads[i] = new Thread(instance);
        }
        
        for (int i = 0; i < N; i++) {
            threads[i].start();
        }
        
        for (int i = 0; i < N; i++) {
            try {
                threads[i].join();
            } catch (InterruptedException ie) {
                System.err.println("Thread interrupted: " + ie.getMessage());
            }
        }
        
        long after = System.currentTimeMillis();
        long memory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        long latency = after - before;
        
        System.out.println("(" + N + ") Lock-Free List, Method: HazardPointers, Time: " + latency + " ms, Memory: " + memory + " KB");
    }
    
    @Override
    public void run() {
        for (int i = 0; i < NUM_ITERATIONS; i++) {
            Object obj = new Object();
            hazardPointers.protect(obj);
            hazardPointers.retire(obj);
            hazardPointers.unprotect(obj);
        }
    }
}