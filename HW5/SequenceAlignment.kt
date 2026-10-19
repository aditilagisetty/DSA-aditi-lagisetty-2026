/**
 * SequenceAlignment.kt
 *
 * DNA sequence alignment with dynamic programming:
 *   - needlemanWunsch: global (aligns both sequences end to end)
 *   - smithWaterman: local (best matching piece of each)
 *
 * score[i][j] = best score aligning a[0 until i] with b[0 until j], built from the
 * up-left, up, and left cells. Θ(mn) time and space, plus an O(m + n) backtrace.
 */

/** Points per alignment column. */
data class Scoring(val match: Int = 1, val mismatch: Int = -1, val gap: Int = -1) {
    fun pair(x: Char, y: Char): Int = if (x == y) match else mismatch
}

/**
 * An alignment result. [top] and [bottom] are the sequences with '-' for gaps, the same length.
 * [aStart]..[aEnd] and [bStart]..[bEnd] (end exclusive) are the aligned parts of the originals.
 */
data class Alignment(
    val score: Int,
    val top: String,
    val bottom: String,
    val aStart: Int,
    val aEnd: Int,
    val bStart: Int,
    val bEnd: Int,
) {
    val matches: Int get() = top.indices.count { top[it] == bottom[it] && top[it] != '-' }
    val gaps: Int get() = top.count { it == '-' } + bottom.count { it == '-' }

    /** The alignment in rows of [width], with '|' marking matches. */
    fun pretty(width: Int = 60): String {
        val middle = top.indices.joinToString("") { if (top[it] == bottom[it] && top[it] != '-') "|" else " " }
        val sb = StringBuilder()
        for (start in top.indices step width) {
            val end = minOf(start + width, top.length)
            sb.append(top, start, end).append('\n')
            sb.append(middle, start, end).append('\n')
            sb.append(bottom, start, end).append("\n\n")
        }
        return sb.toString().trimEnd()
    }
}

/**
 * Needleman-Wunsch global alignment of [a] and [b].
 * Edges start at i * gap. Each cell is the max of diagonal + match/mismatch,
 * up + gap, or left + gap. The answer is the bottom right cell.
 */
fun needlemanWunsch(a: String, b: String, scoring: Scoring = Scoring()): Alignment {
    val score = Array(a.length + 1) { IntArray(b.length + 1) }
    for (i in 1..a.length) score[i][0] = i * scoring.gap
    for (j in 1..b.length) score[0][j] = j * scoring.gap

    for (i in 1..a.length) {
        for (j in 1..b.length) {
            score[i][j] = maxOf(
                score[i - 1][j - 1] + scoring.pair(a[i - 1], b[j - 1]),
                score[i - 1][j] + scoring.gap,
                score[i][j - 1] + scoring.gap,
            )
        }
    }

    val (top, bottom, start) = backtrace(a, b, score, scoring, a.length, b.length, local = false)
    return Alignment(score[a.length][b.length], top, bottom, start.first, a.length, start.second, b.length)
}

/**
 * Smith-Waterman local alignment of [a] and [b].
 * Like Needleman-Wunsch, but cells floor at 0 (start fresh), the answer is the
 * max cell anywhere, and the backtrace stops at 0. Defaults to Wikipedia's +3/-3/-2.
 */
fun smithWaterman(a: String, b: String, scoring: Scoring = Scoring(match = 3, mismatch = -3, gap = -2)): Alignment {
    val score = Array(a.length + 1) { IntArray(b.length + 1) } // first row/column stay 0
    var bestI = 0
    var bestJ = 0

    for (i in 1..a.length) {
        for (j in 1..b.length) {
            score[i][j] = maxOf(
                0,
                score[i - 1][j - 1] + scoring.pair(a[i - 1], b[j - 1]),
                score[i - 1][j] + scoring.gap,
                score[i][j - 1] + scoring.gap,
            )
            if (score[i][j] > score[bestI][bestJ]) {
                bestI = i
                bestJ = j
            }
        }
    }

    val (top, bottom, start) = backtrace(a, b, score, scoring, bestI, bestJ, local = true)
    return Alignment(score[bestI][bestJ], top, bottom, start.first, bestI, start.second, bestJ)
}

/**
 * Rebuilds the alignment from (endI, endJ) by stepping to the neighbor each score
 * came from (diagonal, then up, then left), until (0, 0), or until a 0 if [local].
 * @return the two aligned strings and the (i, j) where the alignment starts
 */
private fun backtrace(
    a: String, b: String, score: Array<IntArray>, scoring: Scoring,
    endI: Int, endJ: Int, local: Boolean,
): Triple<String, String, Pair<Int, Int>> {
    val top = StringBuilder()
    val bottom = StringBuilder()
    var i = endI
    var j = endJ

    while (i > 0 || j > 0) {
        if (local && score[i][j] == 0) break
        if (i > 0 && j > 0 && score[i][j] == score[i - 1][j - 1] + scoring.pair(a[i - 1], b[j - 1])) {
            top.append(a[i - 1]); bottom.append(b[j - 1]); i--; j--
        } else if (i > 0 && score[i][j] == score[i - 1][j] + scoring.gap) {
            top.append(a[i - 1]); bottom.append('-'); i--
        } else {
            top.append('-'); bottom.append(b[j - 1]); j--
        }
    }

    // we built it from the end, so flip it around
    return Triple(top.reverse().toString(), bottom.reverse().toString(), Pair(i, j))
}

/** Rescores an aligned pair column by column (used by tests). */
fun scoreOf(top: String, bottom: String, scoring: Scoring): Int =
    top.indices.sumOf { k ->
        when {
            top[k] == '-' || bottom[k] == '-' -> scoring.gap
            else -> scoring.pair(top[k], bottom[k])
        }
    }
