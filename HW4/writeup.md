# HW4 Writeup

## Complexity Analysis

| Sort | Best | Average | Worst | Extra space | Stable |
|------|------|---------|-------|-------------|---------|
| Insertion | O(n) | O(n²) | O(n²) | O(1) | yes |
| Merge | Θ(n log n) | Θ(n log n) | Θ(n log n) | O(n) | yes |
| Quick (random pivot) | O(n log n) | O(n log n) expected | O(n²) | O(log n) expected | no |
| Heap | Θ(n log n) | Θ(n log n) | Θ(n log n) | O(1) | no |
| Intro | O(n log n) | O(n log n) | O(n log n) | O(log n) | no |
| Radix | Θ(n) | Θ(n) | Θ(n) | O(n) | yes |
| Sample | O(n log n) | O(n log n) expected | O(n log n) | O(n) | no |

### Insertion Sort
Insertion sort works like sorting a hand of cards: take the next element and slide it left until it sits next to something smaller. The outer loop always runs n−1 times, so the cost depends on the inner loop. On sorted input the inner loop does 1 comparison and 0 shifts per element, which gives O(n). On reverse sorted input element i has to shift i times, so the total is 1 + 2 + … + (n−1) = n(n−1)/2 = O(n²). On random input each element shifts about halfway, which is still O(n²). It sorts in place, so it uses O(1) extra space. It's stable because the inner loop uses `>` and not `>=`, so equal values never pass each other.

### Merge Sort
Merge sort splits the array in half, sorts each half recursively, then merges the two sorted halves. There are log n levels of splitting for n elements and one level of merging takes n work, so log n levels × n work per level = Θ(n log n). When one half runs out early the merge skips some comparisons, but it still copies every element into temp with the leftover loops and then back into arr. So each level still does Θ(n) work and sorted input is still Θ(n log n). It needs an O(n) temp buffer, which I allocate once and reuse for every merge. It's stable because the merge uses `<=`, so on a tie the left element goes first.

A possible improvement: if `arr[mid - 1] <= arr[mid]`, the two halves are already in order, so `mergeSortRange` could skip the merge. That one check makes sorted input O(n).

### Quick Sort
Quick sort picks a pivot, partitions the range so smaller values go left and larger values go right, then recurses on each side. I used Lomuto partitioning with a random pivot.

If the pivot were always the last element and the input were already sorted, the pivot would always be the largest element. Partitioning would put all n−1 remaining elements on the left and none on the right. Each level of recursion removes only one element (the pivot), so the total work is n + (n−1) + (n−2) + … + 1 = n(n+1)/2 = Θ(n²). The recursion would also be n levels deep, so on a 1,000,000-element sorted array it would most likely crash with a StackOverflowError before it finished.

In the best case the pivot is the median, so each partition splits the range into two halves of size n/2. That gives log₂ n levels of recursion, and each level partitions n elements in total, so the cost is Θ(n log n), the same levels × work-per-level argument as merge sort. A random pivot won't land on the median every time, but it doesn't need to. Any split where the smaller side is a constant fraction of n still gives O(log n) levels. For example, a 25%/75% split gives log₄⁄₃ n levels, which is still O(log n). A random pivot lands in the middle half of the values 50% of the time, so the expected runtime is O(n log n). The randomness comes from the algorithm, not the input, so no particular input (including sorted input) is consistently bad.

**Space:** no temp array, but the recursion stack is O(log n) deep on average (O(n) in the worst case).

### Heap Sort
Heap sort works in two phases. First it builds a max heap inside the array so the biggest value is at index 0. Then it repeatedly swaps the root (the current max) to the end of the heap, shrinks the heap by one, and sifts the new root down to fix the heap. The sorted part grows from the right end of the array.

A heap with n elements is about log n levels tall, so each siftDown can swap at most log n times (from the root down to a leaf). Phase 2 calls siftDown n−1 times, so that's n × log n = O(n log n). Phase 1 (building the heap) is actually only O(n), because most nodes are near the bottom and barely have to move, but even if you count it as n log n the total is still Θ(n log n).

If the array is already sorted (smallest to largest), phase 1 still has to flip it into a max heap, so the biggest values get moved to the front. Then phase 2 pulls them off one by one and each new root has to sift all the way back down. So the work doesn't change and heap sort is Θ(n log n) in every case (best, worst, and average). It doesn't take advantage of sorted input the way insertion sort does.

**Space:** heap sort doesn't use recursion and doesn't need a temp array. It just swaps things around inside the original array, so it only uses a few variables (i, left, right, largest), which is O(1) extra space. Merge sort needs O(n) for the temp buffer, and quick sort needs O(log n) for the recursion stack (and up to O(n) in its worst case). So heap sort is the most memory efficient of the three. It's not stable, because swapping the root with the last element can move equal values out of their original order.

## Extra Credit Sorts

### Introsort
Introsort combines quick sort, heap sort, and insertion sort, which is roughly what C++'s std::sort does. It runs quick sort, but counts how deep the recursion goes. A good quick sort is about log n levels deep, so if the depth reaches 2 × log₂ n the pivots are going badly and that range switches to heap sort. Ranges of 16 or fewer elements are skipped, and one insertion sort pass at the end finishes them.

**Runtime:** the quick sort part is O(n log n) because the depth is capped at 2 log n and each level does O(n) partitioning work. The final insertion sort pass is O(n) when quick sort stops on a small range, every value in it already belongs in that range, so every element is fewer than 16 slots from its final spot and shifts at most 15 times. That makes the worst case O(n log n). Space is O(log n) for the recursion stack, since the depth is capped.

**Why have the heap sort fallback if the random pivot already makes O(n²) super unlikely?** Because "almost never" isn't "never." A random pivot makes the expected runtime O(n log n), but there's still a tiny chance of picking bad pivots over and over, and a library sort gets called billions of times so eventually someone hits it. The heap sort fallback makes the worst case a guaranteed O(n log n) no matter what the input is or how unlucky the pivots are, and you still get quick sort's speed in the normal case since the fallback almost never actually runs.

### Radix Sort
Radix sort never compares two values. It sorts by digits, starting with the least significant one (LSD radix sort). I used bytes as the digits, so each digit has 256 possible values and a 32-bit Int takes exactly 4 passes. Each pass is a counting sort: count how many numbers have each digit value, turn the counts into starting positions, then place each number at its digit's next free slot. Each pass has to be stable, otherwise the earlier passes' work gets messed up. To handle negative numbers I flip the sign bit before reading the digits, so negatives sort before positives.

**Runtime:** each pass loops over the array a couple of times (O(n)) plus loops over the 256 digit values (O(256)), so one pass is O(n + 256). There are always 4 passes for 32-bit ints, so the total is 4 × O(n + 256) = Θ(n). It's the same for sorted, reverse sorted, or random input. Space is O(n) for the buffer plus two arrays of 256 counts. It's stable.

**Doesn't sorting have to be at least n log n?** That limit only applies to comparison sorts, meaning sorts that only learn about the data by asking "is a < b?". There are n! possible orderings of n items, and each comparison only gives a yes or no, so you need at least log₂(n!) ≈ n log n comparisons to figure out which ordering you have. Radix sort never compares two values. It looks at the digits directly and uses them as array indexes, so the n log n limit doesn't apply. The catch is that it only works because the keys are fixed-size ints with a known number of digits.

**When radix sort is a bad choice:**
- Tiny arrays: every pass makes two 256-slot arrays and loops over them, no matter how small the input is. For n = 10 that's way more work than just sorting 10 numbers, so insertion sort would win.
- Keys that aren't fixed-size ints: the number of passes depends on how many digits the keys have. Long strings would need a pass per character, and doubles need extra bit tricks. If the keys have k digits the runtime is really O(k × n), so with long keys that k can be bigger than log n.
- Memory: it needs an O(n) buffer, unlike heap sort or insertion sort, which sort in place.
- Custom orderings: comparison sorts work with any rule for less than, but radix sort needs keys that break down into digits.

### Sample Sort
Sample sort is like quick sort but with a bunch of pivots instead of one. It splits the array into 16 buckets at once using 15 splitters, then sorts each bucket. To pick good splitters it takes a random sample of 128 values, sorts the sample, and takes every 8th value. Then each element gets put in its bucket with a binary search over the splitters, using the same count → starting positions → place pattern as radix sort. Small inputs and badly split buckets go to introsort.

Runtime: each level of recursion does O(n) work to put every element in a bucket. Each level divides the array into 16 buckets instead of 2, so there are about log₁₆ n levels instead of log₂ n. For n = 1,000,000 that's about 5 levels instead of about 20. But log₁₆ n = log₂ n / 4, which is still O(log n), so the total is O(n log n) expected. It's expected because the splitters come from a random sample, so the buckets are only roughly equal on average. Space is O(n) for the buffer and the bucket copies.

**What happens on 1,000,000 copies of 7 without the count[b] > n / 2 guard?** Every value in the sample is 7, so all 15 splitters are 7. Every element is ≥ every splitter, so all 1,000,000 elements go in the last bucket. Then sampleSort gets called on that bucket, which is the exact same array, so the same thing happens again, forever. It never gets smaller, so it recurses until it crashes with a StackOverflowError. The guard catches it, if a bucket has more than half the elements the splitting clearly isn't working, so that bucket goes to introsort. Introsort handles it fine because all the equal values make the partitions super lopsided, so it hits the depth limit and falls back to heap sort. I tested all 7s, all 7s except two values, and 1,000,000 values that are only 0, 1, or 2, and they all sort correctly.

**Why is sample sort popular for parallel sorting?** Once the elements are in their buckets, every bucket is completely independent. Everything in bucket 0 is smaller than everything in bucket 1, so no merging is needed afterward. That means you can give each bucket to a different CPU core or a different machine and sort them all at the same time. Quick sort only splits into 2 at a time, so at the start there's only 1 or 2 pieces of work to share between cores. Merge sort's halves are independent too, but the final merges still need one big O(n) pass at the end that's hard to split up. Sample sort splits into a lot of pieces right away, which keeps all the cores busy, and it only has to move data between machines once.

## Unit Tests

All 7 sorts are tested in HW4Test.kt. Every sort runs on 11 inputs: empty, one element, two elements, already sorted, reverse sorted, all duplicates, only 3 distinct values, negatives, Int.MIN_VALUE/Int.MAX_VALUE, 20 random values, and 5,000 random values. Each result is compared to Kotlin's built-in.

## Benchmarking

### Testing Methodology
Every sort was timed at n = 10, 100, 1,000, 10,000, 100,000, and 1,000,000. I added 10,000 and 100,000 to the required sizes so it's easier to see how the times grow. Each run is timed with Kotlin's measureTime, and only the sort is timed.

Before timing anything the benchmark does a warm-up, it runs every sort 5 times on 10,000 random values. The JVM starts out running code slowly and then compiles the hot parts into fast machine code while the program runs, so without a warm-up whichever sort ran first would look unfairly slow.

### List Generation
I tested two kinds of input:
- Random: IntArray(n) { Random.nextInt() }, which gives random ints across the whole Int range, so negatives are included and there are basically no duplicates.
- Already sorted: IntArray(n) { it }, which is 0, 1, 2, …, n−1. This shows how each sort handles its best or worst case.

In each trial a new input is generated and every sort gets a copy of the same input.

### Repetition
Small sizes finish in microseconds, so one measurement is mostly noise. Bigger sizes are more stable but take longer. So the number of trials depends on n.

| n | trials |
|---|--------|
| 10 – 10,000 | 100 |
| 100,000 | 5 |
| 1,000,000 | 3 (insertion sort took ~70 seconds per run here) |

I report the median of the trials instead of the average, because one weird slow run would throw off an average but doesn't affect the median that much.

### Results

**Random input (median ms)**

| n | insertion | merge | quick | heap | intro | radix | sample |
|---|---|---|---|---|---|---|---|
| 10 | 0.0005 | 0.0012 | 0.0010 | 0.0014 | 0.0004 | 0.0096 | 0.0004 |
| 100 | 0.0027 | 0.0079 | 0.0066 | 0.0067 | 0.0047 | 0.0124 | 0.0046 |
| 1,000 | 0.0706 | 0.0939 | 0.0795 | 0.0904 | 0.0599 | 0.0180 | 0.1117 |
| 10,000 | 6.1956 | 1.2006 | 1.0373 | 1.1861 | 0.8575 | 0.1217 | 1.9680 |
| 100,000 | 693.3723 | 17.2881 | 13.6866 | 16.4617 | 12.7617 | 1.2566 | 26.1125 |
| 1,000,000 | 69842.7416 | 177.7103 | 153.5577 | 203.0313 | 128.9949 | 13.0782 | 315.5013 |

**Already sorted input (median ms)**

| n | insertion | merge | quick | heap | intro | radix | sample |
|---|---|---|---|---|---|---|---|
| 10 | 0.0001 | 0.0003 | 0.0005 | 0.0002 | 0.0001 | 0.0045 | 0.0001 |
| 100 | 0.0002 | 0.0020 | 0.0037 | 0.0018 | 0.0014 | 0.0051 | 0.0013 |
| 1,000 | 0.0014 | 0.0283 | 0.0468 | 0.0600 | 0.0179 | 0.0242 | 0.0405 |
| 10,000 | 0.0087 | 0.2308 | 0.3623 | 0.6726 | 0.1500 | 0.1639 | 0.4307 |
| 100,000 | 0.1318 | 3.9128 | 5.8319 | 7.7334 | 1.9740 | 3.1127 | 8.0872 |
| 1,000,000 | 1.3328 | 40.0959 | 52.8448 | 100.8255 | 29.4884 | 45.8258 | 77.7199 |

**Does the growth match the Big-O?** When n goes up 10×, an O(n) sort should take about 10× longer, an O(n log n) sort about 12× longer (from 100,000 to 1,000,000 it's 10 × log(1,000,000)/log(100,000) = 10 × 1.2), and an O(n²) sort about 100× longer. Here's the ratio of the 1,000,000 time to the 100,000 time on random input:

| sort | expected | measured |
|------|----------|----------|
| insertion | ~100× (n²) | 69842.7 / 693.4 = **100.7×** |
| merge | ~12× (n log n) | 177.7 / 17.3 = **10.3×** |
| quick | ~12× (n log n) | 153.6 / 13.7 = **11.2×** |
| heap | ~12× (n log n) | 203.0 / 16.5 = **12.3×** |
| intro | ~12× (n log n) | 129.0 / 12.8 = **10.1×** |
| radix | ~10× (n) | 13.08 / 1.26 = **10.4×** |
| sample | ~12× (n log n) | 315.5 / 26.1 = **12.1×** |

They all seem to make sense!

### Conclusions
- **Insertion sort is O(n²) on random input, and it shows.** At 1,000,000 it took ~70 seconds, while every other sort finished in under a third of a second. But it's actually the fastest at n = 10, because it has no recursion, no extra arrays. And on already sorted input it's the fastest sort at every size, which matches its O(n) best case. Best use: tiny arrays or nearly sorted data, which is exactly why introsort uses it for small ranges.
- **Radix sort is the fastest on large random input by far**: 13 ms at 1,000,000, about 10× faster than the best comparison sort. That's its Θ(n) beating the rests else's n log n. But at n = 10 it's the slowest, because it always loops over 256 digit values in each of its 4 passes no matter how small the input is. It also didn't speed up on sorted input; it was actually slower. Best use: large arrays of fixed-size integer keys.
- **Introsort was the fastest comparison sort** at almost every size, for both random and sorted input. It has a O(n log n) worst case thanks to the heap sort fallback. Best use: general purpose sorting.
- **Quick sort** was the fastest of the four basic n log n sorts on random input. The random pivot did its job on sorted input too: no O(n²) and no stack overflow, and it actually ran faster because sorted data makes the comparisons predictable for the CPU. **Best use:** fast general purpose sorting.
- **Merge sort** was a bit slower than quick sort on random input, but it's the only n log n sort here that has a guaranteed Θ(n log n) worst case. It costs O(n) extra memory. Best use: when you need stability or a guaranteed worst case.
- **Heap sort** was the slowest of the basic n log n sorts, especially on sorted input. It jumps all over the array, which is bad for the CPU cache, while merge and quick sort mostly walk through memory in order. Its strengths are O(1) extra memory and a guaranteed Θ(n log n). Best use: when memory is tight and you need a guaranteed worst case
- **Sample sort was the slowest n log n sort** which was a surprise to me. In my single-threaded it does a binary search over the splitters twice per element per level, copies everything into a buffer, copies each bucket out, and copies it back. Its real advantage is that the buckets are independent and can be sorted at the same time on different cores, which I didn't do. At n = 10 and 100 it matches introsort because it just calls introsort for inputs of 256 or less. Best use: parallel or distributed sorting, not single-threaded.
- **At n = 10 and 100 the choice basically doesn't matter.** Everything finished in a few microseconds or less, and these times are close to the limit of what the timer can measure reliably. Big-O only really starts to matter around n = 10,000 and up.

**Limitations:** all results come from one machine. At 1,000,000 I only ran 3 trials because insertion sort takes ~70 seconds each time. I only tested random and sorted inputs, not reverse sorted or inputs with lots of duplicates.
