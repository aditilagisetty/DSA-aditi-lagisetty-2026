/**
 * Main.kt
 *
 * Runs the HW4 sorting benchmark and prints markdown tables of the results
 *
 * Compile:
 *   kotlinc Main.kt Benchmark.kt InsertionSort.kt MergeSort.kt HeapSort.kt QuickSort.kt IntroSort.kt RadixSort.kt SampleSort.kt -include-runtime -d hw4.jar
 * Run:
 *   java -jar hw4.jar
 */

fun main() {
    runBenchmarks()
}
