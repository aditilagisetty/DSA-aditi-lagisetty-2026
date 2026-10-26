/** returns the flow value and how many augmenting paths it took to get there */
data class MaxFlowResult(val value: Long, val augmentations: Int)

/**
 * The Ford-Fulkerson method -- while the residual graph has a path from source to sink push as much
 * flow as that path allows (its bottleneck). Subclasses only decide how to find the path.
 */
abstract class AugmentingPathMaxFlow {
    abstract val name: String

    /**
     * Looks for a source -> sink path in the residual graph.
     * Returns `parent`, where `parent[v]` is the edge used to reach v, or null if the sink can't be reached.
     */
    protected abstract fun findPath(network: FlowNetwork, source: Int, sink: Int): Array<Edge?>?

    /** Computes a maximum flow from [source] to [sink]. Leaves the flow on the network's edges. */
    fun maxFlow(network: FlowNetwork, source: Int, sink: Int): MaxFlowResult {
        require(source != sink) { "source and sink must be different" }
        network.resetFlow()
        var total = 0L
        var augmentations = 0
        while (true) {
            val parent = findPath(network, source, sink) ?: break

            // walk back from the sink to find the bottleneck
            var bottleneck = Long.MAX_VALUE
            var v = sink
            while (v != source) {
                val edge = parent[v]!!
                bottleneck = minOf(bottleneck, edge.residual)
                v = edge.from
            }

            // walk back again and push the bottleneck along every edge of the path
            v = sink
            while (v != source) {
                val edge = parent[v]!!
                edge.push(bottleneck)
                v = edge.from
            }

            total += bottleneck
            augmentations++
        }
        return MaxFlowResult(total, augmentations)
    }
}

/**
 * Ford-Fulkerson with DFS. DFS takes the first residual edge it sees and keeps going deep,
 * so it can wander down long paths with small bottlenecks. Runtime is O(E * f) where f is the max flow
 * value so it depends on the capacities not just the size of the graph.
 *
 * The DFS uses an explicit stack instead of recursion so long paths can't overflow the call stack.
 */
class FordFulkerson : AugmentingPathMaxFlow() {
    override val name = "Ford-Fulkerson (DFS)"

    override fun findPath(network: FlowNetwork, source: Int, sink: Int): Array<Edge?>? {
        val n = network.vertexCount
        val parent = arrayOfNulls<Edge>(n)
        val visited = BooleanArray(n)
        val nextEdge = IntArray(n)   // which edge of each vertex's list to try next
        val stack = ArrayDeque<Int>()
        visited[source] = true
        stack.addLast(source)

        while (stack.isNotEmpty()) {
            val v = stack.last()
            if (v == sink) return parent
            val edges = network.edgesFrom(v)
            if (nextEdge[v] == edges.size) {
                stack.removeLast()   // dead end, back up
                continue
            }
            val edge = edges[nextEdge[v]++]
            if (edge.residual > 0 && !visited[edge.to]) {
                visited[edge.to] = true
                parent[edge.to] = edge
                stack.addLast(edge.to)
            }
        }
        return null
    }
}

/**
 * Edmonds-Karp: Ford-Fulkerson with BFS, so every augmenting path is a shortest one
 * (fewest edges). That makes the number of augmentations at most V*E/2 no matter what the capacities
 * are, and each BFS is O(E), so the whole thing is O(V * E^2).
 */
class EdmondsKarp : AugmentingPathMaxFlow() {
    override val name = "Edmonds-Karp (BFS)"

    override fun findPath(network: FlowNetwork, source: Int, sink: Int): Array<Edge?>? {
        val n = network.vertexCount
        val parent = arrayOfNulls<Edge>(n)
        val visited = BooleanArray(n)
        val queue = ArrayDeque<Int>()
        visited[source] = true
        queue.addLast(source)

        while (queue.isNotEmpty()) {
            val v = queue.removeFirst()
            for (edge in network.edgesFrom(v)) {
                if (edge.residual > 0 && !visited[edge.to]) {
                    visited[edge.to] = true
                    parent[edge.to] = edge
                    if (edge.to == sink) return parent
                    queue.addLast(edge.to)
                }
            }
        }
        return null
    }
}
