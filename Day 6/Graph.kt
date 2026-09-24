
class Graph<VertexType> {
    private var adjacencyList: MutableMap<VertexType, MutableSet<VertexType>> = mutableMapOf()

   
    fun addVertex(v: VertexType): Boolean {
        if (adjacencyList.contains(v)) {
            return false
        }
        adjacencyList[v] = mutableSetOf()
        return true
    }

    fun addEdge(from: VertexType, to: VertexType): Boolean {
        val adjacentVertices = adjacencyList[from]
        if (adjacentVertices != null && adjacencyList.contains(to)) {
            adjacentVertices.add(to)
            return true
        }
        return false
    }

    fun getEdges(from: VertexType): Set<VertexType> {
        // Note: Elvis operator gives us a value if left hand expression is null
        return adjacencyList[from] ?: setOf()
    }

    fun clear() {
        adjacencyList.clear()
    }

    // Exercise 3: Breadth-first search (QUEUE)
     /**
     * Search through a graph using a breadth-first search
     * @param start the node to start the search
     * @param target the node to search for
     * @return the path from start to target (if one exists) and null otherwise.
     *   Because BFS explores in order of distance, this path has the fewest edges.
     */

    fun bfs(start: VertexType, target: VertexType): List<VertexType>? {
        // cameFrom[m] = n means "we first reached m from n".
        // Its keys double as the toVisit set from the pseudocode.
        val cameFrom = mutableMapOf(start to start)
        val queue = ArrayDeque<VertexType>()
        queue.addLast(start)

        while (queue.isNotEmpty()) {
            val n = queue.removeFirst()          // oldest first -> breadth-first
            if (n == target) {
                return buildPath(cameFrom, start, target)
            }
            for (m in getEdges(n)) {
                if (m !in cameFrom) {
                    cameFrom[m] = n
                    queue.addLast(m)
                }
            }
        }
        return null
    }

    
    // Exercise 4: Depth-first search (STACK)

    /**
     * Search through a graph using a depth-first search
     * @param start the node to start the search
     * @param target the node to search for
     * @return a path from start to target (if one exists) and null otherwise.
     *   Not guaranteed to be the shortest path.
     */
    fun dfs(start: VertexType, target: VertexType): List<VertexType>? {
        val cameFrom = mutableMapOf(start to start)
        val stack = ArrayDeque<VertexType>()
        stack.addLast(start)

        while (stack.isNotEmpty()) {
            val n = stack.removeLast()           // newest first -> depth-first
            if (n == target) {
                return buildPath(cameFrom, start, target)
            }
            for (m in getEdges(n)) {
                if (m !in cameFrom) {
                    cameFrom[m] = n
                    stack.addLast(m)
                }
            }
        }
        return null
    }

    // Boolean version of DFS: true if and only if a path exists. 
    fun dfsExists(start: VertexType, target: VertexType): Boolean = dfs(start, target) != null

    // Exercise 2: path reconstruction helper

    // Walk backwards through cameFrom from target to start, then reverse.
    private fun buildPath(
        cameFrom: Map<VertexType, VertexType>,
        start: VertexType,
        target: VertexType
    ): List<VertexType> {
        val path = mutableListOf(target)
        var current = target
        while (current != start) {
            current = cameFrom.getValue(current)
            path.add(current)
        }
        return path.reversed()
    }

    // Every vertex reachable from start (including start) via BFS
    private fun reachableFrom(start: VertexType): Set<VertexType> {
        val seen = mutableSetOf(start)
        val queue = ArrayDeque<VertexType>()
        queue.addLast(start)
        while (queue.isNotEmpty()) {
            val n = queue.removeFirst()
            for (m in getEdges(n)) {
                if (seen.add(m)) {               // add returns false if already present
                    queue.addLast(m)
                }
            }
        }
        return seen
    }

    // Graph properties
    /**
     * Number of connected components.
     * Assumes the graph is undirected (every edge A->B also has B->A).
     */
    fun countConnectedComponents(): Int {
        val seen = mutableSetOf<VertexType>()
        var count = 0
        for (v in getVertices()) {
            if (v !in seen) {
                count++                          // found a new "island"
                seen.addAll(reachableFrom(v))    // mark the whole island
            }
        }
        return count
    }

    /**
     * Kahn's algorithm.
     * @return a topological ordering of the vertices, or null if the graph has a cycle.
     *   Instead of deleting edges from the real graph, we track how many
     *   incoming edges each vertex has left.
     */
    fun topologicalSort(): List<VertexType>? {
        val inDegree = mutableMapOf<VertexType, Int>()
        for (v in getVertices()) inDegree[v] = 0
        for (v in getVertices()) {
            for (m in getEdges(v)) {
                inDegree[m] = inDegree.getValue(m) + 1
            }
        }

        val result = mutableListOf<VertexType>()                        // L
        val ready = ArrayDeque(getVertices().filter { inDegree[it] == 0 }) // S

        while (ready.isNotEmpty()) {
            val n = ready.removeFirst()
            result.add(n)
            for (m in getEdges(n)) {
                val remaining = inDegree.getValue(m) - 1                // "remove edge n->m"
                inDegree[m] = remaining
                if (remaining == 0) {
                    ready.addLast(m)
                }
            }
        }

        // If some vertices never reached in-degree 0, they're stuck in a cycle.
        return if (result.size == getVertices().size) result else null
    }

    /** A directed graph is a DAG iff it has a topological ordering. */
    fun isDAG(): Boolean = topologicalSort() != null

    /**
     * Undirected tree check: connected and |E| = |V| - 1.
     * Assumes the graph is undirected (every edge stored in both directions),
     * so each undirected edge is counted twice in the adjacency list.
     */
    fun isUndirectedTree(): Boolean {
        val vertexCount = getVertices().size
        if (vertexCount == 0) return false
        val edgeCount = getVertices().sumOf { getEdges(it).size } / 2
        return countConnectedComponents() == 1 && edgeCount == vertexCount - 1
    }

    /**
     * Directed tree check: acyclic, and the graph you get by making every
     * edge undirected is a tree.
     */
    fun isDirectedTree(): Boolean {
        if (!isDAG()) return false
        return toUndirected().isUndirectedTree()
    }

    // A copy of this graph with every edge added in both directions. 
    fun toUndirected(): Graph<VertexType> {
        val g = Graph<VertexType>()
        for (v in getVertices()) g.addVertex(v)
        for (v in getVertices()) {
            for (m in getEdges(v)) {
                g.addEdge(v, m)
                g.addEdge(m, v)
            }
        }
        return g
    }
}