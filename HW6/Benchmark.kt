import java.io.File
import kotlin.time.DurationUnit
import kotlin.time.measureTime

// runs f once and returns how long it took in milliseconds
fun timeMs(f: () -> Unit): Double = measureTime { f() }.toDouble(DurationUnit.MILLISECONDS)

fun median(times: List<Double>): Double = times.sorted()[times.size / 2]

// keep calling f until about [budgetMs] has passed (at least [minRuns] times), and return every run's time
fun timeRuns(budgetMs: Double, minRuns: Int, f: () -> Unit): List<Double> {
    val times = mutableListOf<Double>()
    var total = 0.0
    while (times.size < minRuns || total < budgetMs) {
        val t = timeMs(f)
        times.add(t)
        total += t
        if (times.size >= 200) break
    }
    return times
}

/**
 * the JVM starts out interpreting the bytecode and only compiles methods to fast
 * machine code after theyve been called a lot. So before timing anything i run both algorithms
 * on every graph for a while and throw those times away. Then the timed runs measure compiled code.
 */
fun warmUp(problems: List<FlowProblem>, algorithms: List<AugmentingPathMaxFlow>) {
    for (problem in problems) {
        for (alg in algorithms) timeRuns(budgetMs = 300.0, minRuns = 3) { alg.maxFlow(problem.network, problem.source, problem.sink) }
    }
}

/** Times both algorithms on every benchmark graph and writes results/benchmarks.csv */
fun runBenchmarks(out: File) {
    out.parentFile?.mkdirs()
    val algorithms = listOf(FordFulkerson(), EdmondsKarp())
    val families = listOf(
        "sparse" to BenchmarkGraphs.SPARSE_SIZES,
        "dense" to BenchmarkGraphs.DENSE_SIZES,
        "adversarial" to BenchmarkGraphs.ADVERSARIAL_SIZES,
    )
    // build every graph up front so building them isn't part of any timing
    val problems = families.flatMap { (family, sizes) -> sizes.map { Triple(family, it, BenchmarkGraphs.make(family, it)) } }

    println()
    println("=== Benchmark ===")
    println("warming up the JIT on every graph...")
    warmUp(problems.map { it.third }, algorithms)

    val lines = mutableListOf("family,V,E,algorithm,median_ms,min_ms,max_ms,runs")
    println()
    println("| family | V | E | Ford-Fulkerson median ms | Edmonds-Karp median ms | FF / EK |")
    println("|---|---|---|---|---|---|")
    for ((family, v, problem) in problems) {
        val medians = algorithms.map { alg ->
            val times = timeRuns(budgetMs = 2000.0, minRuns = 5) { alg.maxFlow(problem.network, problem.source, problem.sink) }
            lines.add("$family,$v,${problem.network.edgeCount},${alg.name},%.4f,%.4f,%.4f,${times.size}"
                .format(median(times), times.min(), times.max()))
            median(times)
        }
        println("| $family | $v | ${problem.network.edgeCount} | %.3f | %.3f | %.1fx |".format(medians[0], medians[1], medians[0] / medians[1]))
    }
    out.writeText(lines.joinToString("\n") + "\n")
    println()
    println("wrote ${out.path}")
}
