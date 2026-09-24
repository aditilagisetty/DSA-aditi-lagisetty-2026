// heap property MUST hold
// for every slot i, slotOf[items[i]] == i

class MinHeap<T> : MinPriorityQueue<T> {
    // Heap order: items[0] has the smallest priority.
    // For slot i: parent = (i - 1) / 2, left child = 2i + 1, right child = 2i + 2
    private val items = ArrayList<T>() // the tree itself

    // element -> its current priority
    // seperate so priority doesnt have to be bundled into each item
    private val priorities = HashMap<T, Double>()

    // element -> its current slot in `items` (this is what makes adjustPriority fast)
    // when dijkstras finds a shorter path to a vertex it calls adjustPriority
    // slotOf is useful so it doesnt have to scan the whole array
    private val slotOf = HashMap<T, Int>()

    override fun isEmpty(): Boolean = items.isEmpty()

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
        // priority of the element sitting in this slot (two lookups)
        // what element is in the slot
        val elem = items[slot]
        // the element's priority
        return priorities.getValue(elem)
    }

    private fun swap(a: Int, b: Int) {
        // 1. Swap items[a] and items[b]
        val temp = items[a]
        items[a] = items[b]
        items[b] = temp
        // 2. Update slotOf for BOTH elements to their new slots
        slotOf[items[a]] = a
        slotOf[items[b]] = b
    }

    // u can only add to bottom of tree
    // but priority may be smaller than its parent which breaks heap property
    private fun siftUp(start: Int) {
        var i = start
        while (i > 0) {
            val parent = (i - 1) / 2 # can be used for left and right child bc the remainder get thrown away
            if (priorityAt(i) >= priorityAt(parent)) {
                break
            }
            swap(i, parent)
            i = parent
        }
    }

    private fun siftDown(start: Int) {
        var i = start
        while (true){
            val left = 2 * i + 1
            val right = 2 * i + 2
            var smallest = i

            if (left < items.size && priorityAt(left) < priorityAt(smallest)){
                smallest = left
            }

            if (right < items.size && priorityAt(right) < priorityAt(smallest)){
                smallest = right
            }

            if (smallest == i){
                break
            }

            swap(i, smallest)
            i = smallest

        }
    }
}
