# HW4 Writeup

## Complexity Analysis

### Insertion Sort
The outer loop always runs n−1 times. The cost depends on the inner loop, and 0 shifts per element on sorted input gives O(n), and i shifts per element on reverse sorted input gives O(n²).

### Merge Sort
There are log n levels of splitting for n elements and one level of merging takes n work. log n levels x n work per level = Θ(n log n). When one half runs out early the merge skips some comparisons, but it still copies every element into temp with the leftover loops and then back into arr. So each level still does Θ(n) work and sorted input is still Θ(n log n). If arr[mid - 1] <= arr[mid], the two halves are already in order, so mergeSortRange could skip the merge. That one check makes sorted input O(n). 

### Quick Sort

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
