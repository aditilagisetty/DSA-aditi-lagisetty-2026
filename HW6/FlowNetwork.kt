/**
 * One directed edge in a [FlowNetwork]
 *
 * Every edge the user adds comes with a partner [reverse] edge going the other way with capacity 0.
 * Pushing flow along an edge takes it away from the partners flow, so the partner's
 * [residual] goes up. That's how the algorithms undo flow they sent earlier.
 */
class Edge(val from: Int, val to: Int, val capacity: Long, val isOriginal: Boolean) {
    var flow: Long = 0
        private set

    lateinit var reverse: Edge
        internal set

    /** How much more flow this edge can take right now. */
    val residual: Long get() = capacity - flow

    /** Sends [amount] more flow along this edge (and takes it back off the reverse edge). */
    fun push(amount: Long) {
        flow += amount
        reverse.flow -= amount
    }

    internal fun resetFlow() {
        flow = 0
    }

    override fun toString() = "$from -> $to ($flow/$capacity)"
}

/**
 * A flow network on the vertices 0 until [vertexCount], stored as adjacency lists of [Edge]s.
 *
 * The adjacency list of a vertex holds its original out-edges and the reverse edges of its
 * in edges, in the order they were added. Together those are the edges of the residual graph:
 * an edge is in the residual graph whenever its [Edge.residual] is above 0.
 */
class FlowNetwork(val vertexCount: Int) {
    private val adjacency = List(vertexCount) { mutableListOf<Edge>() }
    private val originalEdges = mutableListOf<Edge>()

    /** Number of edges the user added (not counting the reverse edges). */
    val edgeCount: Int get() = originalEdges.size

    /** Adds an edge [from] -> [to] that can carry up to [capacity] units of flow, and returns it. */
    fun addEdge(from: Int, to: Int, capacity: Long): Edge {
        require(from in 0 until vertexCount && to in 0 until vertexCount) { "vertex out of range: $from -> $to" }
        require(capacity >= 0) { "capacity can't be negative: $capacity" }
        val forward = Edge(from, to, capacity, isOriginal = true)
        val backward = Edge(to, from, 0, isOriginal = false)
        forward.reverse = backward
        backward.reverse = forward
        adjacency[from].add(forward)
        adjacency[to].add(backward)
        originalEdges.add(forward)
        return forward
    }

    /** All residual-graph edges leaving [vertex] (original edges and reverse edges). */
    fun edgesFrom(vertex: Int): List<Edge> = adjacency[vertex]

    /** The edges that were added with [addEdge] in the order they were added. */
    fun edges(): List<Edge> = originalEdges

    /** Sets the flow on every edge back to 0 so another algorithm can run on the same network. */
    fun resetFlow() {
        for (list in adjacency) for (edge in list) edge.resetFlow()
    }

    /** Net flow leaving [vertex] along original edges minus flow entering it. */
    fun netFlowOut(vertex: Int): Long {
        var total = 0L
        for (edge in originalEdges) {
            if (edge.from == vertex) total += edge.flow
            if (edge.to == vertex) total -= edge.flow
        }
        return total
    }

    /**
     * The source side of a minimum cut -- every vertex still reachable from [source] in the residual graph.
     * Only meaningful after a max-flow algorithm has finished. Every original edge from this set to the
     * rest is saturated, and their capacities add up to the max flow (the max flow min cut theorem).
     */
    fun minCutSourceSide(source: Int): Set<Int> {
        val seen = BooleanArray(vertexCount)
        val queue = ArrayDeque<Int>()
        seen[source] = true
        queue.add(source)
        while (queue.isNotEmpty()) {
            val v = queue.removeFirst()
            for (edge in adjacency[v]) {
                if (edge.residual > 0 && !seen[edge.to]) {
                    seen[edge.to] = true
                    queue.add(edge.to)
                }
            }
        }
        return (0 until vertexCount).filter { seen[it] }.toSet()
    }

    /** The original edges that cross the min cut from the source side to the sink side. */
    fun minCutEdges(source: Int): List<Edge> {
        val sourceSide = minCutSourceSide(source)
        return originalEdges.filter { it.from in sourceSide && it.to !in sourceSide }
    }
}
