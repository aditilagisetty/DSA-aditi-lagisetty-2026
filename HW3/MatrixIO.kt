import java.io.File

fun readMatrix(path: String): List<List<Int>> =
    File(path).readLines()
        .filter { it.isNotBlank() }
        .map { line -> line.split(",").map { it.trim().toInt() } }
