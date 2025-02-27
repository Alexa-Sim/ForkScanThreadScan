// Implementation for ThreadScan SkipList

class ThreadScanSkipList implements Runnable {
    private static final int NUM_ITERATIONS = 50;
    private int numThreads;
    private static final ThreadScan threadScan = new ThreadScan();
    private static final SkipList skipList = new SkipList();
    
    public ThreadScanSkipList(int numThreads) {
        this.numThreads = numThreads;
    }
    
    public static void reset() {
        // Reset memory management for a clean run
        threadScan.collectGarbage();
    }
    
    public static void main(String[] args) {
        int N = Integer.parseInt(args[0]);
        ThreadScanSkipList instance = new ThreadScanSkipList(N);
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
        
        System.out.println("(" + N + ") SkipList, Method: ThreadScan, Time: " + latency + " ms, Memory: " + memory + " KB");
    }
    
    @Override
    public void run() {
        for (int i = 0; i < NUM_ITERATIONS; i++) {
            Object obj = new Object();
            int key = obj.hashCode();  // Use a Comparable key instead of raw Object

            ThreadScan.registerObject(obj);
            skipList.insert(key);  // Insert an Integer instead of Object
            threadScan.retire(obj);
            skipList.remove(key);
            ThreadScan.unregisterObject(obj);
        }
    }
}
