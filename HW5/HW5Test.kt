/**
 * HW5Test.kt
 *
 * Tests for the Matrix class (all three multiplies) and both sequence alignments
 *
 * Compile: kotlinc Matrix.kt SequenceAlignment.kt Genome.kt HW5Test.kt -include-runtime -d test.jar
 * Run:     java -cp test.jar HW5TestKt
 */

import kotlin.random.Random

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

// small whole numbers so every multiply gives the exact same doubles and we can compare with ==
fun randomMatrix(n: Int, random: Random = Random): Matrix {
    val m = Matrix(n)
    for (r in 0 until n) for (c in 0 until n) m[r, c] = random.nextInt(-10, 11).toDouble()
    return m
}

// the textbook definition, written as plainly as possible so it's easy to trust
fun naiveMultiply(a: Matrix, b: Matrix): Matrix {
    val n = a.size
    val c = Matrix(n)
    for (i in 0 until n) for (j in 0 until n) {
        var sum = 0.0
        for (k in 0 until n) sum += a[i, k] * b[k, j]
        c[i, j] = sum
    }
    return c
}

fun matrixTests() {
    // get / set / of
    val m = Matrix(3)
    m[1, 2] = 5.0
    check("set then get", m[1, 2] == 5.0 && m[2, 1] == 0.0)
    val small = Matrix.of(listOf(1.0, 2.0), listOf(3.0, 4.0))
    check("of builds rows in order", small[0, 0] == 1.0 && small[0, 1] == 2.0 && small[1, 0] == 3.0 && small[1, 1] == 4.0)

    // split / combine
    val four = Matrix(4)
    for (r in 0 until 4) for (c in 0 until 4) four[r, c] = (r * 4 + c).toDouble()
    val (a11, a12, a21, a22) = four.split()
    check("split top left", a11 == Matrix.of(listOf(0.0, 1.0), listOf(4.0, 5.0)))
    check("split top right", a12 == Matrix.of(listOf(2.0, 3.0), listOf(6.0, 7.0)))
    check("split bottom left", a21 == Matrix.of(listOf(8.0, 9.0), listOf(12.0, 13.0)))
    check("split bottom right", a22 == Matrix.of(listOf(10.0, 11.0), listOf(14.0, 15.0)))
    check("combine undoes split", Matrix.combine(a11, a12, a21, a22) == four)
    check("split doesn't share memory", run { a11[0, 0] = 99.0; four[0, 0] == 0.0 })

    // + and -
    check("plus", small + small == Matrix.of(listOf(2.0, 4.0), listOf(6.0, 8.0)))
    check("minus", small - small == Matrix(2))

    // a known product
    val b = Matrix.of(listOf(5.0, 6.0), listOf(7.0, 8.0))
    val expected = Matrix.of(listOf(19.0, 22.0), listOf(43.0, 50.0))
    check("multiply 2x2", small.multiply(b) == expected)
    check("strassen 2x2", small.strassenMultiply(b) == expected)
    check("hybrid 2x2", small.hybridMultiply(b) == expected)
    check("times operator", small * b == expected)

    // mismatched sizes give null
    check("multiply mismatched -> null", Matrix(2).multiply(Matrix(3)) == null)
    check("strassen mismatched -> null", Matrix(4).strassenMultiply(Matrix(2)) == null)
    check("hybrid mismatched -> null", Matrix(5).hybridMultiply(Matrix(3)) == null)

    // edge sizes
    check("0x0 multiply", Matrix(0).multiply(Matrix(0)) == Matrix(0))
    check("0x0 strassen", Matrix(0).strassenMultiply(Matrix(0)) == Matrix(0))
    val one = Matrix.of(listOf(3.0))
    check("1x1 strassen", one.strassenMultiply(Matrix.of(listOf(-4.0))) == Matrix.of(listOf(-12.0)))

    // identity and zero
    val r8 = randomMatrix(8)
    check("A * I = A (strassen)", r8.strassenMultiply(Matrix.identity(8)) == r8)
    check("I * A = A (strassen)", Matrix.identity(8).strassenMultiply(r8) == r8)
    check("A * 0 = 0 (strassen)", r8.strassenMultiply(Matrix(8)) == Matrix(8))

    // every size from 1 to 40 (powers of two and not), compared against the plain definition
    val random = Random(5)
    var allMultiply = true
    var allStrassen = true
    var allHybrid = true
    for (n in 1..40) {
        val x = randomMatrix(n, random)
        val y = randomMatrix(n, random)
        val truth = naiveMultiply(x, y)
        if (x.multiply(y) != truth) allMultiply = false
        if (x.strassenMultiply(y) != truth) allStrassen = false
        for (cutoff in listOf(1, 2, 4, 8, 16)) if (x.hybridMultiply(y, cutoff) != truth) allHybrid = false
    }
    check("multiply matches definition for n = 1..40", allMultiply)
    check("strassen matches definition for n = 1..40 (incl. non powers of 2)", allStrassen)
    check("hybrid matches definition for n = 1..40, many cutoffs", allHybrid)

    // a bigger one with real valued entries, so we need a tolerance
    val big1 = Matrix(128).also { for (r in 0 until 128) for (c in 0 until 128) it[r, c] = random.nextDouble() }
    val big2 = Matrix(128).also { for (r in 0 until 128) for (c in 0 until 128) it[r, c] = random.nextDouble() }
    check("128x128 doubles: strassen ≈ multiply", big1.strassenMultiply(big2)!!.approxEquals(big1.multiply(big2)!!))
    check("128x128 doubles: hybrid ≈ multiply", big1.hybridMultiply(big2)!!.approxEquals(big1.multiply(big2)!!))

    // the inputs shouldn't be changed by multiplying
    val before = Matrix.combine(a11, a12, a21, a22)
    a11.strassenMultiply(a22)
    check("multiplying doesn't modify inputs", Matrix.combine(a11, a12, a21, a22) == before)
}

// tries every possible alignment of two short strings, to double check the DP gets the best one
fun bruteForceGlobal(a: String, b: String, s: Scoring): Int {
    if (a.isEmpty()) return b.length * s.gap
    if (b.isEmpty()) return a.length * s.gap
    return maxOf(
        s.pair(a[0], b[0]) + bruteForceGlobal(a.substring(1), b.substring(1), s),
        s.gap + bruteForceGlobal(a.substring(1), b, s),
        s.gap + bruteForceGlobal(a, b.substring(1), s),
    )
}

// best local score = best global-with-free-ends score over every pair of substrings
fun bruteForceLocal(a: String, b: String, s: Scoring): Int {
    var best = 0
    for (i in 0..a.length) for (i2 in i..a.length) for (j in 0..b.length) for (j2 in j..b.length) {
        best = maxOf(best, bruteForceGlobal(a.substring(i, i2), b.substring(j, j2), s))
    }
    return best
}

fun alignmentTests() {
    val nwScoring = Scoring()
    val swScoring = Scoring(3, -3, -2)

    // Needleman-Wunsch
    val same = needlemanWunsch("ACGT", "ACGT")
    check("NW identical", same.score == 4 && same.top == "ACGT" && same.bottom == "ACGT")
    val empty = needlemanWunsch("", "ACG")
    check("NW empty vs ACG is all gaps", empty.score == -3 && empty.top == "---" && empty.bottom == "ACG")
    check("NW both empty", needlemanWunsch("", "").score == 0)
    // the example on the Needleman-Wunsch Wikipedia page
    val wiki = needlemanWunsch("GCATGCG", "GATTACA")
    check("NW Wikipedia example scores 0", wiki.score == 0) { "got ${wiki.score}" }
    check("NW one gap", needlemanWunsch("ACGT", "AGT").let { it.score == 2 && it.top == "ACGT" && it.bottom == "A-GT" })

    // Smith-Waterman
    // the example on the Smith-Waterman Wikipedia page
    val sw = smithWaterman("TGTTACGG", "GGTTGACTA")
    check("SW Wikipedia example score 13", sw.score == 13) { "got ${sw.score}" }
    check("SW Wikipedia example alignment", sw.top == "GTT-AC" && sw.bottom == "GTTGAC") { "got ${sw.top} / ${sw.bottom}" }
    check("SW nothing in common scores 0", smithWaterman("AAAA", "TTTT").let { it.score == 0 && it.top.isEmpty() })
    val inside = smithWaterman("GATTACA", "CCCCGATTACACCCC")
    check("SW finds an exact substring", inside.score == 21 && inside.top == "GATTACA" && inside.bStart == 4 && inside.bEnd == 11)

    // against brute force on lots of short random strings
    val random = Random(7)
    fun randomDna(len: Int) = String(CharArray(len) { "ACGT"[random.nextInt(4)] })
    var nwOk = true
    var swOk = true
    var consistent = true
    repeat(150) {
        val a = randomDna(random.nextInt(0, 7))
        val b = randomDna(random.nextInt(0, 7))
        val nw = needlemanWunsch(a, b, nwScoring)
        val loc = smithWaterman(a, b, swScoring)
        if (nw.score != bruteForceGlobal(a, b, nwScoring)) nwOk = false
        if (loc.score != bruteForceLocal(a, b, swScoring)) swOk = false
        // the backtrace has to give a real alignment that actually earns the score
        if (nw.top.replace("-", "") != a || nw.bottom.replace("-", "") != b) consistent = false
        if (scoreOf(nw.top, nw.bottom, nwScoring) != nw.score) consistent = false
        if (loc.top.replace("-", "") != a.substring(loc.aStart, loc.aEnd)) consistent = false
        if (loc.bottom.replace("-", "") != b.substring(loc.bStart, loc.bEnd)) consistent = false
        if (scoreOf(loc.top, loc.bottom, swScoring) != loc.score) consistent = false
    }
    check("NW score matches brute force (150 random pairs)", nwOk)
    check("SW score matches brute force (150 random pairs)", swOk)
    check("backtraces are valid alignments that earn their score", consistent)

    // the real genomes: just check the alignments are internally consistent
    val g = needlemanWunsch(genomeSnippet, testAgainst)
    check("genome NW backtrace valid",
        g.top.replace("-", "") == genomeSnippet && g.bottom.replace("-", "") == testAgainst && scoreOf(g.top, g.bottom, nwScoring) == g.score)
    val l = smithWaterman(testAgainst, targetGenome)
    check("genome SW backtrace valid",
        l.top.replace("-", "") == testAgainst.substring(l.aStart, l.aEnd) &&
            l.bottom.replace("-", "") == targetGenome.substring(l.bStart, l.bEnd) &&
            scoreOf(l.top, l.bottom, swScoring) == l.score)
}

fun main() {
    matrixTests()
    alignmentTests()
    println()
    println("$passed passed, $failed failed")
}
