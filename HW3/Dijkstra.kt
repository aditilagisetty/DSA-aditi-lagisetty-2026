/**
 * Finds the cheapest path from [start] to [target] in [graph] using Dijkstra's algorithm.
 * Edge costs must be non-negative.
 *
 * @return the path as a list of vertices from [start] to [target] inclusive,
 *   or null if [target] is not reachable from [start].
 */
fun <V> dijkstra(graph: Graph<V>, start: V, target: V): List<V>? {
    // best known cost from start to each vertex
    val dist = mutableMapOf<V, Double>()
    // the vertex we came from on the best known path
    val prev = mutableMapOf<V, V>()
    // vertices waiting to be processed, cheapest first
    val heap = MinHeap<V>()

    for (v in graph.getVertices()) {
        dist[v] = if (v == start) 0.0 else Double.POSITIVE_INFINITY
        heap.addWithPriority(v, dist.getValue(v))
    }

    while (!heap.isEmpty()){
        val current = heap.next() ?: break // gets cheapest unvisited vertices
        if (current == target){ // stop looking if its the target
            break
        }
        for ((neighbor, cost) in graph.getEdges(current)){ // look everywhere that current connects to
            val newDist = dist.getValue(current) + cost // cost of reachign a neighbor through current
            if (newDist < dist.getValue(neighbor)){ // update route if its cheaper
                dist[neighbor] = newDist
                prev[neighbor] = current
                heap.adjustPriority(neighbor, newDist) // tell heap to resort
            }

        }
    }

    if (dist.getValue(target) == Double.POSITIVE_INFINITY) {
        return null
    }

    // reverse to get shortest path returned
    val path = mutableListOf<V>()
    var currentVertex: V? = target

    while (currentVertex != null) {
        path.add(currentVertex)
        currentVertex = prev[currentVertex]
    }

    return path.reversed()

}