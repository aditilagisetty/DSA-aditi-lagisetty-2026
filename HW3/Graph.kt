interface Graph<VertexType> {
    fun getVertices(): Set<VertexType>
    fun addEdge(from: VertexType, to: VertexType, cost: Double)
    fun getEdges(from: VertexType): Map<VertexType, Double>
    fun clear()
}

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