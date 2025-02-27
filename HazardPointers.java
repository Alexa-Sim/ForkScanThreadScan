import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

class HazardPointers {
	// 'MAX_HAZARD_POINTERS' per thread defines the upper limit on the number of objects a thread can protect at a time
    private static final int MAX_HAZARD_POINTERS = 128;
    // 'ThreadLocal' 'hazardPointers' to tracks its own 'protected objects'
    private static final ThreadLocal<Set<Object>> hazardPointers = ThreadLocal.withInitial(HashSet::new);
    // 'ConcurrentLinkedQueue' 'retiredObjects' list stores objects that have been 'retired' but 'cannot be deleted yet'
    private static final ConcurrentLinkedQueue<Object> retiredObjects = new ConcurrentLinkedQueue<>();
    // 'retireCount' to tracks the number of objects awaiting garbage collection to determine when to trigger a 'scan'
    private static final AtomicInteger retireCount = new AtomicInteger(0);
    // 'SCAN_THRESHOLD' for garbage collection
    private static final int SCAN_THRESHOLD = 10;

    // 'protect' marks an object as in use, preventing premature deletion
    public void protect(Object obj) {
        hazardPointers.get().add(obj);
    }

    // 'unprotect' unmarks an object, allowing it to be reclaimed later
    public void unprotect(Object obj) {
        hazardPointers.get().remove(obj);
    }

    // 'retire' adds the object to the 'deleteBuffer'
    public void retire(Object obj) {
        retiredObjects.add(obj);
        retireCount.incrementAndGet();
        // triggers a scan if the threshold is exceeded
        if (retireCount.get() >= SCAN_THRESHOLD) {
            scanAndCollect();
        }
    }

    // 'scanAndCollect' scans hazard pointers and safely reclaims unreferenced objects
    public void scanAndCollect() {
        Set<Object> globalHazardPointers = new HashSet<>();
        // aggregate hazard pointers from all threads
        for (Thread t : Thread.getAllStackTraces().keySet()) {
            globalHazardPointers.addAll(hazardPointers.get());
        }

        // collect objects that are safe to delete
        Iterator<Object> iterator = retiredObjects.iterator();
        while (iterator.hasNext()) {
            Object obj = iterator.next();
            // remove and free objects not in hazard pointers
            if (!globalHazardPointers.contains(obj)) {
                iterator.remove(); // Safe to reclaim
            }
        }
        retireCount.set(retiredObjects.size());
    }
}
