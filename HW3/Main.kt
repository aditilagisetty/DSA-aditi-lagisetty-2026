fun main() {
    val heap = MinHeap<String>()
    heap.addWithPriority("D", 9.0)
    heap.addWithPriority("A", 1.0)
    heap.addWithPriority("C", 4.0)
    heap.addWithPriority("B", 3.0)
    heap.addWithPriority("E", 7.0)

    heap.adjustPriority("D", 2.0)
    heap.adjustPriority("A", 8.0)

    while (!heap.isEmpty()) {
        print("${heap.next()} ")
    }
    println()
    println(heap.next())
}