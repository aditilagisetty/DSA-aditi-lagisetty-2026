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

### Radix Sort

### Sample Sort

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
