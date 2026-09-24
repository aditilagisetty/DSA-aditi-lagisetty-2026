/**
 * A directed, weighted graph over vertices of type [VertexType].
 */
interface Graph<VertexType> {
    /** Returns every vertex currently in the graph. */
    fun getVertices(): Set<VertexType>

    /**
     * Adds a directed edge from [from] to [to] with the given [cost].
     * Both [from] and [to] become vertices of the graph if they weren't already.
     */
    fun addEdge(from: VertexType, to: VertexType, cost: Double)

    /** Returns the neighbors of [from], mapped to the cost of the edge leading to each one. */
    fun getEdges(from: VertexType): Map<VertexType, Double>

    /** Removes every vertex and edge from the graph. */
    fun clear()
}

/**
 * A [Graph] implementation backed by an adjacency map: each vertex maps to a map of its
 * outgoing neighbors and their edge costs.
 */
class AdjacencyMapGraph<VertexType> : Graph<VertexType> {
    private val edges = mutableMapOf<VertexType, MutableMap<VertexType, Double>>()

    override fun getVertices(): Set<VertexType> = edges.keys

    override fun addEdge(from: VertexType, to: VertexType, cost: Double) {
        edges.getOrPut(from) { mutableMapOf() }[to] = cost
        edges.getOrPut(to) { mutableMapOf() }
    }

    override fun getEdges(from: VertexType): Map<VertexType, Double> =
        edges[from] ?: emptyMap()

    override fun clear() = edges.clear()
}