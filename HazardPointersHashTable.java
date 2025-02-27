// Implementation for HazardPointers HashTable

import java.util.concurrent.ConcurrentHashMap;

class HazardPointersHashTable implements Runnable {
    private static final int NUM_ITERATIONS = 100;
    private int numThreads;
    private static final HazardPointers hazardPointers = new HazardPointers();
    private static final ConcurrentHashMap<Object, Object> hashTable = new ConcurrentHashMap<>();
    
    public HazardPointersHashTable(int numThreads) {
        this.numThreads = numThreads;
    }
    
    public static void reset() {
        // Reset memory management for a clean run
        hazardPointers.scanAndCollect();
        hashTable.clear();
    }
    
    public static void main(String[] args) {
        int N = Integer.parseInt(args[0]);
        HazardPointersHashTable instance = new HazardPointersHashTable(N);
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
        
        System.out.println("(" + N + ") HashTable, Method: HazardPointers, Time: " + latency + " ms, Memory: " + memory + " KB");
    }
    
    @Override
    public void run() {
        for (int i = 0; i < NUM_ITERATIONS; i++) {
            Object obj = new Object();
            hazardPointers.protect(obj);
            hashTable.put(obj, obj);
            hazardPointers.retire(obj);
            hashTable.remove(obj);
            hazardPointers.unprotect(obj);
        }
    }
}