package ru.tsu.mobileprojectmap.domain.algorithms.astar

import ru.tsu.mobileprojectmap.domain.model.GridCell
import ru.tsu.mobileprojectmap.domain.model.Point
import java.util.PriorityQueue
import kotlin.math.abs
import kotlin.math.min

class AStarPathfinder {

    private fun heuristic(a: Point, b: Point) : Double {
        val dx = abs(a.x - b.x)
        val dy = abs(a.y - b.y)
        val dist = dx + dy + (1.4 - 2) * min(dx, dy)
        return dist
    }

    fun findPath(
        grid : List<GridCell>,
        start : Point,
        end : Point
    ): List<Point> {
        val openSet = PriorityQueue<Node>(compareBy{it.f})
        val closedSet = mutableSetOf<Point>()

        val startNode = Node(
            point = start,
            g = 0.0,
            h = heuristic(start, end)
        )

        openSet.add(startNode)

        val directions = listOf(
            Point(1, 0),
            Point(-1, 0),
            Point(0, 1),
            Point(0, -1),
            Point(1, 1),
            Point(1, -1),
            Point(-1, 1),
            Point(-1, -1)
        )

        val nodeMap = mutableMapOf<Point, Node>()
        nodeMap[start] = startNode

        while (openSet.isNotEmpty()) {
            val current = openSet.poll()

            if (current.point == end) {
                return reconstructPath(current)
            }

            closedSet.add(current.point)

            for (dir in directions) {
                val neighbor = Point(
                    current.point.x + dir.x,
                    current.point.y + dir.y
                )
                if (closedSet.contains(neighbor)) continue

                val cell = grid.find{ it.point == neighbor }

                if (cell == null) continue

                if (dir.x != 0  && dir.y != 0) {
                    val first =
                        grid.find { (it.point.x == current.point.x && it.point.y == neighbor.y) }
                    val second =
                        grid.find { (it.point.x == neighbor.x && it.point.y == current.point.y) }
                    if (first?.isWalkable != true || second?.isWalkable != true) continue
                }

                if (!cell.isWalkable) continue

                val neighborNode = nodeMap[neighbor] ?: Node(point = neighbor)

                val cost = if (dir.x != 0 && dir.y != 0) 1.4 else 1.0

                val newG = current.g + cost

                if (newG < neighborNode.g) {
                    neighborNode.g = newG
                    neighborNode.parent = current
                    neighborNode.h = heuristic(neighborNode.point, end)
                    nodeMap[neighbor] = neighborNode
                    openSet.add(neighborNode)
                }

            }

        }



        return emptyList()
    }

    private fun reconstructPath(node : Node) : List<Point> {
        val path = arrayListOf<Point>()
        var current : Node? = node

        while (current != null) {
            path.add(current.point)
            current = current.parent
        }
        return path.reversed()
    }

}