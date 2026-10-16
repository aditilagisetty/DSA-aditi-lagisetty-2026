/**
 * Sorts [arr] in place using top down merge sort
 *
 * Time: Θ(n log n) in every case
 * Space: O(n) for the temp buffer
 *
 */

fun mergeSort(arr: IntArray) {
    if (arr.size < 2) return
    val temp = IntArray(arr.size)
    mergeSortRange(arr, temp, 0, arr.size)
}

// sorts arr[lo until hi]
private fun mergeSortRange(arr: IntArray, temp: IntArray, lo: Int, hi: Int) {
    if (hi - lo < 2) return
    val mid = lo + (hi - lo) / 2
    mergeSortRange(arr, temp, lo, mid)
    mergeSortRange(arr, temp, mid, hi)
    merge(arr, temp, lo, mid, hi)
}

// merges the sorted runs arr[lo until mid] and arr[mid until hi]
private fun merge(arr: IntArray, temp: IntArray, lo: Int, mid: Int, hi: Int) {
    var i = lo      // front of left run
    var j = mid     // front of right run
    var k = lo      // next slot to fill in temp

    while (i < mid && j < hi) {
        // compare the front elements of the two runs and copy the smaller one to temp
        // only runs while both halves have elements
        if (arr[i] <= arr[j]) {
            temp[k] = arr[i]
            i++
        } else {
            temp[k] = arr[j]
            j++
        }
        k++
    }

    // while the left half still has elements
    // take the front element from the left half and copy it to temp
    while (i < mid) {
        temp[k] = arr[i]
        i++
        k++
    }
    // while the right half still has elements
    // take the front element from the right half and copy it to temp
    while (j < hi) {
        temp[k] = arr[j]
        j++
        k++
    }

    // when one half runs out the rest of the other half is alr sorted
    // copy it over
    for (x in lo until hi) {
        arr[x] = temp[x]
    }

}

