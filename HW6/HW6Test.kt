/**
 * HW6Test.kt
 *
 * Tests for both max flow algorithms and team matching
 *
 * Compile: kotlinc FlowNetwork.kt MaxFlow.kt GraphGenerator.kt BenchmarkGraphs.kt TeamMatching.kt HW6Test.kt
 *              -cp lib/jgrapht-core-1.5.2.jar -include-runtime -d test.jar
 * Run:     java -cp "test.jar;lib/jgrapht-core-1.5.2.jar;lib/jheaps-0.14.jar" HW6TestKt
 *          
 */

import org.jgrapht.alg.flow.EdmondsKarpMFImpl
import org.jgrapht.alg.flow.PushRelabelMFImpl
import org.jgrapht.graph.DefaultWeightedEdge
import org.jgrapht.graph.SimpleDirectedWeightedGraph

var passed = 0
var failed = 0

fun check(name: String, condition: Boolean, detail: () -> String = { "" }) {
    if (condition) {
        passed++
        println("PASS: $name")
    } else {
        failed++
        println("FAIL: $name ${detail()}")
    }
}

val algorithms = listOf(FordFulkerson(), EdmondsKarp())

/** Builds a network from (from, to, capacity) triples. */
fun network(vertices: Int, vararg edges: Triple<Int, Int, Long>): FlowNetwork {
    val network = FlowNetwork(vertices)
    for ((from, to, cap) in edges) network.addEdge(from, to, cap)
    return network
}

/** Every edge is within capacity and every vertex except source and sink has flow in = flow out. */
fun isValidFlow(network: FlowNetwork, source: Int, sink: Int, value: Long): Boolean {
    if (network.edges().any { it.flow !in 0..it.capacity }) return false
    for (v in 0 until network.vertexCount) {
        if (v != source && v != sink && network.netFlowOut(v) != 0L) return false
    }
    return network.netFlowOut(source) == value && network.netFlowOut(sink) == -value
}

/** The cut separates source and sink, its edges are all full, and they add up to the flow value. */
fun isMinCut(network: FlowNetwork, source: Int, sink: Int, value: Long): Boolean {
    val side = network.minCutSourceSide(source)
    val cut = network.minCutEdges(source)
    return source in side && sink !in side && cut.all { it.flow == it.capacity } && cut.sumOf { it.capacity } == value
}

// hand worked examples

// The worked example in explainer.md: s=0, a=1, b=2, t=3
fun explainerExample() = network(
    4,
    Triple(0, 1, 3L), Triple(0, 2, 2L), Triple(1, 2, 2L), Triple(1, 3, 2L), Triple(2, 3, 3L),
)

fun handExampleTests() {
    for (alg in algorithms) {
        val net = explainerExample()
        val result = alg.maxFlow(net, 0, 3)
        check("${alg.name}: explainer example = 5", result.value == 5L) { "got ${result.value}" }
        check("${alg.name}: explainer example flow is valid", isValidFlow(net, 0, 3, 5))
        check("${alg.name}: explainer example min cut", isMinCut(net, 0, 3, 5) && net.minCutSourceSide(0) == setOf(0))
    }
    // DFS: s-a-b-t (2), s-a-t (1), s-b-a-t using the reverse edge (1), s-b-t (1)
    val ffPaths = FordFulkerson().maxFlow(explainerExample(), 0, 3).augmentations
    check("DFS takes 4 paths on the explainer example", ffPaths == 4) { "got $ffPaths" }
    // BFS: s-a-t (2), s-b-t (2), s-a-b-t (1)
    val ekPaths = EdmondsKarp().maxFlow(explainerExample(), 0, 3).augmentations
    check("BFS takes 3 paths on the explainer example", ekPaths == 3) { "got $ekPaths" }

    for (alg in algorithms) {
        // CLRS 3rd ed. Figure 26.1: s=0, v1..v4 = 1..4, t=5. Max flow is 23, min cut {s, v1, v2, v4}.
        val clrs = network(
            6,
            Triple(0, 1, 16L), Triple(0, 2, 13L), Triple(2, 1, 4L), Triple(1, 3, 12L), Triple(3, 2, 9L),
            Triple(2, 4, 14L), Triple(4, 3, 7L), Triple(3, 5, 20L), Triple(4, 5, 4L),
        )
        val value = alg.maxFlow(clrs, 0, 5).value
        check("${alg.name}: CLRS figure 26.1 = 23", value == 23L) { "got $value" }
        check("${alg.name}: CLRS flow valid and min cut matches the book",
            isValidFlow(clrs, 0, 5, 23) && isMinCut(clrs, 0, 5, 23) && clrs.minCutSourceSide(0) == setOf(0, 1, 2, 4))

        // if the first path is s-a-b-t, only undoing a->b with the reverse edge gets the answer 2
        val diamond = network(4, Triple(0, 1, 1L), Triple(0, 2, 1L), Triple(1, 2, 1L), Triple(1, 3, 1L), Triple(2, 3, 1L))
        check("${alg.name}: needs the reverse edge", alg.maxFlow(diamond, 0, 3).value == 2L)

        val disconnected = network(4, Triple(0, 1, 5L), Triple(2, 3, 5L))
        val noPath = alg.maxFlow(disconnected, 0, 3)
        check("${alg.name}: no path means 0", noPath.value == 0L && noPath.augmentations == 0)

        check("${alg.name}: single edge", alg.maxFlow(network(2, Triple(0, 1, 7L)), 0, 1).value == 7L)
        check("${alg.name}: zero capacity", alg.maxFlow(network(2, Triple(0, 1, 0L)), 0, 1).value == 0L)
        check("${alg.name}: parallel edges add up", alg.maxFlow(network(2, Triple(0, 1, 3L), Triple(0, 1, 4L)), 0, 1).value == 7L)

        // two fat edges into a thin one into a fat one
        val thin = network(
            6,
            Triple(0, 1, 100L), Triple(0, 2, 100L), Triple(1, 3, 100L), Triple(2, 3, 100L), Triple(3, 4, 5L), Triple(4, 5, 100L),
        )
        check("${alg.name}: bottleneck in the middle",
            alg.maxFlow(thin, 0, 5).value == 5L && thin.minCutEdges(0).map { it.from to it.to } == listOf(3 to 4))
    }

    // maxFlow resets the flow first, so the same network can be reused
    val reused = explainerExample()
    check("running twice gives the same answer",
        FordFulkerson().maxFlow(reused, 0, 3).value == 5L && EdmondsKarp().maxFlow(reused, 0, 3).value == 5L &&
            FordFulkerson().maxFlow(reused, 0, 3).value == 5L)

    val adv = GraphGenerator.adversarial(100, 3, 1_000_000, 1)
    val ff = FordFulkerson().maxFlow(adv.network, adv.source, adv.sink)
    val ek = EdmondsKarp().maxFlow(adv.network, adv.source, adv.sink)
    check("adversarial case: same answer, DFS needs 10x+ more paths",
        ff.value == ek.value && ff.augmentations > 10 * ek.augmentations) { "FF ${ff.augmentations} vs EK ${ek.augmentations}" }
}

//  comparison against JGraphT 

fun jgraphtMaxFlows(problem: FlowProblem): List<Double> {
    val graph = SimpleDirectedWeightedGraph<Int, DefaultWeightedEdge>(DefaultWeightedEdge::class.java)
    for (v in 0 until problem.network.vertexCount) graph.addVertex(v)
    for (edge in problem.network.edges()) {
        val e = graph.addEdge(edge.from, edge.to)
        graph.setEdgeWeight(e, edge.capacity.toDouble())
    }
    return listOf(
        EdmondsKarpMFImpl(graph).getMaximumFlowValue(problem.source, problem.sink),
        PushRelabelMFImpl(graph).getMaximumFlowValue(problem.source, problem.sink),
    )
}

/** Returns null if both of our algorithms match JGraphT (and give a valid flow + min cut), otherwise what went wrong. */
fun compareWithJGraphT(problem: FlowProblem): String? {
    val expected = jgraphtMaxFlows(problem)
    if (expected[0] != expected[1]) return "JGraphT's two algorithms disagree"
    for (alg in algorithms) {
        val value = alg.maxFlow(problem.network, problem.source, problem.sink).value
        if (value != expected[0].toLong()) return "${alg.name} got $value, JGraphT got ${expected[0]}"
        if (!isValidFlow(problem.network, problem.source, problem.sink, value)) return "${alg.name} flow invalid"
        if (!isMinCut(problem.network, problem.source, problem.sink, value)) return "${alg.name} min cut wrong"
    }
    return null
}

fun jgraphtTests() {
    var graphs = 0
    val problems = mutableListOf<String>()
    for (seed in 1..60) {
        for ((vertices, edges) in listOf(6 to 10, 10 to 30, 30 to 120, 60 to 600)) {
            for (maxCap in listOf(1L, 10L, 1000L)) {
                val error = compareWithJGraphT(GraphGenerator.random(vertices, edges, maxCap, seed))
                if (error != null) problems.add("V=$vertices E=$edges cap=$maxCap seed=$seed: $error")
                graphs++
            }
        }
    }
    check("both algorithms match JGraphT on $graphs random graphs", problems.isEmpty()) { problems.take(3).toString() }

    for ((family, size) in listOf("sparse" to 400, "dense" to 100, "adversarial" to 100)) {
        val error = compareWithJGraphT(BenchmarkGraphs.make(family, size))
        check("both algorithms match JGraphT on benchmark graph $family V=$size", error == null) { error ?: "" }
    }
}

//  team matching 

/** No team over capacity, everyone placed on a team they ranked, and the flow value is the head count. */
fun isValidMatching(students: List<Student>, teams: List<Team>, result: MatchingResult): Boolean {
    val overFull = teams.any { team -> result.assignment.values.count { it == team.name } > team.size }
    val unranked = result.assignment.any { (name, team) -> team !in students.first { it.name == name }.rankedTeams }
    return !overFull && !unranked &&
        result.maxFlow == result.assignment.size.toLong() &&
        students.size == result.assignment.size + result.unplaced.size
}

fun teamMatchingTests() {
    // A and B both only want X (1 seat); C wants X or Y. Best: one of A/B on X, C on Y.
    run {
        val teams = listOf(Team("X", 1), Team("Y", 1))
        val students = listOf(Student("A", listOf("X")), Student("B", listOf("X")), Student("C", listOf("X", "Y")))
        val result = TeamMatcher(students, teams).solve()
        check("small example is valid", isValidMatching(students, teams, result))
        check("small example places 2, C on Y", result.maxFlow == 2L && result.assignment["C"] == "Y" && result.unplaced.size == 1)
        check("small example min cut is A and B fighting over X",
            result.blockedTeams == listOf("X") && result.blockedStudents.toSet() == setOf("A", "B"))
    }

    run {
        val teams = listOf(Team("X", 2), Team("Y", 2))
        val students = listOf("A", "B", "C", "D").map { Student(it, listOf("X", "Y")) }
        val result = TeamMatcher(students, teams).solve()
        check("everyone fits when there is room",
            isValidMatching(students, teams, result) && result.maxFlow == 4L && result.unplaced.isEmpty() && result.blockedStudents.isEmpty())
    }

    run {
        val (students, teams) = SyntheticClass.generate()
        val ek = TeamMatcher(students, teams).solve(EdmondsKarp())
        val ff = TeamMatcher(students, teams).solve(FordFulkerson())
        check("synthetic class is valid with both algorithms",
            isValidMatching(students, teams, ek) && isValidMatching(students, teams, ff) && ek.maxFlow == ff.maxFlow)
    }

    // on 50 different made-up classes, the min cut accounts for exactly the unplaced students
    var good = 0
    for (seed in 1..50) {
        val (students, teams) = SyntheticClass.generate(seed = seed)
        val result = TeamMatcher(students, teams).solve()
        val seats = teams.filter { it.name in result.blockedTeams }.sumOf { it.size }
        val onlyRankedBlocked = result.blockedStudents.all { name ->
            result.blockedTeams.containsAll(students.first { it.name == name }.rankedTeams)
        }
        val blockedTeamsFull = result.blockedTeams.all { name ->
            result.assignment.values.count { it == name } == teams.first { it.name == name }.size
        }
        if (isValidMatching(students, teams, result) &&
            result.blockedStudents.containsAll(result.unplaced) &&
            onlyRankedBlocked && blockedTeamsFull &&
            result.unplaced.size == result.blockedStudents.size - seats
        ) good++
    }
    check("min cut explains every unplaced student (50 classes)", good == 50) { "only $good of 50" }
}

fun main() {
    handExampleTests()
    jgraphtTests()
    teamMatchingTests()
    println()
    println("$passed passed, $failed failed")
}
