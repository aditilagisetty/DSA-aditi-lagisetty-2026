/**
 * Sorts [arr] in place using radix sort, one byte at a time.
 *
 * Time: Θ(n) for 32-bit ints. Space: O(n). Stable.
 */

fun radixSort(arr: IntArray) {
    if (arr.size < 2) return
    val buffer = IntArray(arr.size)

    for (shift in 0 until 32 step 8) {
        // count how many numbers have each digit value
        val count = IntArray(256)
        for (x in arr) {
            count[digit(x, shift)]++
        }

        // turn counts into starting positions
        val start = IntArray(256)
        for (d in 1 until 256) {
            start[d] = start[d - 1] + count[d - 1]
        }

        // place each number at its digit's next free slot
        for (x in arr) {
            val d = digit(x, shift)
            buffer[start[d]] = x
            start[d]++
        }

        buffer.copyInto(arr)
    }
}

// the byte of x at this shift, with the sign bit flipped so negatives sort before positives
private fun digit(x: Int, shift: Int): Int = ((x xor Int.MIN_VALUE) ushr shift) and 0xFF
