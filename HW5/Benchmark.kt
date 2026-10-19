import kotlin.random.Random
import kotlin.time.DurationUnit
import kotlin.time.measureTime

fun randomBenchMatrix(n: Int): Matrix {
    val m = Matrix(n)
    for (r in 0 until n) for (c in 0 until n) m[r, c] = Random.nextDouble()
    return m
}

// runs f once and returns how long it took in milliseconds
fun timeMs(f: () -> Unit): Double = measureTime { f() }.toDouble(DurationUnit.MILLISECONDS)

fun median(times: List<Double>): Double = times.sorted()[times.size / 2]

// small sizes run so fast that we need lots of trials to get a stable number
fun trialsFor(n: Int): Int = when {
    n <= 64 -> 50
    n <= 256 -> 10
    n <= 512 -> 5
    else -> 3
}

// median time of [trials] runs of multiplying two fresh random n x n matrices
fun medianTime(n: Int, trials: Int, mult: (Matrix, Matrix) -> Unit): Double {
    val times = mutableListOf<Double>()
    repeat(trials) {
        val a = randomBenchMatrix(n)
        val b = randomBenchMatrix(n)
        times.add(timeMs { mult(a, b) })
    }
    return median(times)
}

// run everything a bunch first so the JIT has compiled it before we start timing
fun warmUp() {
    repeat(20) {
        val a = randomBenchMatrix(64)
        val b = randomBenchMatrix(64)
        a.multiply(b)
        a.strassenMultiply(b)
        a.hybridMultiply(b, 16)
    }
}

// pure Strassen gets really slow (7^10 ≈ 280 million 1x1 multiplies at n = 1024), so stop it at 512
const val MAX_PURE_STRASSEN = 512

/** Table 1: normal vs pure Strassen vs hybrid (default cutoff) at a range of sizes. */
fun benchmarkMethods() {
    println("### Normal vs Strassen vs hybrid (cutoff $DEFAULT_CUTOFF), median ms")
    println()
    println("| n | multiply | strassenMultiply | hybridMultiply |")
    println("|---|---|---|---|")
    for (n in listOf(16, 32, 64, 100, 128, 256, 512, 1024, 2048)) {
        val trials = trialsFor(n)
        val normal = medianTime(n, trials) { a, b -> a.multiply(b) }
        val strassen = if (n <= MAX_PURE_STRASSEN) "%.3f".format(medianTime(n, trials) { a, b -> a.strassenMultiply(b) }) else "(too slow)"
        val hybrid = medianTime(n, trials) { a, b -> a.hybridMultiply(b) }
        println("| $n | %.3f | $strassen | %.3f |".format(normal, hybrid))
    }
    println()
}

/** Table 2: hybrid with different cutoffs, to find the crossover point. */
fun benchmarkCutoffs() {
    val cutoffs = listOf(8, 16, 32, 64, 128, 256, 512)
    println("### Hybrid cutoff sweep, median ms (normal multiply shown for comparison)")
    println()
    println("| n | multiply | " + cutoffs.joinToString(" | ") { "cutoff $it" } + " |")
    println("|---|---|" + cutoffs.joinToString("|") { "---" } + "|")
    for (n in listOf(256, 512, 1024, 2048)) {
        val trials = trialsFor(n)
        val normal = medianTime(n, trials) { a, b -> a.multiply(b) }
        val cells = cutoffs.map { cutoff ->
            if (cutoff >= n) "n/a" else "%.3f".format(medianTime(n, trials) { a, b -> a.hybridMultiply(b, cutoff) })
        }
        println("| $n | %.3f | ".format(normal) + cells.joinToString(" | ") + " |")
    }
    println()
}

fun runBenchmarks() {
    warmUp()
    benchmarkMethods()
    benchmarkCutoffs()
}
