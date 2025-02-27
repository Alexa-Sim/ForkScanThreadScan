import java.util.concurrent.ConcurrentLinkedQueue;

class SamplingForkLockFreeList implements Runnable {
    private static final int NUM_ITERATIONS = 100;
    private int numThreads;
    private static final SamplingForkScan forkScan = new SamplingForkScan();
    private static final ConcurrentLinkedQueue<Object> queue = new ConcurrentLinkedQueue<>();
    
    public SamplingForkLockFreeList(int numThreads) {
        this.numThreads = numThreads;
    }
    
    public static void reset() {
        forkScan.forkAndScan();
        queue.clear(); // Clear queue for a clean reset
    }
    
    public static void main(String[] args) {
        int N = Integer.parseInt(args[0]);
        SamplingForkLockFreeList instance = new SamplingForkLockFreeList(N);
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
        
        System.out.println("(" + N + ") Lock-Free List, Method: SamplingForkScan, Time: " + latency + " ms, Memory: " + memory + " KB");
    }
    
    @Override
    public void run() {
        for (int i = 0; i < NUM_ITERATIONS; i++) {
            Object obj = new Object();
            SamplingForkScan.registerObject(obj);
            queue.add(obj); // Simulated lock-free insertion
            forkScan.retire(obj);
            queue.remove(obj); // Simulated lock-free deletion
            SamplingForkScan.unregisterObject(obj);
        }
    }
}
