// Implementation for ThreadScan HashTable

import java.util.concurrent.ConcurrentHashMap;

class ThreadScanHashTable implements Runnable {
    private static final int NUM_ITERATIONS = 100;
    private int numThreads;
    private static final ThreadScan threadScan = new ThreadScan();
    private static final ConcurrentHashMap<Object, Object> hashTable = new ConcurrentHashMap<>();
    
    public ThreadScanHashTable(int numThreads) {
        this.numThreads = numThreads;
    }
    
    public static void reset() {
        // Reset memory management for a clean run
        threadScan.collectGarbage();
        hashTable.clear();
    }
    
    public static void main(String[] args) {
        int N = Integer.parseInt(args[0]);
        ThreadScanHashTable instance = new ThreadScanHashTable(N);
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
        
        System.out.println("(" + N + ") HashTable, Method: ThreadScan, Time: " + latency + " ms, Memory: " + memory + " KB");
    }
    
    @Override
    public void run() {
        for (int i = 0; i < NUM_ITERATIONS; i++) {
            Object obj = new Object();
            threadScan.registerObject(obj);
            hashTable.put(obj, obj);
            threadScan.retire(obj);
            hashTable.remove(obj);
            threadScan.unregisterObject(obj);
        }
    }
}