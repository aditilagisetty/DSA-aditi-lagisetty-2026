fun main() {
    // The example graph from the walkthrough:
    // A -> B, A -> C, B -> D, B -> E, C -> F, E -> G, F -> G
    val g = Graph<String>()
    listOf("A", "B", "C", "D", "E", "F", "G").forEach { g.addVertex(it) }
    g.addEdge("A", "B"); g.addEdge("A", "C")
    g.addEdge("B", "D"); g.addEdge("B", "E")
    g.addEdge("C", "F")
    g.addEdge("E", "G"); g.addEdge("F", "G")

    println("--- Search ---")
    println("bfs(A, G) = ${g.bfs("A", "G")}")          // [A, B, E, G]
    println("dfs(A, G) = ${g.dfs("A", "G")}")          // [A, C, F, G]
    println("bfs(D, A) = ${g.bfs("D", "A")}")          // null (edges only go downward)
    println("bfs(A, A) = ${g.bfs("A", "A")}")          // [A]
    println("bfsExists(A, F) = ${g.bfsExists("A", "F")}")

    println("\n--- DAG / topological sort ---")
    println("isDAG = ${g.isDAG()}")                    // true
    println("topologicalSort = ${g.topologicalSort()}")
    println("isDirectedTree = ${g.isDirectedTree()}")  // false (G has two parents)

    val cyclic = Graph<String>()
    listOf("X", "Y", "Z").forEach { cyclic.addVertex(it) }
    cyclic.addEdge("X", "Y"); cyclic.addEdge("Y", "Z"); cyclic.addEdge("Z", "X")
    println("cycle X->Y->Z->X isDAG = ${cyclic.isDAG()}")  // false

    println("\n--- Connected components (undirected) ---")
    val u = Graph<Int>()
    (1..6).forEach { u.addVertex(it) }
    fun undirected(a: Int, b: Int) { u.addEdge(a, b); u.addEdge(b, a) }
    undirected(1, 2); undirected(2, 3)   // island {1, 2, 3}
    undirected(4, 5)                     // island {4, 5}
                                         // island {6}
    println("components = ${u.countConnectedComponents()}")  // 3
    println("isUndirectedTree = ${u.isUndirectedTree()}")    // false (not connected)

    undirected(3, 4); undirected(5, 6)   // now one connected path 1-2-3-4-5-6
    println("after connecting: components = ${u.countConnectedComponents()}, " +
            "isUndirectedTree = ${u.isUndirectedTree()}")    // 1, true
}