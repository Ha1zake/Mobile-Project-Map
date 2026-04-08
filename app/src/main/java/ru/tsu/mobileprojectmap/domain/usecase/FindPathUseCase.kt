package ru.tsu.mobileprojectmap.domain.usecase

import kotlinx.coroutines.flow.Flow
import ru.tsu.mobileprojectmap.domain.algorithms.astar.AStarPathfinder
import ru.tsu.mobileprojectmap.domain.algorithms.astar.AStarStep
import ru.tsu.mobileprojectmap.domain.model.GridCell
import ru.tsu.mobileprojectmap.domain.model.Point

class FindPathUseCase(
    private val pathFinder: AStarPathfinder
) {
    fun execute(
        grid: List<GridCell>,
        start: Point,
        end: Point
    ): List<Point> {
        return pathFinder.findPath(grid, start, end)
    }

    fun executeWithSteps(
        grid: List<GridCell>,
        start: Point,
        end: Point
    ): Flow<AStarStep> {
        return pathFinder.findPathWithSteps(grid, start, end)
    }
}