/** The three graph families the benchmark uses, kept in one place so the benchmark and `counts` agree. */
object BenchmarkGraphs {
    val SPARSE_SIZES = listOf(100, 200, 400, 800, 1600, 3200)
    val DENSE_SIZES = listOf(25, 50, 100, 150, 200, 300)
    val ADVERSARIAL_SIZES = listOf(25, 50, 100, 150, 200, 250)

    const val SEED = 1

    fun make(family: String, vertices: Int): FlowProblem = when (family) {
        // about 10 edges per vertex, small capacities
        "sparse" -> GraphGenerator.random(vertices, vertices * 10, 100, SEED)
        // about half of all possible edges, small capacities
        "dense" -> GraphGenerator.random(vertices, vertices * vertices / 2, 100, SEED)
        // chain first edge order with huge capacities, built to make DFS pick long thin paths
        "adversarial" -> GraphGenerator.adversarial(vertices, 3, 1_000_000, SEED)
        else -> error("unknown family $family")
    }
}
