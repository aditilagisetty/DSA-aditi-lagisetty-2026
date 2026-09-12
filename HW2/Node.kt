/**
 * A single node in a doubly linked list.
 *
 * @param T the type of value stored in the node
 * @property data the value stored in this node
 */
class Node<T>(var data: T) {
    /** Reference to the next node in the list, or null if this is the tail. */
    var next: Node<T>? = null

    /** Reference to the previous node in the list, or null if this is the head. */
    var prev: Node<T>? = null
}