import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.*;

class PriorityForkSkipList implements Runnable {
    private static final int NUM_ITERATIONS = 50;
    private int numThreads;
    private static final PrioritySamplingForkScan forkScan = new PrioritySamplingForkScan();
    private static final ConcurrentSkipListSet<Integer> skipList = new ConcurrentSkipListSet<>();
    
    public PriorityForkSkipList(int numThreads) {
        this.numThreads = numThreads;
    }
    
    public static void reset() {
        forkScan.forkAndScan();
        skipList.clear(); // Clear SkipList for a clean reset
    }
    
    public static void main(String[] args) {
        int N = Integer.parseInt(args[0]);
        PriorityForkSkipList instance = new PriorityForkSkipList(N);
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
        
        System.out.println("(" + N + ") SkipList, Method: PrioritySamplingForkScan, Time: " + latency + " ms, Memory: " + memory + " KB");
    }
    
    @Override
    public void run() {
        for (int i = 0; i < NUM_ITERATIONS; i++) {
            Object obj = new Object();
            int key = obj.hashCode(); // Convert Object to a valid key

            PrioritySamplingForkScan.registerObject(obj);
            skipList.add(key); // Insert using hashCode() instead of raw Object
            forkScan.retire(obj);
            skipList.remove(key); // Remove using hashCode()
            PrioritySamplingForkScan.unregisterObject(obj);
        }
    }
}
