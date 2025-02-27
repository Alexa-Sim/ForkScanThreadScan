// Implementation for ForkScan SkipList 
class ForkScanSkipList implements Runnable {
    private static final int NUM_ITERATIONS = 50;
    private int numThreads;
    private static final Forkscan forkScan = new Forkscan();
    private static final SkipList skipList = new SkipList();
    
    public ForkScanSkipList(int numThreads) {
        this.numThreads = numThreads;
    }
    
    public static void reset() {
        // Reset memory management for a clean run
        forkScan.forkAndScan();
    }
    
    public static void main(String[] args) {
        int N = Integer.parseInt(args[0]);
        ForkScanSkipList instance = new ForkScanSkipList(N);
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
        
        System.out.println("(" + N + ") SkipList, Method: ForkScan, Time: " + latency + " ms, Memory: " + memory + " KB");
    }
    
    @Override
    public void run() {
        for (int i = 0; i < NUM_ITERATIONS; i++) {
            Object obj = new Object();
            int key = obj.hashCode();

            Forkscan.registerObject(obj);
            skipList.insert(key);
            forkScan.retire(obj);
            
            if (skipList.search(key)) { // Ensure key exists before removing
                skipList.remove(key);
            }

            Forkscan.unregisterObject(obj);
        }
    }

}
