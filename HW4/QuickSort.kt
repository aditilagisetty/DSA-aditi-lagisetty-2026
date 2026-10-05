import kotlin.random.Random

/** Swaps arr[a] and arr[b]. Shared with QuickSort, HeapSort, and IntroSort. */
fun swap(arr: IntArray, a: Int, b: Int) {
    val temp = arr[a]
    arr[a] = arr[b]
    arr[b] = temp
}

/**
 * Sorts [arr] in place using quicksort with a random pivot
 *
 * Time: O(n log n) expected, O(n^2) worst case. Space: O(log n) expected recursion stack.
 */
fun quickSort(arr: IntArray) {
    // arr.size - 1 is last valid index since the range is inclusive
    quickSortRange(arr, 0, arr.size - 1)
}

// sorts arr[lo... hi] (inclusive)
private fun quickSortRange(arr: IntArray, lo: Int, hi: Int) {
    if (lo >= hi) return // means 0 or 1 elements which is already sorted
    val p = lomutoPartition(arr, lo, hi)
    // the pivot is at index p
    // recurse on everything to its left and everything to its right
    quickSortRange(arr, lo, p - 1)
    quickSortRange(arr, p + 1, hi)
}

/**
 * Partitions arr[lo..hi] around a random pivot
 * after everything left of the returned index is < pivot and everything right is >= pivot
 *
 * @return the pivot's final index
 */
fun lomutoPartition(arr: IntArray, lo: Int, hi: Int): Int {
    val pivotIndex = Random.nextInt(lo, hi + 1) // picks random index
    swap(arr, pivotIndex, hi) // swap chosen pivot to end so loop below doesnt have to step around it
    val pivot = arr[hi]

    var store = lo // boundary of the smaller than pivot section
    // k checks every element except the pivot at hi
    for (k in lo until hi) {
        if (arr[k] < pivot) {
            swap(arr, k, store)
            store++
        }
    }

    swap(arr, store, hi)
    return store
}