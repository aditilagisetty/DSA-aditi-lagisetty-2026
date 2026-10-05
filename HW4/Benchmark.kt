import kotlin.random.Random
import kotlin.time.DurationUnit
import kotlin.time.measureTime

// every sort
val sorts: Map<String, (IntArray) -> Unit> = mapOf(
    "insertion" to ::insertionSort,
    "merge" to ::mergeSort,
    "quick" to ::quickSort,
    "heap" to ::heapSort,
    "intro" to ::introSort,
    "radix" to ::radixSort,
    "sample" to ::sampleSort,
)

val SIZES = listOf(10, 100, 1_000, 10_000, 100_000, 1_000_000)

// small sizes run so fast that we need lots of trials to get a stable number
fun trialsFor(n: Int): Int = when {
    n <= 10_000 -> 100
    n <= 100_000 -> 5
    else -> 3
}

// sorts a copy of input and returns how long it took in milliseconds
fun timeOnce(sort: (IntArray) -> Unit, input: IntArray): Double {
    val copy = input.copyOf()
    val elapsed = measureTime { sort(copy) }
    return elapsed.toDouble(DurationUnit.MILLISECONDS)
}

fun median(times: List<Double>): Double {
    val sorted = times.sorted()
    return sorted[sorted.size / 2]
}

// run every sort a few times first so its optimized
fun warmUp() {
    repeat(5) {
        val input = IntArray(10_000) { Random.nextInt() }
        for (sort in sorts.values) {
            sort(input.copyOf())
        }
    }
}

// times every sort at every size and prints a markdown table of median times
fun benchmark(label: String, makeInput: (Int) -> IntArray) {
    println("### $label (median ms)")
    println()
    println("| n | " + sorts.keys.joinToString(" | ") + " |")
    println("|---|" + sorts.keys.joinToString("|") { "---" } + "|")

    for (n in SIZES) {
        val times = sorts.keys.associateWith { mutableListOf<Double>() }
        repeat(trialsFor(n)) {
            val input = makeInput(n)
            for ((name, sort) in sorts) {
                times.getValue(name).add(timeOnce(sort, input))
            }
        }
        val cells = sorts.keys.map { name -> "%.4f".format(median(times.getValue(name))) }
        println("| %,d | ".format(n) + cells.joinToString(" | ") + " |")
    }
    println()
}

fun runBenchmarks() {
    warmUp()
    benchmark("Random input") { n -> IntArray(n) { Random.nextInt() } }
    benchmark("Already sorted input") { n -> IntArray(n) { it } }
}
