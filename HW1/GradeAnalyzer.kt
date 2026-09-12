/**
 * GradeAnalyzer.kt
 *
 * Kotlin port of grade_analyzer.py.
 * Analyzes a set of students' grades: computes each student's average,
 * median, and letter grade, plus overall class stats.
 */

/**
 * Represents a student and their list of grades.
 * A Kotlin data class automatically gives us equals(), hashCode(),
 * toString(), and copy() for free -- no need to write them by hand.
 */
data class Student(val name: String, val grades: List<Double> = emptyList()) {

    /** Returns the average of this student's grades, or 0.0 if empty. */
    fun average(): Double {
        if (grades.isEmpty()) return 0.0
        return grades.sum() / grades.size
    }

    /** Returns the median of this student's grades, or 0.0 if empty. */
    fun median(): Double {
        if (grades.isEmpty()) return 0.0
        val sorted = grades.sorted()
        val n = sorted.size
        val mid = n / 2
        return if (n % 2 == 0) {
            (sorted[mid - 1] + sorted[mid]) / 2
        } else {
            sorted[mid]
        }
    }

    /** Converts this student's average into a letter grade. */
    fun letterGrade(): String {
        val avg = average()
        return when {
            avg >= 90 -> "A"
            avg >= 80 -> "B"
            avg >= 70 -> "C"
            avg >= 60 -> "D"
            else -> "F"
        }
    }
}

/** Returns the average of all students' averages. */
fun classAverage(students: List<Student>): Double {
    if (students.isEmpty()) return 0.0
    return students.sumOf { it.average() } / students.size
}

/** Returns the student with the highest average. Throws if list is empty. */
fun topStudent(students: List<Student>): Student {
    if (students.isEmpty()) throw IllegalArgumentException("Cannot find top student of an empty list")
    return students.maxBy { it.average() }
}

fun main() {
    val students = listOf(
        Student("Aditi", listOf(95.0, 88.0, 92.0)),
        Student("Sam", listOf(70.0, 75.0, 68.0)),
        Student("Jordan", listOf(82.0, 85.0, 79.0, 91.0))
    )

    for (s in students) {
        println("${s.name}: avg=%.2f, median=%.2f, letter=${s.letterGrade()}"
            .format(s.average(), s.median()))
    }

    println()
    println("Class average: %.2f".format(classAverage(students)))
    println("Top student: ${topStudent(students).name}")
}
