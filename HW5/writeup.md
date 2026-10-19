# HW5 Writeup

## Files

| File | What's in it |
|------|--------------|
| `Matrix.kt` | The matrix class and the three ways to multiply |
| `SequenceAlignment.kt` | Needleman-Wunsch and Smith-Waterman |
| `Genome.kt` | The Salmonella sequences from the course sample repo |
| `Benchmark.kt` | Timing code for matrix multiplication |
| `HW5Test.kt` | Unit tests for both parts |
| `Main.kt` | Runs the alignments, and the benchmarks with `bench` |
| `alignment_output.txt`, `benchmark_output.txt` | Saved output |

```
kotlinc Matrix.kt SequenceAlignment.kt Genome.kt HW5Test.kt -include-runtime -d test.jar
java -cp test.jar HW5TestKt            # 43 passed, 0 failed

kotlinc Main.kt Matrix.kt Benchmark.kt SequenceAlignment.kt Genome.kt -include-runtime -d hw5.jar
java -jar hw5.jar                      # alignments
java -Xmx2g -jar hw5.jar bench         # alignments + benchmarks
```

## Part 1: Matrix Class

I store the matrix as one long `DoubleArray(n*n)`, one row after another. Entry (r, c) is at `data[r*n + c]`. Each row sits together in memory, so `split()` can copy a row of a block in one step.

- `m[r, c]` reads a value and `m[r, c] = x` sets one
- `split()` cuts the matrix into four copies of size n/2: `[A11, A12, A21, A22]`
- `Matrix.combine` glues four blocks back together
- `+`, `-`, and `*` work on matrices (`*` uses the hybrid multiply)

## Part 2: Strassen's Algorithm

### Implementation

`multiply` is the normal way you learn in math class, with three nested loops. I put the loops in the order i, k, j instead of i, j, k. That way the inner loop moves along a row instead of down a column, and that's faster because rows sit together in memory. It's Θ(n³).

`strassenMultiply` follows the Wikipedia version. It splits A and B into four blocks each and does 7 block multiplies instead of 8:

```
M1 = (A11 + A22)(B11 + B22)     C11 = M1 + M4 - M5 + M7
M2 = (A21 + A22) B11            C12 = M3 + M5
M3 = A11 (B12 - B22)            C21 = M2 + M4
M4 = A22 (B21 - B11)            C22 = M1 - M2 + M3 + M6
M5 = (A11 + A12) B22
M6 = (A21 - A11)(B11 + B12)
M7 = (A12 - A22)(B21 + B22)
```

So T(n) = 7T(n/2) + Θ(n²). By the master theorem with a = 7 and b = 2, that's Θ(n^log₂7) ≈ Θ(n^2.807).

Strassen needs to cut the matrix in half at every level. If n isn't a power of two, I fill the matrix out with zeros up to the next power of two, multiply, then cut the answer back down to n × n. The extra zeros don't change the answer. All three multiplies return `null` if the sizes don't match.

`strassenMultiply` keeps splitting all the way down to 1 × 1. `hybridMultiply(other, cutoff)` does the same thing but switches to normal `multiply` once the blocks are `cutoff` × `cutoff` or smaller.

### Testing

`HW5Test.kt` tests:

- get/set, split/combine (and that split makes copies), `+`, `-`
- a 2 × 2 product I worked out by hand
- `null` when the sizes don't match
- 0 × 0 and 1 × 1 matrices
- A × I = A, I × A = A, and A × 0 = 0
- random matrices of **every size from 1 to 40** (so lots of sizes that need padding), checking all three multiplies (and the hybrid with several cutoffs) against a simple, obviously correct version. The entries are small whole numbers, so the answers should match exactly.
- a 128 × 128 test with decimal numbers. This one allows tiny differences, because Strassen adds things in a different order and the rounding comes out slightly different.

The A × 0 = 0 test found a bug in my `equals`. Strassen can produce `-0.0`, and `DoubleArray.contentEquals` treats `-0.0` and `0.0` as different. I changed `equals` to compare the numbers with `==`.

### Benchmark results

Each time is the median of several runs on random matrices, in milliseconds on my laptop.

| n | multiply | strassenMultiply (to 1×1) | hybridMultiply (cutoff 128) |
|---|---|---|---|
| 16 | 0.002 | 0.136 | 0.002 |
| 32 | 0.009 | 0.985 | 0.011 |
| 64 | 0.065 | 6.652 | 0.072 |
| 100 | 0.244 | 44.321 | 0.523 |
| 128 | 0.537 | 40.726 | 0.521 |
| 256 | 5.362 | 370.675 | 4.280 |
| 512 | 41.525 | 2611.886 | 37.832 |
| 1024 | 369.267 | (too slow) | 285.618 |
| 2048 | 7883.204 | (too slow) | 2024.716 |

Pure Strassen is **60–100× slower** than normal multiplication at every size. It does fewer multiplications, but going all the way down to 1 × 1 means that for every single number multiplied, it also does about 18 matrix additions and creates new matrix objects. That extra work costs way more than it saves. At n = 512 it does about 40 million tiny multiplies (7⁹), so I didn't run it at 1024.

### Finding the crossover

To find the best cutoff, I tried cutoffs from 8 to 512:

| n | multiply | cutoff 8 | cutoff 16 | cutoff 32 | cutoff 64 | cutoff 128 | cutoff 256 | cutoff 512 |
|---|---|---|---|---|---|---|---|---|
| 256 | 5.057 | 19.965 | 15.983 | 13.773 | 8.959 | 9.849 | n/a | n/a |
| 512 | 52.569 | 118.376 | 73.632 | 55.643 | 42.286 | 33.994 | 36.698 | n/a |
| 1024 | 333.100 | 927.712 | 581.412 | 397.818 | 330.572 | 278.950 | 280.650 | 269.608 |
| 2048 | 8324.046 | 6782.373 | 4501.207 | 3200.807 | 2329.318 | 2069.674 | 2239.533 | 2055.118 |

**What I found:**

- **Small cutoffs are slow.** With a cutoff of 8 or 16, the hybrid is slower than normal `multiply` everywhere except n = 2048. When the blocks are that small, the extra adding and copying costs more than the multiply it saves.
- **The best cutoff is about 128.** It was the fastest at n = 512 and n = 2048. At n = 1024, cutoffs from 128 to 512 were all within about 4%, which is about how much the times change between runs anyway. Going below 128 clearly makes things slower. So I set `DEFAULT_CUTOFF = 128`.
- **When the hybrid helps.** With cutoff 128, the hybrid ties normal `multiply` at n = 128 and is faster from n = 256 on: 1.1× faster at 512, 1.3× at 1024, and **3.9× at 2048**. Each Strassen level saves 1 out of 8 multiplies, so at 1024 (3 levels) the best you could hope for is about 1.5× (8/7 × 8/7 × 8/7). I got 1.3× because of the extra additions.
- **Why 2048 is so much faster.** This is mostly about memory, not math. Doubling n should make normal multiply 8× slower, but going from 1024 to 2048 made it about 21× slower. A 2048 × 2048 matrix is 32 MB, which is too big for the CPU's fast memory (the cache), so the computer keeps waiting on slower memory. Strassen copies its blocks into small new matrices, and by the 128 × 128 level all the data fits in the cache. A lot of the speedup comes from that.
- **Sizes that aren't powers of two cost extra.** At n = 100, the hybrid is about 2× slower than `multiply`, because padding 100 up to 128 roughly doubles the work. A fix would be to skip Strassen when n is already below the cutoff.

**Conclusion:** use normal multiplication for matrices up to about 128 × 128, and Strassen for bigger ones. On my laptop the hybrid starts winning around n = 256, and it's a lot faster by n = 2048.

## Part 3: Sequence Alignment

I did both Needleman-Wunsch (global, which lines up the whole of both sequences) and Smith-Waterman (local, which finds the best matching piece) so I could run both of the suggested tests.

### How it works

Both fill in a table where `score[i][j]` is the best score for lining up the first i letters of one sequence with the first j letters of the other. Each cell picks the best of three moves:

- **diagonal:** line up the two letters (+ match or − mismatch)
- **up:** put a gap in the second sequence (− gap)
- **left:** put a gap in the first sequence (− gap)

This is dynamic programming because each cell only needs answers that are already in the table. The table has about m × n cells and each one is quick, so it's **Θ(mn) time and Θ(mn) space**.

- **Needleman-Wunsch:** the first row and column start at 0, −1, −2, … (lining letters up with nothing). The answer is the bottom right cell.
- **Smith-Waterman:** a cell can't go below 0, so a bad section doesn't ruin a good match after it. The answer is the **biggest cell anywhere** in the table.

**Backtracing:** starting from the answer, look at which of the three moves gave that score, take that step, and write down the letters. Needleman-Wunsch goes all the way back to the top left corner. Smith-Waterman stops when it reaches a 0. The result comes out backwards, so I reverse it at the end.

**Scoring:** Needleman-Wunsch uses +1 for a match, −1 for a mismatch, and −1 for a gap. Smith-Waterman uses +3 / −3 / −2. Both match the examples on Wikipedia.

### Testing

- The Wikipedia examples: Needleman-Wunsch on `GCATGCG` / `GATTACA` scores 0. Smith-Waterman on `TGTTACGG` / `GGTTGACTA` scores 13 and gives `GTT-AC` / `GTTGAC`.
- Simple cases: identical strings, empty strings, one gap, nothing in common, and finding `GATTACA` hidden inside a longer string.
- **Brute force:** for 150 random short DNA strings, I checked the score against a slow version that tries every possible alignment.
- **Checking the backtrace:** taking out the `-`s gives back the original sequences, and adding up the columns gives the same score. I ran this check on the real genomes too.

### Results on the Salmonella sequences

The full output is in `alignment_output.txt`.

**Needleman-Wunsch, `genomeSnippet` vs `testAgainst`** (both 368 letters long):

```
score: 143
columns: 389, matches: 266, gaps: 42, identity: 68.4%

TGGCGACAACCG-TAGCGGAATATTTTCGCGACCAGGGAAAACGGGTCGTGCTT-TTTAT
|||| || | || |||| |||| |||||||||  | ||||| || ||||| |||  ||
TGGCCACCA-CGATAGCAGAATTTTTTCGCGATAATGGAAAGCGAGTCGT-CTTGCTTGC
...
TGTACT-GAAAAGCGTAAGCCGCGTTTTT
 ||  | | || || | ||||||||||||
CGTGTTGGCAACGC-TCAGCCGCGTTTTT
```

The two sequences are clearly related. 68% of the columns match, and there are long matching runs like `TTTTATACGGTACT`. Random DNA would only match about 25% of the time.

**Smith-Waterman, `testAgainst` vs `targetGenome`** (6,387 letters long):

```
score: 482
columns: 396, matches: 269, gaps: 56, identity: 67.9%
query  range: 0..368
target range: 398..766
```

The best match uses the **whole query** and is at **positions 398–766 of `targetGenome`**. I checked that this part of `targetGenome` is exactly `genomeSnippet`. So Smith-Waterman found where `testAgainst` fits in the big genome on its own, and it's the same spot `genomeSnippet` comes from. The best matching sequence in the target:

```
TGGCGACAACCGTAGCGGAATATTTTCGCGACCAGGGAAAACGGGTCGTGCTTTTTATCGATTCCATGACCCGTTATGCG
CGTGCTTTGCGAGACGTGGCACTGGCGTCGGGAGAGCGTCCGGCTCGTCGAGGTTATCCCGCCTCCGTATTCGATAATTT
GCCCCGCTTGCTGGAACGCCCAGGGGCGACCAGCGAGGGAAGCATTACTGCCTTTTATACGGTACTGCTGGAAAGCGAGG
AAGAGGCGGACCCGATGGCGGATGAAATTCGCTCTATCCTTGACGGTCACCTGTATCTGAGCAGAAAGCTGGCCGGGCAG
GGACATTACCCGGCAATCGATGTACTGAAAAGCGTAAGCCGCGTTTTT
```

The table for this was about 2.4 million cells (6,388 × 369), and it still ran in under a second.
