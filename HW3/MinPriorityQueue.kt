interface MinPriorityQueue<T> {
    fun isEmpty(): Boolean
    fun addWithPriority(elem: T, priority: Double)
    fun next(): T?
    fun adjustPriority(elem: T, newPriority: Double)
}
