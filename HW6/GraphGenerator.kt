import kotlin.random.Random

/** A network plus which vertices are the source and the sink. */
class FlowProblem(val network: FlowNetwork, val source: Int, val sink: Int)

object GraphGenerator {
    /**
     * A random graph with [vertices] vertices and about [edges] edges, capacities from 1 to [maxCapacity].
     * Vertex 0 is the source and the last vertex is the sink. No self loops and no edge into the source
     * or out of the sink (they could never carry flow anyway). The same [seed] gives the same graph.
     */
    fun random(vertices: Int, edges: Int, maxCapacity: Long, seed: Int): FlowProblem {
        require(vertices >= 2)
        val random = Random(seed)
        val network = FlowNetwork(vertices)
        val source = 0
        val sink = vertices - 1
        val used = HashSet<Long>()
        val maxPossible = (vertices - 1).toLong() * (vertices - 1) - (vertices - 2)
        val target = minOf(edges.toLong(), maxPossible).toInt()
        while (network.edgeCount < target) {
            val from = random.nextInt(0, vertices - 1)   // anything but the sink
            val to = random.nextInt(1, vertices)          // anything but the source
            if (from == to || !used.add(from.toLong() * vertices + to)) continue
            network.addEdge(from, to, random.nextLong(1, maxCapacity + 1))
        }
        return FlowProblem(network, source, sink)
    }

    /**
     * The case built to make DFS pick bad paths
     *
     * Every vertex v gets an edge to v + 1 first, so DFS always dives down the long chain
     * 0 -> 1 -> 2 -> ... -> sink before trying anything else. Each vertex also gets a few random
     * "shortcut" edges, so short fat paths exist (BFS finds those). All capacities are large and random.
     *
     * A long paths bottleneck is the smallest of many random capacities, so it's tiny. DFS keeps pushing
     * tiny amounts along long paths, and every push opens reverse edges that send the next DFS on another
     * long tour. BFS uses short paths, whose bottlenecks are much bigger, and its number of augmentations
     * is bounded by V*E/2 whatever the capacities are.
     */
    fun adversarial(vertices: Int, shortcutsPerVertex: Int, maxCapacity: Long, seed: Int): FlowProblem {
        require(vertices >= 3)
        val random = Random(seed)
        val network = FlowNetwork(vertices)
        val sink = vertices - 1
        for (v in 0 until sink) {
            val targets = linkedSetOf(v + 1)   // the chain edge goes first in v's list
            repeat(shortcutsPerVertex) {
                val to = random.nextInt(1, vertices)
                if (to != v) targets.add(to)
            }
            for (to in targets) network.addEdge(v, to, random.nextLong(1, maxCapacity + 1))
        }
        return FlowProblem(network, 0, sink)
    }
}
