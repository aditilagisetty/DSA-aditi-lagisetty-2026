/**
 * Project Euler 81: finds the minimal path sum through [matrix] from the top-left to the
 * bottom-right cell, moving only right or down. Each grid cell becomes a graph vertex;
 * edge weight = value of the destination cell; solved with [dijkstra].
 *
 * @return the minimal path sum, or null if no path exists.
 */
fun solve81(matrix: List<List<Int>>): Int? {
    val numRows = matrix.size
    val numCols = matrix[0].size

    val graph: Graph<Pair<Int, Int>> = AdjacencyMapGraph()

    for (row in 0 until numRows) {
        for (col in 0 until numCols) {
            // if a right neighbor exists, add an edge to it
            if (col + 1 < numCols){
                graph.addEdge(Pair(row, col), Pair(row, col + 1), matrix[row][col + 1].toDouble())
            }
            // if a down neighbor exists, add an edge to it
            if (row + 1 < numRows){
                graph.addEdge(Pair(row, col), Pair(row + 1, col), matrix[row + 1][col].toDouble())
            }
        }
    }

    val path = dijkstra(graph, Pair(0, 0), Pair(numRows - 1, numCols - 1)) ?: return null
    return path.sumOf { (r, c) -> matrix[r][c] }
}

fun main() {
    val matrix = readMatrix("matrix.txt")
    val total = solve81(matrix)
    if (total == null) {
        println("no path found")
    } else {
        println("minimal path sum: $total")
    }
}
