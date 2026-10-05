import kotlin.random.Random

private const val BUCKETS = 16
private const val OVERSAMPLE = 8     // sample this many values per bucket
private const val SMALL_INPUT = 256  // below this

/**
 * Sorts [arr] in place using sample sort, pick splitters from a random sample, distribute
 * every element into the bucket between two splitters then sort each bucket
 *
 * Time: O(n log n) expected. Space: O(n).
 */
fun sampleSort(arr: IntArray) {
    val n = arr.size
    if (n <= SMALL_INPUT) {
        introSort(arr)
        return
    }

    // pick a random sample and sort it
    val sample = IntArray(BUCKETS * OVERSAMPLE) { arr[Random.nextInt(n)] }
    introSort(sample)

    // take evenly spaced splitters from the sorted sample
    val splitters = IntArray(BUCKETS - 1) { i -> sample[(i + 1) * OVERSAMPLE] }

    // count how many elements go in each bucket
    val count = IntArray(BUCKETS)
    for (x in arr) {
        count[findBucket(splitters, x)]++
    }

    // turn counts into starting positions
    val start = IntArray(BUCKETS)
    for (b in 1 until BUCKETS) {
        start[b] = start[b - 1] + count[b - 1]
    }

    // place each element into its bucket's next free slot
    val buffer = IntArray(n)
    val next = start.copyOf()
    for (x in arr) {
        val b = findBucket(splitters, x)
        buffer[next[b]] = x
        next[b]++
    }

    // sort each bucket and copy it back into arr
    for (b in 0 until BUCKETS) {
        val bucket = buffer.copyOfRange(start[b], start[b] + count[b])
        if (count[b] > n / 2) {
            introSort(bucket)
        } else {
            sampleSort(bucket)
        }
        bucket.copyInto(arr, start[b])
    }
}

// which bucket x belongs in
// the index of the first splitter greater than x
private fun findBucket(splitters: IntArray, x: Int): Int {
    var lo = 0
    var hi = splitters.size
    while (lo < hi) {
        val mid = lo + (hi - lo) / 2
        if (splitters[mid] <= x) {
            lo = mid + 1
        } else {
            hi = mid
        }
    }
    return lo
}