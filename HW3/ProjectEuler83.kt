fun main() {
    val matrix = readMatrix("matrix.txt")
    val numRows = matrix.size
    val numCols = matrix[0].size

    val graph: Graph<Pair<Int, Int>> = AdjacencyMapGraph()

    for (row in 0 until numRows) {
        for (col in 0 until numCols) {
            // right
            if (col + 1 < numCols) {
                graph.addEdge(Pair(row, col), Pair(row, col + 1), matrix[row][col + 1].toDouble())
            }
            // left
            if (col - 1 >= 0) {
                graph.addEdge(Pair(row, col), Pair(row, col - 1), matrix[row][col - 1].toDouble())
            }
            // down
            if (row + 1 < numRows) {
                graph.addEdge(Pair(row, col), Pair(row + 1, col), matrix[row + 1][col].toDouble())
            }
            // up
            if (row - 1 >= 0) {
                graph.addEdge(Pair(row, col), Pair(row - 1, col), matrix[row - 1][col].toDouble())
            }
        }
    }

    val path = dijkstra(graph, Pair(0, 0), Pair(numRows - 1, numCols - 1))
    if (path == null) {
        println("no path found")
    } else {
        val total = path.sumOf { (r, c) -> matrix[r][c] }
        println("shortest path sum: $total")
    }
}
