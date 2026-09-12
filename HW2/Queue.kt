/**
 * A generic FIFO Queue ADT.
 *
 * @param T the type of element stored in the queue
 */
interface Queue<T> {
    /** Adds [data] to the back of the queue. */
    fun enqueue(data: T)

    /** Removes and returns the front element, or null if the queue is empty. */
    fun dequeue(): T?

    /** Returns the front element without removing it, or null if the queue is empty. */
    fun peek(): T?

    /** Returns true if the queue has no elements. */
    fun isEmpty(): Boolean
}