/**
 * Matrix.kt
 *
 * A square n x n matrix of doubles, stored as one flat row-major array
 * (entry (row, col) lives at data[row * n + col]).
 *
 * Supports get/set, splitting into four n/2 x n/2 blocks, joining four blocks
 * back together, +, -, and three ways to multiply:
 *   - multiply: the normal triple loop, Θ(n³)
 *   - strassenMultiply: Strassen's algorithm all the way down to 1x1, Θ(n^log2(7)) ≈ Θ(n^2.807)
 *   - hybridMultiply: Strassen for big blocks, normal multiply once blocks are <= cutoff
 */
class Matrix(val size: Int) {
    private val data = DoubleArray(size * size)

    init {
        require(size >= 0) { "size must be non-negative" }
    }

    companion object {
        /** Builds a matrix from a list of rows, e.g. Matrix.of(listOf(1.0, 2.0), listOf(3.0, 4.0)). */
        fun of(vararg rows: List<Double>): Matrix {
            val m = Matrix(rows.size)
            for (r in rows.indices) {
                require(rows[r].size == rows.size) { "matrix must be square" }
                for (c in rows[r].indices) m[r, c] = rows[r][c]
            }
            return m
        }

        fun identity(n: Int): Matrix {
            val m = Matrix(n)
            for (i in 0 until n) m[i, i] = 1.0
            return m
        }

        /** Joins four equal sized blocks into one matrix twice as wide: [a11 a12; a21 a22]. */
        fun combine(a11: Matrix, a12: Matrix, a21: Matrix, a22: Matrix): Matrix {
            val h = a11.size
            val m = Matrix(h * 2)
            for (r in 0 until h) {
                for (c in 0 until h) {
                    m[r, c] = a11[r, c]
                    m[r, c + h] = a12[r, c]
                    m[r + h, c] = a21[r, c]
                    m[r + h, c + h] = a22[r, c]
                }
            }
            return m
        }
    }

    operator fun get(row: Int, col: Int): Double = data[row * size + col]

    operator fun set(row: Int, col: Int, value: Double) {
        data[row * size + col] = value
    }

    /**
     * Splits this matrix into four n/2 x n/2 blocks, returned in the order
     * [top left, top right, bottom left, bottom right]. Requires n to be even.
     */
    fun split(): List<Matrix> {
        require(size % 2 == 0) { "can only split a matrix with even size" }
        val h = size / 2
        return listOf(subMatrix(0, 0, h), subMatrix(0, h, h), subMatrix(h, 0, h), subMatrix(h, h, h))
    }

    // copies the h x h block whose top left corner is at (row, col)
    private fun subMatrix(row: Int, col: Int, h: Int): Matrix {
        val m = Matrix(h)
        for (r in 0 until h) {
            System.arraycopy(data, (row + r) * size + col, m.data, r * h, h)
        }
        return m
    }

    operator fun plus(other: Matrix): Matrix {
        require(size == other.size) { "sizes must match" }
        val m = Matrix(size)
        for (i in data.indices) m.data[i] = data[i] + other.data[i]
        return m
    }

    operator fun minus(other: Matrix): Matrix {
        require(size == other.size) { "sizes must match" }
        val m = Matrix(size)
        for (i in data.indices) m.data[i] = data[i] - other.data[i]
        return m
    }

    /** A * B uses the hybrid version, since that's the fastest one. Throws if sizes don't match. */
    operator fun times(other: Matrix): Matrix =
        hybridMultiply(other) ?: throw IllegalArgumentException("sizes must match")

    /**
     * Multiply [this] matrix by [other] using normal matrix multiplication.
     * The loops go i, k, j instead of i, j, k so the inner loop walks along a
     * row of both [other] and the result, which is much friendlier to the cache.
     * @return [this]*[other] if the dimensions are compatible and null otherwise
     */
    fun multiply(other: Matrix): Matrix? {
        if (size != other.size) return null
        val n = size
        val result = Matrix(n)
        val a = data
        val b = other.data
        val c = result.data
        for (i in 0 until n) {
            for (k in 0 until n) {
                val aik = a[i * n + k]
                val bRow = k * n
                val cRow = i * n
                for (j in 0 until n) {
                    c[cRow + j] += aik * b[bRow + j]
                }
            }
        }
        return result
    }

    /**
     * Multiply [this] matrix by [other].
     * Your code should use Strassen's algorithm
     * This version recurses all the way down to 1x1 matrices.
     * @return [this]*[other] if the dimensions are compatible and null otherwise
     */
    fun strassenMultiply(other: Matrix): Matrix? = hybridMultiply(other, cutoff = 1)

    /**
     * Multiply [this] matrix by [other] with Strassen's algorithm, but switch to
     * normal multiplication once the blocks are [cutoff] x [cutoff] or smaller.
     * The default cutoff is the crossover I found in Benchmark.kt.
     * @return [this]*[other] if the dimensions are compatible and null otherwise
     */
    fun hybridMultiply(other: Matrix, cutoff: Int = DEFAULT_CUTOFF): Matrix? {
        if (size != other.size) return null
        if (size == 0) return Matrix(0)
        // Strassen needs to halve evenly every level, so pad with zeros up to the
        // next power of two. The zeros don't change the top left n x n of the product.
        val padded = nextPowerOfTwo(size)
        if (padded == size) return strassen(this, other, cutoff)
        return strassen(padTo(padded), other.padTo(padded), cutoff).cropTo(size)
    }

    // copies this matrix into the top left of a bigger all zero matrix
    private fun padTo(n: Int): Matrix {
        val m = Matrix(n)
        for (r in 0 until size) System.arraycopy(data, r * size, m.data, r * n, size)
        return m
    }

    // keeps just the top left n x n corner
    private fun cropTo(n: Int): Matrix = subMatrix(0, 0, n)

    // compares values with == instead of DoubleArray.contentEquals, because
    // contentEquals compares bits and treats -0.0 and 0.0 as different
    override fun equals(other: Any?): Boolean =
        other is Matrix && size == other.size && data.indices.all { data[it] == other.data[it] }

    // adding 0.0 turns -0.0 into 0.0 so equal matrices get equal hashes
    override fun hashCode(): Int = data.fold(size) { h, v -> 31 * h + (v + 0.0).hashCode() }

    /** True if every entry is within [tolerance] of the matching entry in [other] (for floating point). */
    fun approxEquals(other: Matrix, tolerance: Double = 1e-9): Boolean {
        if (size != other.size) return false
        for (i in data.indices) {
            if (kotlin.math.abs(data[i] - other.data[i]) > tolerance) return false
        }
        return true
    }

    override fun toString(): String =
        (0 until size).joinToString("\n") { r -> (0 until size).joinToString(" ") { c -> "%.2f".format(this[r, c]) } }
}

/** Blocks this size or smaller get multiplied normally in hybridMultiply (see writeup.md). */
const val DEFAULT_CUTOFF = 128

fun nextPowerOfTwo(n: Int): Int {
    var p = 1
    while (p < n) p *= 2
    return p
}

/**
 * Strassen's algorithm (following the Wikipedia version) on two matrices
 * whose size is a power of two.
 *
 * Split A and B into blocks:   A = [A11 A12]   B = [B11 B12]
 *                                  [A21 A22]       [B21 B22]
 * then compute 7 products instead of the obvious 8:
 *   M1 = (A11 + A22)(B11 + B22)
 *   M2 = (A21 + A22) B11
 *   M3 = A11 (B12 - B22)
 *   M4 = A22 (B21 - B11)
 *   M5 = (A11 + A12) B22
 *   M6 = (A21 - A11)(B11 + B12)
 *   M7 = (A12 - A22)(B21 + B22)
 * and put the result back together:
 *   C11 = M1 + M4 - M5 + M7
 *   C12 = M3 + M5
 *   C21 = M2 + M4
 *   C22 = M1 - M2 + M3 + M6
 *
 * Runtime: T(n) = 7T(n/2) + Θ(n²), which is Θ(n^log2(7)) ≈ Θ(n^2.807) by the master theorem.
 */
private fun strassen(a: Matrix, b: Matrix, cutoff: Int): Matrix {
    val n = a.size
    if (n == 1) {
        val m = Matrix(1)
        m[0, 0] = a[0, 0] * b[0, 0]
        return m
    }
    if (n <= cutoff) return a.multiply(b)!!

    val (a11, a12, a21, a22) = a.split()
    val (b11, b12, b21, b22) = b.split()

    val m1 = strassen(a11 + a22, b11 + b22, cutoff)
    val m2 = strassen(a21 + a22, b11, cutoff)
    val m3 = strassen(a11, b12 - b22, cutoff)
    val m4 = strassen(a22, b21 - b11, cutoff)
    val m5 = strassen(a11 + a12, b22, cutoff)
    val m6 = strassen(a21 - a11, b11 + b12, cutoff)
    val m7 = strassen(a12 - a22, b21 + b22, cutoff)

    val c11 = m1 + m4 - m5 + m7
    val c12 = m3 + m5
    val c21 = m2 + m4
    val c22 = m1 - m2 + m3 + m6

    return Matrix.combine(c11, c12, c21, c22)
}
