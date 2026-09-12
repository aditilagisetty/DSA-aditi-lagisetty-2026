data class Meeting(val name: String, val start: Int, val end: Int) {
    /** True if this meeting overlaps with [other] (touching endpoints do not count). */
    fun overlaps(other: Meeting): Boolean {
        return this.start < other.end && other.start < this.end
    }
}

/** helper so test data can be written as 10:00 instead of minutes. */
fun time(hhmm: String): Int {
    val (h, m) = hhmm.split(":").map { it.toInt() }
    return h * 60 + m
}

/**
 * Checks every pair of meetings against each other.
 *
 * Runtime: for n meetings there are n*(n-1)/2 pairs to check, so this runs in
 * Θ(n^2) time. As the number of meetings grows, the number of comparisons
 * grows quadratically -- doubling n roughly quadruples the work.
 */
fun hasConflictBruteForce(meetings: List<Meeting>): Boolean {
    for (i in meetings.indices) {
        for (j in i + 1 until meetings.size) {
            if (meetings[i].overlaps(meetings[j])) {
                return true
            }
        }
    }
    return false
}

/**
 * Algorithm 2: sort-based.
 *
 * Sorts meetings by start time

 * Runtime: sorting takes Θ(n log n) The single pass afterward to compare adjacent meetings
 * takes Θ(n). Since n log n dominates n for large n, the overall runtime is
 * Θ(n log n)ows.
 */
fun hasConflictSorted(meetings: List<Meeting>): Boolean {
    val sorted = meetings.sortedBy { it.start }
    for (i in 0 until sorted.size - 1) {
        if (sorted[i].overlaps(sorted[i + 1])) {
            return true
        }
    }
    return false
}

fun main() {
    val meetings = listOf(
        Meeting("A", time("10:00"), time("11:00")),
        Meeting("B", time("10:15"), time("10:45")),
        Meeting("C", time("13:30"), time("14:00"))
    )

    println("Meetings:")
    meetings.forEach { println("  ${it.name}: ${it.start}-${it.end}") }

    println("\nBrute force conflict check: ${hasConflictBruteForce(meetings)}")
    println("Sort-based conflict check:  ${hasConflictSorted(meetings)}")

    val noConflicts = listOf(
        Meeting("X", time("10:00"), time("11:00")),
        Meeting("Y", time("11:00"), time("11:30")),
        Meeting("Z", time("13:00"), time("14:00"))
    )
    println("\nNo-conflict schedule (touching endpoints):")
    println("Brute force: ${hasConflictBruteForce(noConflicts)}")
    println("Sort-based:  ${hasConflictSorted(noConflicts)}")
}