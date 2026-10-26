# HW6 Writeup: Deep Dive on Maximum Flow

For this project I wrote Ford-Fulkerson (using DFS) and Edmonds-Karp (using BFS) from scratch in Kotlin. I tested them against JGraphT, used them to put students on project teams, and timed them against each other.

## Reports

| Report | What's in it |
|---|---|
| [`explainer.md`](explainer.md) | How Ford-Fulkerson and Edmonds-Karp work, why they give the right answer, how fast they are, and an example worked out step by step |
| [`application_report.md`](application_report.md) | How putting students on teams becomes a max flow problem, the results for a class of 60, and how the min cut explains who couldn't be placed |
| [`benchmark_report.md`](benchmark_report.md) | Runtime plots, how the results compare to O(VE²) and O(E·F), and how I handled JVM warm-up |

## Files

| File | What's in it |
|------|--------------|
| `FlowNetwork.kt` | The graph. `Edge` (each edge has a matching reverse edge) and `FlowNetwork` (adjacency lists, how much room each edge has left, the min cut) |
| `MaxFlow.kt` | The loop both algorithms share, `FordFulkerson` (DFS) and `EdmondsKarp` (BFS) |
| `TeamMatching.kt` | Builds the student → team network, reads off who goes where and the min cut, and makes up the class |
| `GraphGenerator.kt` | Makes random graphs and the adversarial graph for the benchmark |
| `BenchmarkGraphs.kt` | The three kinds of benchmark graphs and their sizes |
| `Benchmark.kt` | The timing code (warm-up first, then the median of repeated runs) |
| `Main.kt` | Runs the team matching. `counts` also counts paths, and `bench` also counts paths and times everything |
| `HW6Test.kt` | Unit tests: examples worked out by hand, the JGraphT comparison, team matching |
| `plot_results.py` | Makes `results/runtime.png` and `results/augmentations.png` from the CSV files |
| `lib/` | JGraphT 1.5.2 and jheaps 0.14 (which JGraphT needs). Only the tests use these |
| `results/` | Saved output, CSV files and plots |

## How to build and run

You need `kotlinc` and Java 21. You only need Python and matplotlib for the plots. Run everything from the `HW6` folder.

```
# tests (these need the JGraphT jars in lib/)
kotlinc FlowNetwork.kt MaxFlow.kt GraphGenerator.kt BenchmarkGraphs.kt TeamMatching.kt HW6Test.kt -cp lib/jgrapht-core-1.5.2.jar -include-runtime -d test.jar
java -cp "test.jar;lib/jgrapht-core-1.5.2.jar;lib/jheaps-0.14.jar" HW6TestKt     # 36 passed, 0 failed
# (on Mac/Linux use : instead of ; after -cp)

# main program
kotlinc FlowNetwork.kt MaxFlow.kt GraphGenerator.kt BenchmarkGraphs.kt TeamMatching.kt Benchmark.kt Main.kt -include-runtime -d hw6.jar
java -jar hw6.jar           # team matching          -> results/team_matching_output.txt
java -jar hw6.jar counts    # + number of paths      -> results/counts.csv
java -jar hw6.jar bench     # + timings (~2 min)     -> results/benchmarks.csv, results/benchmark_output.txt

# plots
python plot_results.py      # -> results/runtime.png, results/augmentations.png
```

## Testing

`HW6Test.kt` has 36 checks and they all pass.

- Examples worked out by hand, run on both algorithms:
  - the example from the explainer (flow 5; DFS uses 4 paths and BFS uses 3)
  - Figure 26.1 from CLRS (flow 23, and the same min cut as the book)
  - a graph that only gets the right answer if the reverse edge is used
  - no path, a single edge, an edge with capacity 0, two edges between the same vertices, and a small edge in the middle that limits everything
  - running the same network twice
- Every flow gets checked: no edge goes over its capacity, and flow in = flow out at every vertex in the middle. The min cut gets checked to be full and equal to the max flow.
- JGraphT: both of my algorithms get the same answer as JGraphT's `EdmondsKarpMFImpl` and `PushRelabelMFImpl` on 720 random graphs and on the benchmark graphs. The random graphs have 6 to 60 vertices and capacities up to 1, 10 or 1000.
- Team matching:
  - a small example I worked out by hand
  - a case where everyone fits
  - the made-up class, with both algorithms
  - 50 random classes, checking that the min cut explains exactly who wasn't placed

## Changes from my proposal

- No Gradle and no kotlinx-benchmark. I build with plain `kotlinc` like my other homeworks, and JMH needs Gradle. So `Benchmark.kt` handles JVM warm-up itself: it runs everything untimed first, then takes the median of repeated timed runs. The benchmark report explains this.
- I made the plots with a short Python/matplotlib script that reads the CSV files, instead of Lets-Plot. My proposal said either one was fine.
- The adversarial graph isn't the 4-vertex textbook example, because that example doesn't trick a DFS that always tries edges in the same order. I used a "chain-first" graph with huge capacities instead, which makes DFS take long paths that can only carry a little flow. The benchmark report explains why.
- I didn't do the stretch goal (Dinic's algorithm).
