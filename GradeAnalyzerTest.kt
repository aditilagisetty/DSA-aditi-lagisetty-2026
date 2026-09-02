/**
 * GradeAnalyzerTest.kt
 *
 * Unit tests for GradeAnalyzer.kt, using the kotlin.test library.
 * Run with: kotlinc GradeAnalyzer.kt GradeAnalyzerTest.kt -include-runtime -d test.jar
 *           java -jar test.jar
 * (or run directly through IntelliJ's test runner if using a Gradle project)
 */

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class GradeAnalyzerTest {

    @Test
    fun `average of normal grades is correct`() {
        val student = Student("Test", listOf(80.0, 90.0, 100.0))
        assertEquals(90.0, student.average())
    }

    @Test
    fun `average of empty grades is zero`() {
        val student = Student("Empty", emptyList())
        assertEquals(0.0, student.average())
    }

    @Test
    fun `median of odd-length list is the middle value`() {
        val student = Student("Odd", listOf(70.0, 90.0, 80.0))
        assertEquals(80.0, student.median())
    }

    @Test
    fun `median of even-length list is the average of the two middle values`() {
        val student = Student("Even", listOf(70.0, 80.0, 90.0, 100.0))
        assertEquals(85.0, student.median())
    }

    @Test
    fun `median of empty grades is zero`() {
        val student = Student("Empty", emptyList())
        assertEquals(0.0, student.median())
    }

    @Test
    fun `letter grade boundaries are correct`() {
        assertEquals("A", Student("A", listOf(90.0)).letterGrade())
        assertEquals("B", Student("B", listOf(80.0)).letterGrade())
        assertEquals("C", Student("C", listOf(70.0)).letterGrade())
        assertEquals("D", Student("D", listOf(60.0)).letterGrade())
        assertEquals("F", Student("F", listOf(59.9)).letterGrade())
    }

    @Test
    fun `class average is the average of student averages`() {
        val students = listOf(
            Student("A", listOf(100.0)),
            Student("B", listOf(80.0))
        )
        assertEquals(90.0, classAverage(students))
    }

    @Test
    fun `class average of empty list is zero`() {
        assertEquals(0.0, classAverage(emptyList()))
    }

    @Test
    fun `top student returns the student with the highest average`() {
        val students = listOf(
            Student("Low", listOf(60.0)),
            Student("High", listOf(95.0)),
            Student("Mid", listOf(75.0))
        )
        assertEquals("High", topStudent(students).name)
    }

    @Test
    fun `top student throws on empty list`() {
        assertFailsWith<IllegalArgumentException> {
            topStudent(emptyList())
        }
    }
}
