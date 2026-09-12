/**
 * A doubly linked list implementation of the [LinkedList] ADT.
 *
 * Maintains both [head] and [tail] pointers so that every operation
 * runs in O(1) time, with no traversal required.
 *
 * @param T the type of element stored in the list
 */
class DoublyLinkedList<T> : LinkedList<T> {
    private var head: Node<T>? = null
    private var tail: Node<T>? = null

    /** Inserts [data] as the new head of the list in O(1) time. */
    override fun pushFront(data: T) {
        val newNode = Node(data)
        if (isEmpty()){
            // First node in the list: head and tail are the same node.
            head = newNode
            tail = newNode
        }
        else {
            newNode.next = head
            head!!.prev = newNode
            head = newNode
        }
    }

    /** Inserts [data] as the new tail of the list in O(1) time. */
    override fun pushBack(data: T) {
        val newNode = Node(data)
        if (isEmpty()){
            // First node in the list: head and tail are the same node.
            head = newNode
            tail = newNode
        }
        else {
            newNode.prev = tail
            tail!!.next = newNode
            tail = newNode
        }
    }

    /** Removes and returns the head element, or null if the list is empty. */
    override fun popFront(): T? {
        if (isEmpty()){
            return null
        }
        val data = head!!.data
        head = head!!.next
        if (head == null){
            // Removed the last remaining node, so the tail must be cleared too.
            tail = null
            return data
        }
        else {
            head!!.prev = null
            return data
        }
    }

    /** Removes and returns the tail element, or null if the list is empty. */
    override fun popBack(): T? {
        if (isEmpty()){
            return null
        }
        val data = tail!!.data
        tail = tail!!.prev
        if (tail ==  null){
            // Removed the last remaining node, so the head must be cleared too.
            head = null
            return data
        }
        else {
            tail!!.next = null
            return data
        }
    }

    /** Returns the head element without removing it, or null if the list is empty. */
    override fun peekFront(): T? {
        return head?.data
    }

    /** Returns the tail element without removing it, or null if the list is empty. */
    override fun peekBack(): T? {
        return tail?.data
    }

    /** Returns true if the list has no elements. */
    override fun isEmpty(): Boolean {
        return head == null
    }
}