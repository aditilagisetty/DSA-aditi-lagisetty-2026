/**
 * A generic LIFO Stack ADT.
 *
 * @param T the type of element stored in the stack
 */
interface Stack<T> {
    /** Pushes [data] onto the top of the stack. */
    fun push(data: T)

    /** Removes and returns the top element, or null if the stack is empty. */
    fun pop(): T?

    /** Returns the top element without removing it, or null if the stack is empty. */
    fun peek(): T?

    /** Returns true if the stack has no elements. */
    fun isEmpty(): Boolean
}