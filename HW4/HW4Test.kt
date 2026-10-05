/**
 * HW4Test.kt
 *
 * Tests for every sort
 *
 * Compile: kotlinc InsertionSort.kt MergeSort.kt QuickSort.kt HeapSort.kt IntroSort.kt RadixSort.kt SampleSort.kt Benchmark.kt HW4Test.kt -include-runtime -d test.jar
 * Run:     java -cp test.jar HW4TestKt
 */

import kotlin.random.Random

var passed = 0
var failed = 0

fun checkSorted(name: String, sort: (IntArray) -> Unit, input: IntArray) {
    val actual = input.copyOf() // copy so dont sort over already sorted values
    val expected = input.sortedArray()
    sort(actual)
    if (actual.contentEquals(expected)) {
        passed++
        println("PASS: $name")
    } else {
        failed++
        println("FAIL: $name (expected ${expected.contentToString()}, got ${actual.contentToString()})")
    }
}

fun main() {
    // the inputs every sort has to handle
    val cases: Map<String, IntArray> = mapOf(
        "empty" to intArrayOf(),
        "one element" to intArrayOf(42),
        "two elements" to intArrayOf(2, 1),
        "already sorted" to IntArray(500) { it },
        "reverse sorted" to IntArray(500) { 500 - it },
        "all duplicates" to IntArray(500) { 7 },
        "few distinct values" to IntArray(500) { Random.nextInt(0, 3) },
        "negatives" to intArrayOf(-3, 7, 0, -3, 9, -100, 1),
        "int extremes" to intArrayOf(Int.MAX_VALUE, 0, Int.MIN_VALUE, -1, 1, Int.MAX_VALUE, Int.MIN_VALUE),
        "random small" to IntArray(20) { Random.nextInt(-50, 50) },
        "random large" to IntArray(5000) { Random.nextInt() },
    )

    for ((sortName, sort) in sorts) {
        for ((caseName, input) in cases) {
            checkSorted("$sortName: $caseName", sort, input)
        }
    }

    println()
    println("$passed passed, $failed failed")
}
