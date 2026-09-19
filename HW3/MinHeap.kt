class MinHeap<T> : MinPriorityQueue<T> {
    // Heap order: items[0] has the smallest priority.
    // For slot i: parent = (i - 1) / 2, left child = 2i + 1, right child = 2i + 2
    private val items = ArrayList<T>()

    // element -> its current priority
    private val priorities = HashMap<T, Double>()

    // element -> its current slot in `items` (this is what makes adjustPriority fast)
    private val slotOf = HashMap<T, Int>()

    override fun isEmpty(): Boolean {
        // TODO: one line
        TODO()
    }

    override fun addWithPriority(elem: T, priority: Double) {
        // 1. Reject an element that is already in the queue (why would a duplicate break slotOf?)
        // 2. Add elem to the END of items
        // 3. Record its priority in `priorities`
        // 4. Record its slot in `slotOf`  (hint: it's the last index)
        // 5. siftUp from that slot
        TODO()
    }

    override fun next(): T? {
        // 1. If the heap is empty, return null
        // 2. Remember the root (items[0]) -- this is what you'll return
        // 3. Swap the root with the LAST slot, then remove the last slot from `items`
        // 4. Remove the old root from `priorities` and `slotOf`
        // 5. If anything is left, siftDown from slot 0   (what if only 1 element was there?)
        // 6. Return the old root
        TODO()
    }

    override fun adjustPriority(elem: T, newPriority: Double) {
        // 1. Find elem's slot in `slotOf`. If it isn't there, decide: throw, or ignore?
        // 2. Save the OLD priority, then store the new one in `priorities`
        // 3. New priority lower than old?  -> siftUp from its slot
        //    Otherwise                     -> siftDown from its slot
        TODO()
    }

    // ---- helpers ----

    private fun priorityAt(slot: Int): Double {
        // TODO: priority of the element sitting in this slot (two lookups)
        TODO()
    }

    private fun swap(a: Int, b: Int) {
        // 1. Swap items[a] and items[b]
        // 2. Update slotOf for BOTH elements to their new slots
        //    (if you forget this, everything breaks in confusing ways)
        TODO()
    }

    private fun siftUp(start: Int) {
        // var i = start
        // While i has a parent (i > 0):
        //   - compute the parent slot
        //   - if priorityAt(i) is NOT smaller than priorityAt(parent) -> stop
        //   - otherwise swap(i, parent) and set i = parent
        TODO()
    }

    private fun siftDown(start: Int) {
        // var i = start
        // Loop forever:
        //   - compute left and right child slots
        //   - smallest = i
        //   - if left EXISTS (left < items.size) and its priority < priority of smallest -> smallest = left
        //   - if right EXISTS and its priority < priority of smallest -> smallest = right
        //     (compare against `smallest`, not `i`, so you end up with the min of all three)
        //   - if smallest == i -> stop
        //   - otherwise swap(i, smallest) and set i = smallest
        TODO()
    }
}
