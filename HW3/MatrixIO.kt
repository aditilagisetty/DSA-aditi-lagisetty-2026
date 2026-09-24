import java.io.File

/**
 * Reads a comma-separated integer matrix from the file at [path], one row per line.
 *
 * @return the matrix as a list of rows, each row a list of column values.
 */
fun readMatrix(path: String): List<List<Int>> =
    File(path).readLines()
        .filter { it.isNotBlank() }
        .map { line -> line.split(",").map { it.trim().toInt() } }
