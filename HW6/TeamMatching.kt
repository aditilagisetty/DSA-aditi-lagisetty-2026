import kotlin.random.Random

data class Student(val name: String, val rankedTeams: List<String>)   // first choice first
data class Team(val name: String, val size: Int)

/**
 * The result of matching students to teams.
 *
 * [blockedStudents] and [blockedTeams] come from the min cut: the students and teams still reachable
 * from the source after max flow. Every one of the blocked students only ranked blocked teams and
 * there are more blocked students than blocked seats so somebody in that group has to miss out.
 */
class MatchingResult(
    val assignment: Map<String, String>,   // student name -> team name
    val unplaced: List<String>,
    val blockedStudents: List<String>,
    val blockedTeams: List<String>,
    val maxFlow: Long,
)

/**
 * Turns student to team assignment into max flow:
 *
 *     source --1--> each student --1--> each team they ranked --team size--> sink
 *
 * Each student can send 1 unit (they join one team), each team can take `size` units (its seats),
 * and flow can only go to teams a student ranked. So a flow of value k is k students placed on
 * teams they ranked and the max flow places as many students as possible.
 */
class TeamMatcher(private val students: List<Student>, private val teams: List<Team>) {
    // vertex numbering: 0 = source, 1..S = students, S+1..S+T = teams, S+T+1 = sink
    private val source = 0
    private val sink = students.size + teams.size + 1
    private fun studentVertex(i: Int) = 1 + i
    private fun teamVertex(j: Int) = 1 + students.size + j
    private val teamIndex = teams.withIndex().associate { (j, team) -> team.name to j }

    val network = FlowNetwork(students.size + teams.size + 2)

    init {
        for ((i, student) in students.withIndex()) {
            network.addEdge(source, studentVertex(i), 1)
            // ranked teams go in preference order, so the searches try a student's first choice first
            for (teamName in student.rankedTeams) {
                val j = teamIndex[teamName] ?: error("${student.name} ranked unknown team $teamName")
                network.addEdge(studentVertex(i), teamVertex(j), 1)
            }
        }
        for ((j, team) in teams.withIndex()) network.addEdge(teamVertex(j), sink, team.size.toLong())
    }

    fun solve(algorithm: AugmentingPathMaxFlow = EdmondsKarp()): MatchingResult {
        val flow = algorithm.maxFlow(network, source, sink)

        // a student is on the team whose edge carries their 1 unit of flow
        val assignment = linkedMapOf<String, String>()
        for (edge in network.edges()) {
            val i = edge.from - 1
            val j = edge.to - 1 - students.size
            if (i in students.indices && j in teams.indices && edge.flow == 1L) {
                assignment[students[i].name] = teams[j].name
            }
        }
        val unplaced = students.map { it.name }.filter { it !in assignment }

        val sourceSide = network.minCutSourceSide(source)
        val blockedStudents = students.indices.filter { studentVertex(it) in sourceSide }.map { students[it].name }
        val blockedTeams = teams.indices.filter { teamVertex(it) in sourceSide }.map { teams[it].name }

        return MatchingResult(assignment, unplaced, blockedStudents, blockedTeams, flow.value)
    }
}

/** Makes up a class of students whose preferences pile onto a few popular teams. */
object SyntheticClass {
    val topics = listOf(
        "AI Tutor", "Game Engine", "Campus Map", "Music Recommender", "Budget App", "Chess Bot",
        "Weather Dashboard", "Recipe Planner", "Study Scheduler", "Compiler", "Fitness Tracker", "Library Catalog",
    )

    /**
     * [studentCount] students and one team per topic, each with [teamSize] seats.
     * Each student ranks [choices] different teams. Team k is picked with weight 1 / (k + 1)^[popularity],
     * so the first few topics are much more popular than the rest (like real classes).
     */
    fun generate(
        studentCount: Int = 60,
        teamSize: Int = 5,
        choices: Int = 3,
        popularity: Double = 1.5,
        seed: Int = 2026,
    ): Pair<List<Student>, List<Team>> {
        val random = Random(seed)
        val teams = topics.map { Team(it, teamSize) }
        val weights = teams.indices.map { 1.0 / Math.pow(it + 1.0, popularity) }
        val students = (1..studentCount).map { id ->
            val picks = mutableListOf<String>()
            while (picks.size < choices) {
                val team = teams[pickWeighted(weights, random)].name
                if (team !in picks) picks.add(team)
            }
            Student("Student %02d".format(id), picks)
        }
        return students to teams
    }

    private fun pickWeighted(weights: List<Double>, random: Random): Int {
        var r = random.nextDouble() * weights.sum()
        for ((i, w) in weights.withIndex()) {
            r -= w
            if (r < 0) return i
        }
        return weights.lastIndex
    }
}
