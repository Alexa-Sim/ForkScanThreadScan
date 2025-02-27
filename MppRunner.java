import java.util.*;
import java.io.*;
import java.util.concurrent.*;

// Main class for execution on a multicore server
// Do not submit this class! It's only for execution.
class MppRunner {
	public static void main(String[] args) {
        int[] threadCounts = {1,2,4,8,12,20,30,40,60,80,100};
        
        System.out.println("Running benchmarks using structure-specific MyProg implementations:");
     
        System.out.println("FreeList");
    for (int threads : threadCounts) {
        ThreadScanLockFreeList.reset();
        ThreadScanLockFreeList.main(new String[]{Integer.toString(threads)});
    }
    /*
    for (int threads : threadCounts) {
        ForkScanLockFreeList.reset();
        ForkScanLockFreeList.main(new String[]{Integer.toString(threads)});
    }
    
    for (int threads : threadCounts) {
        HazardPointersLockFreeList.reset();
        HazardPointersLockFreeList.main(new String[]{Integer.toString(threads)});
    }
    
    for (int threads : threadCounts) {
    	SamplingForkLockFreeList.reset();
    	SamplingForkLockFreeList.main(new String[]{Integer.toString(threads)});
    }
    
    for (int threads : threadCounts) {
    	PriorityForkLockFreeList.reset();
    	PriorityForkLockFreeList.main(new String[]{Integer.toString(threads)});
    }
    
    System.out.println("SkipList");
    for (int threads : threadCounts) {
        ThreadScanSkipList.reset();
        ThreadScanSkipList.main(new String[]{Integer.toString(threads)});
    }
    
    for (int threads : threadCounts) {
        ForkScanSkipList.reset();
        ForkScanSkipList.main(new String[]{Integer.toString(threads)});
    }
    
    for (int threads : threadCounts) {
        HazardPointersSkipList.reset();
        HazardPointersSkipList.main(new String[]{Integer.toString(threads)});
    }
   
    for (int threads : threadCounts) {
    	SamplingForkSkipList.reset();
    	SamplingForkSkipList.main(new String[]{Integer.toString(threads)});
    }
    
    for (int threads : threadCounts) {
    	PriorityForkSkipList.reset();
    	PriorityForkSkipList.main(new String[]{Integer.toString(threads)});
    }
    
    System.out.println("HashTable");
    for (int threads : threadCounts) {
        ThreadScanHashTable.reset();
        ThreadScanHashTable.main(new String[]{Integer.toString(threads)});
    }
    
    for (int threads : threadCounts) {
        ForkScanHashTable.reset();
        ForkScanHashTable.main(new String[]{Integer.toString(threads)});
    }
    
    for (int threads : threadCounts) {
        HazardPointersHashTable.reset();
        HazardPointersHashTable.main(new String[]{Integer.toString(threads)});
    }
    
    for (int threads : threadCounts) {
    	SamplingForkHashTable.reset();
    	SamplingForkHashTable.main(new String[]{Integer.toString(threads)});
    }
   
    for (int threads : threadCounts) {
    	PriorityForkHashTable.reset();
    	PriorityForkHashTable.main(new String[]{Integer.toString(threads)});
    }
    */
}

}
