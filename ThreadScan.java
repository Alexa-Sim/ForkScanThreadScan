import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReferenceArray;

class ThreadScan {
	// 'deleteBuffer' stores retired objects before they garbage collected
    // uses AtomicReferenceArray, ensuring thread safe updates
    private static final AtomicReferenceArray<Object> deleteBuffer = new AtomicReferenceArray<>(10000);
    // 'bufferIndex' tracks the next available index in `deleteBuffer'
    // ensures atomic updates for concurrent thread access
    private static final AtomicInteger bufferIndex = new AtomicInteger(0);
    // 'scanLatch' to coordinate thread scanning
    // uses synchronization primitive
    // ensures all threads complete scanning before proceeding to garbage collection
    private static final CountDownLatch scanLatch = new CountDownLatch(1);
    // 'Thread-Local<Set<>>' to store objects that each thread has registered
    // each thread maintains its own reference set to track objects in use
    private static final ThreadLocal<Set<Object>> threadLocalObjects = ThreadLocal.withInitial(HashSet::new);

    // marks object as actively referenced by current thread
    public static void registerObject(Object obj) {
        threadLocalObjects.get().add(obj);
    }
   
    // removes object from the thread's active references
    public static void unregisterObject(Object obj) {
        threadLocalObjects.get().remove(obj);
    }
    
    // 'retire' adds the object to the 'deleteBuffer'
    public void retire(Object obj) {
        int index = bufferIndex.getAndIncrement();
        // if buffer is full, reset index and collect garbage
        if (index >= deleteBuffer.length()) {
            bufferIndex.set(0);
            collectGarbage();
        } else {
        	// otherwise store object in buffer for future cleanup
            deleteBuffer.set(index, obj);
        }
    }

    // 'collectGarbage' performs the ThreadScan garbage collection:
    // sorts the 'delete buffer'
    // signals all threads to scan their local stacks
    // waits for all threads to complete scanning
    // frees objects that are no longer referenced
    public void collectGarbage() {
        // copy and sort delete buffer
        Object[] bufferCopy = new Object[bufferIndex.get()];
        for (int i = 0; i < bufferCopy.length; i++) {
            bufferCopy[i] = deleteBuffer.get(i);
        }
        // sorting by memory address
        Arrays.sort(bufferCopy, Comparator.comparingInt(System::identityHashCode));

        // signal all threads to scan their local objects
        scanLatch.countDown();

        // wait for all threads to complete scanning
        try { scanLatch.await(); } catch (InterruptedException ignored) {}

        // frees unreferenced objects
        for (int i = 0; i < bufferCopy.length; i++) {
            if (!isObjectReferenced(bufferCopy[i], bufferCopy)) {
            	// clear from delete buffer
                deleteBuffer.set(i, null);
            }
        }
    }
    
    // checks if an object is still being referenced by any thread
    // uses 'binary search' on 'sorted buffer'
    private boolean isObjectReferenced(Object obj, Object[] sortedBuffer) {
        return Arrays.binarySearch(sortedBuffer, obj, Comparator.comparingInt(System::identityHashCode)) >= 0;
    }
}
