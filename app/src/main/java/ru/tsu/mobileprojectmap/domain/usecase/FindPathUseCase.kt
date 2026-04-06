package ru.tsu.mobileprojectmap.domain.usecase

import ru.tsu.mobileprojectmap.domain.algorithms.astar.AStarPathfinder
import ru.tsu.mobileprojectmap.domain.model.GridCell
import ru.tsu.mobileprojectmap.domain.model.Point

class FindPathUseCase (
    private val pathFinder: AStarPathfinder
) {
    fun execute(
        grid : List<GridCell>,
        start : Point,
        end : Point
    ) : List<Point> {
        return pathFinder.findPath(grid, start, end)
    }
}