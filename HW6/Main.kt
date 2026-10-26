import java.io.File

/**
 * Main.kt
 *
 * Compile: kotlinc FlowNetwork.kt MaxFlow.kt GraphGenerator.kt BenchmarkGraphs.kt TeamMatching.kt Benchmark.kt Main.kt -include-runtime -d hw6.jar
 * Run:     java -jar hw6.jar              team matching on the synthetic class
 *          java -jar hw6.jar counts       + count augmenting paths on the benchmark graphs (results/counts.csv)
 *          java -jar hw6.jar bench        + time both algorithms on the benchmark graphs (results/benchmarks.csv)
 */
fun main(args: Array<String>) {
    runTeamMatching()
    if ("counts" in args || "bench" in args) runAugmentationCounts(File("results/counts.csv"))
    if ("bench" in args) runBenchmarks(File("results/benchmarks.csv"))
}

fun runTeamMatching() {
    val (students, teams) = SyntheticClass.generate()
    val result = TeamMatcher(students, teams).solve()

    println("=== Team matching: ${students.size} students, ${teams.size} teams of ${teams.first().size} ===")
    println()
    println("Demand (how many students ranked each team):")
    for (team in teams) {
        val ranked = students.count { team.name in it.rankedTeams }
        val first = students.count { it.rankedTeams.first() == team.name }
        println("  %-18s ranked by %2d (first choice of %2d)".format(team.name, ranked, first))
    }
    println()
    println("Max flow = ${result.maxFlow}, so ${result.maxFlow} of ${students.size} students get a team they ranked.")
    println()

    println("Assignments:")
    for (team in teams) {
        val members = result.assignment.filterValues { it == team.name }.keys
        println("  %-18s (%d/%d) %s".format(team.name, members.size, team.size, members.joinToString(", ")))
    }
    println()

    val rankCounts = IntArray(3)
    for (student in students) {
        val team = result.assignment[student.name] ?: continue
        rankCounts[student.rankedTeams.indexOf(team)]++
    }
    println("Got 1st choice: ${rankCounts[0]}, 2nd: ${rankCounts[1]}, 3rd: ${rankCounts[2]}, unplaced: ${result.unplaced.size}")
    println()

    if (result.unplaced.isEmpty()) {
        println("Everyone was placed, so the min cut is just {source} and there's nothing to explain.")
        return
    }

    println("Unplaced students:")
    for (name in result.unplaced) {
        val student = students.first { it.name == name }
        println("  $name ranked ${student.rankedTeams.joinToString(", ")}")
    }
    println()

    val blockedSeats = teams.filter { it.name in result.blockedTeams }.sumOf { it.size }
    val freeTeams = teams.filter { it.name !in result.blockedTeams }
    println("Min cut (what's still reachable from the source after max flow):")
    println("  ${result.blockedTeams.size} teams: ${result.blockedTeams.joinToString(", ")}")
    println("  ${result.blockedStudents.size} students, every one of whom ranked ONLY those teams:")
    println("    ${result.blockedStudents.joinToString(", ")}")
    println("  Those teams have $blockedSeats seats in total, so ${result.blockedStudents.size} - $blockedSeats = " +
        "${result.blockedStudents.size - blockedSeats} students can't all fit, whatever the assignment.")
    println("  Teams outside the cut (nobody in the group above ranked them):")
    for (team in freeTeams) {
        val members = result.assignment.values.count { it == team.name }
        println("    %-18s %d/%d seats used".format(team.name, members, team.size))
    }
}

/**
 * Augmenting path counts for the benchmark graph families. These are exact and
 * deterministic so they show the shape of the theory without any JVM timing noise
 */
fun runAugmentationCounts(out: File) {
    out.parentFile?.mkdirs()
    val lines = mutableListOf("family,V,E,maxflow,ff_augmentations,ek_augmentations,ek_bound_VE_over_2")
    val families = listOf(
        "sparse" to BenchmarkGraphs.SPARSE_SIZES,
        "dense" to BenchmarkGraphs.DENSE_SIZES,
        "adversarial" to BenchmarkGraphs.ADVERSARIAL_SIZES,
    )
    println()
    println("=== Augmenting paths per algorithm ===")
    for ((family, sizes) in families) {
        for (v in sizes) {
            val problem = BenchmarkGraphs.make(family, v)
            val ff = FordFulkerson().maxFlow(problem.network, problem.source, problem.sink)
            val ek = EdmondsKarp().maxFlow(problem.network, problem.source, problem.sink)
            check(ff.value == ek.value) { "algorithms disagree on $family V=$v" }
            val e = problem.network.edgeCount
            val bound = v.toLong() * e / 2
            println("%-12s V=%5d E=%6d flow=%8d  FF paths=%8d  EK paths=%5d  (EK bound VE/2 = %d)"
                .format(family, v, e, ff.value, ff.augmentations, ek.augmentations, bound))
            lines.add("$family,$v,$e,${ff.value},${ff.augmentations},${ek.augmentations},$bound")
        }
    }
    out.writeText(lines.joinToString("\n") + "\n")
    println("wrote ${out.path}")
}
