import kotlin.math.log2

// ranges this size or smaller are left for the final insertion sort pass
private const val SMALL_RANGE = 16

/**
 * Sorts [arr] in place using introsort
 * quicksort that switches to heap sort if the recursion
 * gets too deep, then finishes small ranges with one insertion sort pass
 *
 * Time: O(n log n) worst case. Space: O(log n) recursion stack.
 */
fun introSort(arr: IntArray) {
    if (arr.size < 2) return
    val maxDepth = 2 * log2(arr.size.toDouble()).toInt()
    introSortRange(arr, 0, arr.size - 1, maxDepth)
    insertionSort(arr)
}

// quicksorts arr[lo..hi] until ranges are small or depth runs out
private fun introSortRange(arr: IntArray, lo: Int, hi: Int, depthLeft: Int) {
    if (hi - lo + 1 <= SMALL_RANGE) return

    if (depthLeft == 0) {
        val piece = arr.copyOfRange(lo, hi + 1)
        heapSort(piece)
        piece.copyInto(arr, lo)
        return
    }

    val p = lomutoPartition(arr, lo, hi)
    introSortRange(arr, lo, p - 1, depthLeft - 1)
    introSortRange(arr, p + 1, hi, depthLeft - 1)
}
