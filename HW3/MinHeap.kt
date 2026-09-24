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
        require(elem !in slotOf) {
            "$elem is already in the queue"
        }
        // 2. Add elem to the END of items
        items.add(elem) // adds to bottom of tree
        // 3. Record its priority in `priorities`
        priorities[elem] = priority // remember priority
        // 4. Record its slot in `slotOf` 
        slotOf[elem] = items.lastIndex // remeber where it is
        // 5. siftUp from that slot // bubble up to legal spot
        siftUp(items.lastIndex)
    }

    override fun next(): T? {
        // 1. If the heap is empty, return null
        if (isEmpty()) return null
        // 2. Remember the root (items[0]) -- this is what you'll return
        val root = items[0]
        // 3. Swap the root with the LAST slot, then remove the last slot from `items`
        swap(0, items.lastIndex)
        items.removeAt(items.lastIndex)
        // 4. Remove the old root from `priorities` and `slotOf`
        priorities.remove(root)
        slotOf.remove(root)
        // 5. If anything is left, siftDown from slot 0   (what if only 1 element was there?)
        if (items.isNotEmpty()) siftDown(0)
        // 6. Return the old root
        return root
    }

    override fun adjustPriority(elem: T, newPriority: Double) {
        // 1. Find elem's slot in `slotOf`. If it isn't there, decide: throw, or ignore?
        val slot = slotOf[elem] ?: throw IllegalArgumentException("$elem is not in the queue")
        // 2. Save the OLD priority, then store the new one in `priorities`
        val oldPriority = priorities.getValue(elem)
        priorities[elem] = newPriority

        // 3. New priority lower than old?  -> siftUp from its slot
        if (newPriority < oldPriority){
            siftUp(slot)
        }
        //    Otherwise                     -> siftDown from its slot
        else {
            siftDown(slot)
        }
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
            val parent = (i - 1) / 2 // can be used for left and right child bc the remainder get thrown away
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
