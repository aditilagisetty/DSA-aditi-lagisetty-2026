sealed class Node {
    data class Cell(val row: Int, val col: Int) : Node()
    object Source : Node()
    object Sink : Node()
}

/**
 * Project Euler 82: finds the minimal path sum through [matrix], starting anywhere in the
 * leftmost column and ending anywhere in the rightmost column, moving up, down, or right.
 * Uses virtual [Node.Source]/[Node.Sink] vertices so a single [dijkstra] call covers every
 * possible start/end cell.
 *
 * @return the minimal path sum, or null if no path exists.
 */
fun solve82(matrix: List<List<Int>>): Int? {
    val numRows = matrix.size
    val numCols = matrix[0].size

    val graph: Graph<Node> = AdjacencyMapGraph()

    for (row in 0 until numRows) {
        for (col in 0 until numCols) {
            // if a right neighbor exists, add an edge to it
            if (col + 1 < numCols){
                graph.addEdge(Node.Cell(row, col), Node.Cell(row, col + 1), matrix[row][col + 1].toDouble())
            }
            // if an up neighbor exists, add an edge to it
            if (row - 1 >= 0){
                graph.addEdge(Node.Cell(row, col), Node.Cell(row - 1, col), matrix[row - 1][col].toDouble())
            }
            // if a down neighbor exists, add an edge to it
            if (row + 1 < numRows){
                graph.addEdge(Node.Cell(row, col), Node.Cell(row + 1, col), matrix[row + 1][col].toDouble())
            }
        }
    }

    for (row in 0 until numRows) {
        // let dijkstra start at any cell in column 0
        // edge weight counts that cells own value
        graph.addEdge(Node.Source, Node.Cell(row, 0), matrix[row][0].toDouble())
        // let Dijkstra finish at any cell in the last column
        // weight 0 since that cell's value was already counted by the edge that led into it
        graph.addEdge(Node.Cell(row, numCols - 1), Node.Sink, 0.0)
    }

    val path = dijkstra(graph, Node.Source, Node.Sink) ?: return null
    return path.filterIsInstance<Node.Cell>().sumOf { cell -> matrix[cell.row][cell.col] }
}

fun main() {
    val matrix = readMatrix("matrix.txt")
    val total = solve82(matrix)
    if (total == null) {
        println("no path found")
    } else {
        println("shortest path sum: $total")
    }
}
