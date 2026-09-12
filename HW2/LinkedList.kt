/**
 * A generic doubly linked list ADT supporting insertion and removal
 * at both ends in constant time.
 *
 * @param T the type of element stored in the list
 */
interface LinkedList<T> {
    /** Inserts [data] as the new front (head) element of the list. */
    fun pushFront(data: T)

    /** Inserts [data] as the new back (tail) element of the list. */
    fun pushBack(data: T)

    /** Removes and returns the front element, or null if the list is empty. */
    fun popFront(): T?

    /** Removes and returns the back element, or null if the list is empty. */
    fun popBack(): T?

    /** Returns the front element without removing it, or null if the list is empty. */
    fun peekFront(): T?

    /** Returns the back element without removing it, or null if the list is empty. */
    fun peekBack(): T?

    /** Returns true if the list has no elements. */
    fun isEmpty(): Boolean
}