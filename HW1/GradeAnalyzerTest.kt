/**
 * GradeAnalyzerTest.kt
 *
 * Plain-Kotlin tests for GradeAnalyzer.kt -- no JUnit/kotlin.test dependency,
 * just a main() that runs each check and prints PASS/FAIL.
 *
 * Compile: kotlinc GradeAnalyzer.kt GradeAnalyzerTest.kt -include-runtime -d test.jar
 * Run:     java -cp test.jar GradeAnalyzerTestKt
 */

var passed = 0
var failed = 0

fun check(name: String, actual: Any?, expected: Any?) {
    if (actual == expected) {
        passed++
        println("PASS: $name")
    } else {
        failed++
        println("FAIL: $name (expected $expected, got $actual)")
    }
}

fun checkThrows(name: String, block: () -> Unit) {
    try {
        block()
        failed++
        println("FAIL: $name (expected IllegalArgumentException, but nothing was thrown)")
    } catch (e: IllegalArgumentException) {
        passed++
        println("PASS: $name")
    }
}

fun main() {
    check(
        "average of normal grades is correct",
        Student("Test", listOf(80.0, 90.0, 100.0)).average(),
        90.0
    )

    check(
        "average of empty grades is zero",
        Student("Empty", emptyList()).average(),
        0.0
    )

    check(
        "median of odd-length list is the middle value",
        Student("Odd", listOf(70.0, 90.0, 80.0)).median(),
        80.0
    )

    check(
        "median of even-length list is the average of the two middle values",
        Student("Even", listOf(70.0, 80.0, 90.0, 100.0)).median(),
        85.0
    )

    check(
        "median of empty grades is zero",
        Student("Empty", emptyList()).median(),
        0.0
    )

    check("letter grade boundary A", Student("A", listOf(90.0)).letterGrade(), "A")
    check("letter grade boundary B", Student("B", listOf(80.0)).letterGrade(), "B")
    check("letter grade boundary C", Student("C", listOf(70.0)).letterGrade(), "C")
    check("letter grade boundary D", Student("D", listOf(60.0)).letterGrade(), "D")
    check("letter grade boundary F", Student("F", listOf(59.9)).letterGrade(), "F")

    check(
        "class average is the average of student averages",
        classAverage(listOf(Student("A", listOf(100.0)), Student("B", listOf(80.0)))),
        90.0
    )

    check(
        "class average of empty list is zero",
        classAverage(emptyList()),
        0.0
    )

    check(
        "top student returns the student with the highest average",
        topStudent(
            listOf(
                Student("Low", listOf(60.0)),
                Student("High", listOf(95.0)),
                Student("Mid", listOf(75.0))
            )
        ).name,
        "High"
    )

    checkThrows("top student throws on empty list") {
        topStudent(emptyList())
    }

    println()
    println("$passed passed, $failed failed")
    if (failed > 0) {
        kotlin.system.exitProcess(1)
    }
}
