/**
 * A [Stack] implementation backed by a [DoublyLinkedList].
 *
 * The front of the underlying list is treated as the top of the stack,
 * so every operation delegates directly to an O(1) list operation.
 *
 * @param T the type of element stored in the stack
 */
class LinkedListStack<T> : Stack<T> {
    private val list = DoublyLinkedList<T>()

    /** Pushes [data] onto the top of the stack (the front of the list). */
    override fun push(data: T) {
        list.pushFront(data)
    }

    /** Removes and returns the top element, or null if the stack is empty. */
    override fun pop(): T? {
        return list.popFront()
    }

    /** Returns the top element without removing it, or null if the stack is empty. */
    override fun peek(): T? {
        return list.peekFront()
    }

    /** Returns true if the stack has no elements. */
    override fun isEmpty(): Boolean {
        return list.isEmpty()
    }
}