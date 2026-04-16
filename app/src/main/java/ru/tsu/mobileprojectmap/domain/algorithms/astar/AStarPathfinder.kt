package ru.tsu.mobileprojectmap.domain.algorithms.astar

import java.util.PriorityQueue
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.math.abs
import kotlin.math.min
import ru.tsu.mobileprojectmap.domain.model.GridCell
import ru.tsu.mobileprojectmap.domain.model.Point

data class AStarStep(
    val current: Point,
    val openSet: List<Point>,
    val closedSet: List<Point>,
    val path: List<Point> = emptyList(),
    val isFinished: Boolean = false
)

class AStarPathfinder {
    private val directions = listOf(
        Point(1, 0),
        Point(-1, 0),
        Point(0, 1),
        Point(0, -1),
        Point(1, 1),
        Point(1, -1),
        Point(-1, 1),
        Point(-1, -1)
    )

    private fun heuristic(a: Point, b: Point): Double {
        val dx = abs(a.x - b.x)
        val dy = abs(a.y - b.y)
        return dx + dy + (1.4 - 2) * min(dx, dy)
    }

    fun findPath(
        grid: List<GridCell>,
        start: Point,
        end: Point
    ): List<Point> {
        val openSet = PriorityQueue<Node>(compareBy { it.f })
        val closedSet = mutableSetOf<Point>()

        val startNode = Node(
            point = start,
            g = 0.0,
            h = heuristic(start, end)
        )

        openSet.add(startNode)

        val cellMap = grid.associateBy { it.point }
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
                if (neighbor in closedSet) continue

                val cell = cellMap[neighbor] ?: continue

                if (dir.x != 0 && dir.y != 0) {
                    val first = cellMap[Point(current.point.x, neighbor.y)]
                    val second = cellMap[Point(neighbor.x, current.point.y)]
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

    fun findPathWithSteps(
        grid: List<GridCell>,
        start: Point,
        end: Point
    ): Flow<AStarStep> = flow {
        val openSet = PriorityQueue<Node>(compareBy { it.f })
        val closedSet = mutableSetOf<Point>()

        val startNode = Node(
            point = start,
            g = 0.0,
            h = heuristic(start, end)
        )

        openSet.add(startNode)

        val cellMap = grid.associateBy { it.point }
        val nodeMap = mutableMapOf<Point, Node>()
        nodeMap[start] = startNode
        var iteration = 0

        while (openSet.isNotEmpty()) {
            val current = openSet.poll()

            if (current.point == end) {
                val finalPath = reconstructPath(current)
                emit(
                    AStarStep(
                        current = current.point,
                        openSet = openSet.map { it.point },
                        closedSet = closedSet.toList(),
                        path = finalPath,
                        isFinished = true
                    )
                )
                return@flow
            }

            closedSet.add(current.point)
            iteration++

            // Не отправляем в UI каждый шаг: это сильно нагружает Compose на больших картах.
            if (iteration == 1 || iteration % 4 == 0) {
                emit(
                    AStarStep(
                        current = current.point,
                        openSet = openSet.map { it.point },
                        closedSet = closedSet.toList(),
                        path = emptyList(),
                        isFinished = false
                    )
                )
            }

            delay(24)

            for (dir in directions) {
                val neighbor = Point(
                    current.point.x + dir.x,
                    current.point.y + dir.y
                )

                if (neighbor in closedSet) continue

                val cell = cellMap[neighbor] ?: continue

                if (dir.x != 0 && dir.y != 0) {
                    val first = cellMap[Point(current.point.x, neighbor.y)]
                    val second = cellMap[Point(neighbor.x, current.point.y)]
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

        emit(
            AStarStep(
                current = start,
                openSet = emptyList(),
                closedSet = closedSet.toList(),
                path = emptyList(),
                isFinished = true
            )
        )
    }

    private fun reconstructPath(node: Node): List<Point> {
        val path = arrayListOf<Point>()
        var current: Node? = node

        while (current != null) {
            path.add(current.point)
            current = current.parent
        }

        return path.reversed()
    }
}
