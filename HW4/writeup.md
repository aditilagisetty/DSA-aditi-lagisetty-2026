# HW4 Writeup

## Complexity Analysis

| Sort | Best | Average | Worst | Extra space | Stable |
|------|------|---------|-------|-------------|---------|
| Insertion | O(n) | O(n²) | O(n²) | O(1) | yes |
| Merge | Θ(n log n) | Θ(n log n) | Θ(n log n) | O(n) | yes |
| Quick (random pivot) | O(n log n) | O(n log n) expected | O(n²) | O(log n) expected | no |
| Heap | Θ(n log n) | Θ(n log n) | Θ(n log n) | O(1) | no |

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

## Benchmarking

### Testing Methodology

### List Generation

### Repetition

### Results

| n | Insertion | Merge | Quick | Heap | Intro | Radix | Sample |
|---|-----------|-------|-------|------|-------|-------|--------|
| 10 | | | | | | | |
| 100 | | | | | | | |
| 1,000 | | | | | | | |
| 1,000,000 | | | | | | | |

### Conclusions
