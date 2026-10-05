/**
 * Main.kt
 *
 * Reuses MinHeap / MinPriorityQueue from HW3
 *
 * Compile:
 *   kotlinc Main.kt Benchmark.kt InsertionSort.kt MergeSort.kt HeapSort.kt QuickSort.kt IntroSort.kt RadixSort.kt SampleSort.kt ../HW3/MinPriorityQueue.kt ../HW3/MinHeap.kt -include-runtime -d hw4.jar
 * Run:
 *   java -jar hw4.jar
 */

fun main() {
    // the larger priority goes in first
    // a correct heap has to reorder them
    val heap = MinHeap<String>()
    heap.addWithPriority("big", 5.0)
    heap.addWithPriority("small", 1.0)
    println("first out: ${heap.next()}")
    println("second out: ${heap.next()}")
}
