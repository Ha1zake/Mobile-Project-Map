package ru.tsu.mobileprojectmap.domain.algorithms.astar

import ru.tsu.mobileprojectmap.domain.model.GridCell
import ru.tsu.mobileprojectmap.domain.model.Point
import java.util.PriorityQueue

class AStarPathfinder {

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
            h = 0.0
        )

        openSet.add(startNode)

        while (openSet.isNotEmpty()) {
            val current = openSet.poll()

            if (current.point == end) {
                return reconstructPath(current)
            }

            closedSet.add(current.point)

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

            for (dir in directions) {
                val neighbor = Point(
                    current.point.x + dir.x,
                    current.point.y + dir.y
                )
                if (closedSet.contains(neighbor)) continue

                val cell = grid.find{ it.point == neighbor }

                if (cell == null) continue

                if (!cell.isWalkable) continue

                val neighborNode = nodeMap[neighbor] ?: Node(point = neighbor)

                val cost = if (dir.x != 0 && dir.y != 0) 1.4 else 1.0

                val newG = current.g + cost

                if (newG < neighborNode.g) {
                    neighborNode.g = newG
                    neighborNode.parent = current
                    neighborNode.h = 0.0
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