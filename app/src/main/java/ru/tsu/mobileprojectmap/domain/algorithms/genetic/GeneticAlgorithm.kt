package ru.tsu.mobileprojectmap.domain.algorithms.genetic

import ru.tsu.mobileprojectmap.domain.model.Place

class GeneticAlgorithm {
    val populationSize = 30
    val generations = 50
    val mutationRate = 0.1
    val crossoverRate = 0.8
    val tournamentSize = 3

    fun solve(
        places: List<Place>,
        distances: List<List<Double>>,
        request: MealRequest,
        startIndex: Int
    ) : GeneticResult {

    }

}