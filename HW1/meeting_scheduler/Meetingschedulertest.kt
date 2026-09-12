var passed = 0
var failed = 0

fun check(description: String, expected: Boolean, actual: Boolean) {
    if (expected == actual) {
        println("PASS: $description")
        passed++
    } else {
        println("FAIL: $description (expected $expected, got $actual)")
        failed++
    }
}

fun main() {
    // No conflicts: clearly separated meetings
    val noOverlap = listOf(
        Meeting("A", time("09:00"), time("10:00")),
        Meeting("B", time("11:00"), time("12:00")),
        Meeting("C", time("13:00"), time("14:00"))
    )
    check("no conflict (brute force) - separated meetings", false, hasConflictBruteForce(noOverlap))
    check("no conflict (sorted) - separated meetings", false, hasConflictSorted(noOverlap))

    // Clear conflict: overlapping meetings
    val overlap = listOf(
        Meeting("A", time("10:00"), time("11:00")),
        Meeting("B", time("10:30"), time("11:30"))
    )
    check("conflict (brute force) - overlapping meetings", true, hasConflictBruteForce(overlap))
    check("conflict (sorted) - overlapping meetings", true, hasConflictSorted(overlap))

    // Edge case: one meeting ends exactly when another starts (NOT a conflict)
    val touchingEdges = listOf(
        Meeting("A", time("10:00"), time("11:00")),
        Meeting("B", time("11:00"), time("11:30"))
    )
    check("touching endpoints (brute force) - not a conflict", false, hasConflictBruteForce(touchingEdges))
    check("touching endpoints (sorted) - not a conflict", false, hasConflictSorted(touchingEdges))

    // Edge case: near-miss, off by one minute (IS a conflict) ---
    val almostTouching = listOf(
        Meeting("A", time("10:00"), time("11:00")),
        Meeting("B", time("10:59"), time("11:30"))
    )
    check("near-touching by 1 min (brute force) - is a conflict", true, hasConflictBruteForce(almostTouching))
    check("near-touching by 1 min (sorted) - is a conflict", true, hasConflictSorted(almostTouching))

    // Coflict not between adjacent-in-time-order meetings 
    val nonAdjacentConflict = listOf(
        Meeting("A", time("09:00"), time("12:00")),
        Meeting("B", time("10:00"), time("10:30")),
        Meeting("C", time("13:00"), time("14:00"))
    )
    check("conflict between non-adjacent meetings (brute force)", true, hasConflictBruteForce(nonAdjacentConflict))
    check("conflict between non-adjacent meetings (sorted)", true, hasConflictSorted(nonAdjacentConflict))

    val empty = emptyList<Meeting>()
    check("empty list (brute force) - no conflict", false, hasConflictBruteForce(empty))
    check("empty list (sorted) - no conflict", false, hasConflictSorted(empty))

    val single = listOf(Meeting("A", time("09:00"), time("10:00")))
    check("single meeting (brute force) - no conflict", false, hasConflictBruteForce(single))
    check("single meeting (sorted) - no conflict", false, hasConflictSorted(single))

    val nested = listOf(
        Meeting("A", time("09:00"), time("17:00")),
        Meeting("B", time("10:00"), time("10:30"))
    )
    check("nested meeting (brute force) - is a conflict", true, hasConflictBruteForce(nested))
    check("nested meeting (sorted) - is a conflict", true, hasConflictSorted(nested))

    println("\n$passed passed, $failed failed")
}