package ru.tsu.mobileprojectmap.domain.usecase

import ru.tsu.mobileprojectmap.domain.algorithms.antColony.AntColonySolver
import ru.tsu.mobileprojectmap.domain.model.Landmark

class FindLandmarksRouteUseCase (
    private val solver: AntColonySolver
) {
    fun execute(
        landmarks: List<Landmark>,
        distances: List<List<Double>>,
        start: Landmark
    ) : List<Landmark> {
        return solver.solve(landmarks, distances, start)
    }
}