/**
 * Main.kt
 *
 * Runs both sequence alignments on the Salmonella sequences from Genome.kt,
 * then (with the "bench" argument) the matrix multiplication benchmarks.
 *
 * Compile:
 *   kotlinc Main.kt Matrix.kt Benchmark.kt SequenceAlignment.kt Genome.kt -include-runtime -d hw5.jar
 * Run:
 *   java -jar hw5.jar          (alignments only)
 *   java -jar hw5.jar bench    (alignments + matrix benchmarks)
 */

fun printAlignment(title: String, result: Alignment) {
    println("## $title")
    println()
    println("score: ${result.score}")
    println("columns: ${result.top.length}, matches: ${result.matches}, gaps: ${result.gaps}, " +
        "identity: %.1f%%".format(100.0 * result.matches / result.top.length))
    println("query  range: ${result.aStart}..${result.aEnd}")
    println("target range: ${result.bStart}..${result.bEnd}")
    println()
    println(result.pretty())
    println()
}

fun main(args: Array<String>) {
    // global: line up all of genomeSnippet with all of testAgainst
    printAlignment(
        "Needleman-Wunsch: genomeSnippet (top) vs testAgainst (bottom), +1/-1/-1",
        needlemanWunsch(genomeSnippet, testAgainst),
    )

    // local: find where testAgainst best matches inside the much longer targetGenome
    val local = smithWaterman(testAgainst, targetGenome)
    printAlignment("Smith-Waterman: testAgainst (top) vs targetGenome (bottom), +3/-3/-2", local)
    println("best matching piece of targetGenome:")
    println(targetGenome.substring(local.bStart, local.bEnd))
    println()

    if ("bench" in args) runBenchmarks()
}
