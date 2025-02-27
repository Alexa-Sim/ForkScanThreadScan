import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.*;

class PriorityForkHashTable implements Runnable {
    private static final int NUM_ITERATIONS = 100;
    private int numThreads;
    private static final PrioritySamplingForkScan forkScan = new PrioritySamplingForkScan();
    private static final ConcurrentHashMap<Integer, Object> hashTable = new ConcurrentHashMap<>();
    
    public PriorityForkHashTable(int numThreads) {
        this.numThreads = numThreads;
    }
    
    public static void reset() {
        forkScan.forkAndScan();
        hashTable.clear(); // Clear queue for a clean reset
    }
    
    public static void main(String[] args) {
        int N = Integer.parseInt(args[0]);
        PriorityForkHashTable instance = new PriorityForkHashTable(N);
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
        
        System.out.println("(" + N + ") HashTable, Method: PrioritySamplingForkScan, Time: " + latency + " ms, Memory: " + memory + " KB");
    }
    
    @Override
    public void run() {
        for (int i = 0; i < NUM_ITERATIONS; i++) {
            Object obj = new Object();
            PrioritySamplingForkScan.registerObject(obj);
            hashTable.put(obj.hashCode(), obj); // Simulated lock-free insertion
            forkScan.retire(obj);
            hashTable.remove(obj.hashCode()); // Simulated lock-free deletion
            PrioritySamplingForkScan.unregisterObject(obj);
        }
    }
}
