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

    // testing dijkstra
    val graph: Graph<String> = AdjacencyMapGraph()
    graph.addEdge("A", "B", 4.0)
    graph.addEdge("A", "C", 1.0)
    graph.addEdge("C", "B", 1.0)
    graph.addEdge("B", "D", 1.0)
    graph.addEdge("C", "D", 5.0)
    graph.addEdge("Z", "Z", 0.0) // isolated vertex, unreachable from A

    // expected shortest path A -> D is A, C, B, D with cost 3.0
    val path = dijkstra(graph, "A", "D")
    println("Shortest path A -> D: $path")

    // Z has no edges to or from the rest of the graph so no path exists
    val noPath = dijkstra(graph, "A", "Z")
    println("Shortest path A -> Z (should be null): $noPath")
}