/**
 * A [Queue] implementation backed by a [DoublyLinkedList].
 *
 * New elements are added at the back of the list and removed from the
 * front, so every operation delegates directly to an O(1) list operation.
 *
 * @param T the type of element stored in the queue
 */
class LinkedListQueue<T> : Queue<T> {
    private val list = DoublyLinkedList<T>()

    /** Adds [data] to the back of the queue. */
    override fun enqueue(data: T) {
        list.pushBack(data)
    }

    /** Removes and returns the front element, or null if the queue is empty. */
    override fun dequeue(): T? {
        return list.popFront()
    }

    /** Returns the front element without removing it, or null if the queue is empty. */
    override fun peek(): T? {
        return list.peekFront()
    }

    /** Returns true if the queue has no elements. */
    override fun isEmpty(): Boolean {
        return list.isEmpty()
    }
}