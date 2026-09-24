/**
 * HW3Test.kt
 *
 * Plain-Kotlin tests for AdjacencyMapGraph, MinHeap, dijkstra, and the Project Euler solutions
 *
 * Compile: kotlinc Graph.kt MinPriorityQueue.kt MinHeap.kt Dijkstra.kt MatrixIO.kt ProjectEuler81.kt ProjectEuler82.kt ProjectEuler83.kt HW3Test.kt -include-runtime -d test.jar
 * Run:     java -cp test.jar HW3TestKt
 */

var passed = 0
var failed = 0

fun check(name: String, actual: Any?, expected: Any?) {
    if (actual == expected) {
        passed++
        println("PASS: $name")
    } else {
        failed++
        println("FAIL: $name (expected $expected, got $actual)")
    }
}

fun checkThrows(name: String, block: () -> Unit) {
    try {
        block()
        failed++
        println("FAIL: $name (expected an exception, but nothing was thrown)")
    } catch (e: Exception) {
        passed++
        println("PASS: $name")
    }
}

fun main() {
    // AdjacencyMapGraph

    val graph: Graph<String> = AdjacencyMapGraph()
    check("new graph has no vertices", graph.getVertices(), emptySet<String>())

    graph.addEdge("A", "B", 1.0)
    check("addEdge registers the 'from' vertex", "A" in graph.getVertices(), true)
    check("addEdge registers the 'to' vertex", "B" in graph.getVertices(), true)
    check("getEdges returns the new edge", graph.getEdges("A"), mapOf("B" to 1.0))
    check("getEdges on a vertex with no outgoing edges is empty", graph.getEdges("B"), emptyMap<String, Double>())
    check("getEdges on an unknown vertex is empty", graph.getEdges("Z"), emptyMap<String, Double>())

    graph.addEdge("A", "B", 5.0)
    check("addEdge on an existing edge overwrites the cost", graph.getEdges("A"), mapOf("B" to 5.0))

    graph.clear()
    check("clear removes all vertices", graph.getVertices(), emptySet<String>())

    // MinHeap

    val heap = MinHeap<String>()
    check("new heap is empty", heap.isEmpty(), true)
    check("next() on an empty heap returns null", heap.next(), null)

    heap.addWithPriority("D", 9.0)
    heap.addWithPriority("A", 1.0)
    heap.addWithPriority("C", 4.0)
    heap.addWithPriority("B", 3.0)
    check("heap is not empty after adding", heap.isEmpty(), false)
    check("next() returns the lowest-priority element first", heap.next(), "A")
    check("next() returns the next-lowest afterward", heap.next(), "B")

    heap.addWithPriority("E", 100.0)
    heap.adjustPriority("E", 0.0)
    check("adjustPriority to a lower value moves the element to the front", heap.next(), "E")

    checkThrows("addWithPriority rejects a duplicate element") {
        val h = MinHeap<String>()
        h.addWithPriority("X", 1.0)
        h.addWithPriority("X", 2.0)
    }

    checkThrows("adjustPriority on a missing element throws") {
        val h = MinHeap<String>()
        h.adjustPriority("nope", 1.0)
    }

    // dijkstra

    // A -> C -> B -> D (cost 3) is cheaper than A -> B -> D (cost 5) or A -> C -> D (cost 6),
    // so this graph catches a relaxation bug that finds a path but not the cheapest one.
    val trapGraph: Graph<String> = AdjacencyMapGraph()
    trapGraph.addEdge("A", "B", 4.0)
    trapGraph.addEdge("A", "C", 1.0)
    trapGraph.addEdge("C", "B", 1.0)
    trapGraph.addEdge("B", "D", 1.0)
    trapGraph.addEdge("C", "D", 5.0)
    check("dijkstra finds the cheapest path, not just any path", dijkstra(trapGraph, "A", "D"), listOf("A", "C", "B", "D"))

    trapGraph.addEdge("Z", "Z", 0.0) // isolated vertex, unreachable from A
    check("dijkstra returns null when the target is unreachable", dijkstra(trapGraph, "A", "Z"), null)

    check("dijkstra from a vertex to itself is a single-element path", dijkstra(trapGraph, "A", "A"), listOf("A"))

    // Project Euler solutions, checked against their known accepted answers so a change to
    // Dijkstra/AdjacencyMapGraph that breaks the applied problems gets caught here too.

    val matrix = readMatrix("matrix.txt")
    check("Project Euler 81 (right/down only)", solve81(matrix), 427337)
    check("Project Euler 82 (up/down/right, any start/end column)", solve82(matrix), 260324)
    check("Project Euler 83 (all four directions)", solve83(matrix), 425185)

    println()
    println("$passed passed, $failed failed")
    if (failed > 0) {
        kotlin.system.exitProcess(1)
    }
}
