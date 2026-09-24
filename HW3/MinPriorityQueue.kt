/**
 * A priority queue that always returns its lowest-priority element first.
 * Each element may appear in the queue at most once at a time.
 */
interface MinPriorityQueue<T> {
    /** Returns true if the queue has no elements in it. */
    fun isEmpty(): Boolean

    /** Adds [elem] to the queue with the given [priority]. [elem] must not already be in the queue. */
    fun addWithPriority(elem: T, priority: Double)

    /** Removes and returns the lowest-priority element, or null if the queue is empty. */
    fun next(): T?

    /** Changes the priority of [elem], which must already be in the queue, to [newPriority]. */
    fun adjustPriority(elem: T, newPriority: Double)
}
