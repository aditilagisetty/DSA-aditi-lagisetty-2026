/**
 * Sorts [arr] in place using heap sort
 *
 * Time: Θ(n log n) in every case. Space: O(1). Not stable.
 */
fun heapSort(arr: IntArray) {
    val n = arr.size

    // n / 2 - 1 is the index of the last node that has a child
    for (i in n / 2 - 1 downTo 0) {
        siftDown(arr, i, n)
    }

    // end is the last slot of the current heap 
    // swapping index 0 with end puts the current 
    // max into its final spot
    for (end in n - 1 downTo 1) {
        swap(arr, 0, end)
        siftDown(arr, 0, end)
    }
}

// pushes arr[start] down until it is >= both children
// only looking at arr[0 until size]
private fun siftDown(arr: IntArray, start: Int, size: Int) {
    var i = start
    while (true) {
        val left = 2 * i + 1
        val right = 2 * i + 2
        var largest = i

        if (left < size && arr[left] > arr[largest]) {
            largest = left
        }
        if (right < size && arr[right] > arr[largest]) {
            largest = right
        }

        if (largest == i) {
            break
        }

        swap(arr, i, largest)
        i = largest
    }
}
