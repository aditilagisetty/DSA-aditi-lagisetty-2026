# HW4 Writeup

## Complexity Analysis

### Insertion Sort
The outer loop always runs n−1 times. The cost depends on the inner loop, and 0 shifts per element on sorted input gives O(n), and i shifts per element on reverse sorted input gives O(n²).

### Merge Sort
There are log n levels of splitting for n elements and one level of merging takes n work. log n levels x n work per level = Θ(n log n). When one half runs out early the merge skips some comparisons, but it still copies every element into temp with the leftover loops and then back into arr. So each level still does Θ(n) work and sorted input is still Θ(n log n). If arr[mid - 1] <= arr[mid], the two halves are already in order, so mergeSortRange could skip the merge. That one check makes sorted input O(n). 

### Quick Sort
If the pivot is always the largest element, partitioning puts all n−1 remaining elements on the left and none on the right. Each level of recursion removes only one element (the pivot), so the total work is n + (n−1) + (n−2) + … + 1 = n(n+1)/2 = Θ(n²). The recursion is also n levels deep, so on a 1,000,000-element sorted array it would most likely crash with a StackOverflowError before it finished. In the best case the pivot is the median, so each partition splits the range into two halves of size n/2. That gives log₂ n levels of recursion, and each level partitions n elements in total, so the cost is Θ(n log n), the same levels × work-per-level argument as merge sort. A random pivot won't land on the median every time, but it doesn't need to. Any split where the smaller side is a constant fraction of n still gives O(log n) levels. For example, a 25%/75% split gives log₄⁄₃ n levels, which is still O(log n). A random pivot lands in the middle half of the values 50% of the time, so the expected runtime is O(n log n). The randomness comes from the algorithm, not the input, so no particular input is consistently bad.

### Heap Sort

## Benchmarking

### Testing Methodology

### List Generation

### Repetition

### Results

| n | Insertion | Merge | Quick | Heap |
|---|-----------|-------|-------|------|
| 10 | | | | |
| 100 | | | | |
| 1,000 | | | | |
| 1,000,000 | | | | |

### Conclusions
