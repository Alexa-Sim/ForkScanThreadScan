class SamplingForkSkipList implements Runnable {
    private static final int NUM_ITERATIONS = 50;
    private int numThreads;
    private static final SamplingForkScan forkScan = new SamplingForkScan();
    private static final SkipList skipList = new SkipList(); // Using SkipList instead
    
    public SamplingForkSkipList(int numThreads) {
        this.numThreads = numThreads;
    }
    
    public static void reset() {
        forkScan.forkAndScan();
    }
    
    public static void main(String[] args) {
        int N = Integer.parseInt(args[0]);
        SamplingForkSkipList instance = new SamplingForkSkipList(N);
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
        
        System.out.println("(" + N + ") SkipList, Method: SamplingForkScan, Time: " + latency + " ms, Memory: " + memory + " KB");
    }
    
    @Override
    public void run() {
        for (int i = 0; i < NUM_ITERATIONS; i++) {
            Object obj = new Object();
            int key = obj.hashCode(); // Generate Integer key

            SamplingForkScan.registerObject(obj);
            skipList.insert(key); // Use SkipList
            forkScan.retire(obj);
            skipList.remove(key); // Remove from SkipList
            SamplingForkScan.unregisterObject(obj);
        }
    }
}
