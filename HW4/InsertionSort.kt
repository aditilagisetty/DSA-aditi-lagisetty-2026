/**
 * Sorts [arr] in place using insertion sort.
 *
 * Time: O(n^2) worst/average, O(n) best (already sorted). Space: O(1).
 */
fun insertionSort(arr: IntArray) {
    for (i in 1 until arr.size){
        val key = arr[i]
        var j = i - 1
        // save arr[i] in key because it gets written over when shifting
        // as long as element j is bigger than key move it one slot right to make room
        while (j >= 0 && arr[j] > key){
            arr[j + 1] = arr[j]
            j--
        }
        
        arr[j + 1] = key
    }
}
