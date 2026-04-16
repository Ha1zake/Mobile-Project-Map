package ru.tsu.mobileprojectmap.domain.algorithms.genetic

import ru.tsu.mobileprojectmap.domain.model.MenuItem
import ru.tsu.mobileprojectmap.domain.model.Place
import kotlin.random.Random

class GeneticAlgorithm {
    private val fitnessCalculator = FitnessCalculator()
    private val populationSize = 30
    private val generations = 50
    private val mutationRate = 0.1
    private val crossoverRate = 0.8
    private val tournamentSize = 3
    private val minRouteSize = 1
    private val maxRouteSize = 4

    fun solve(
        places: List<Place>,
        distances: List<List<Double>>,
        request: MealRequest,
        startIndex: Int
    ) : GeneticResult {

    }


    private fun generateInitialPopulation(
        places: List<Place>,
        menuItems: List<MenuItem>,
        distances: List<List<Double>>,
        startIndex: Int,
        request: MealRequest
    ): List<Individual> {
        if (places.isEmpty()) return emptyList()

        val population = mutableListOf<Individual>()
        val availableIndices = places.indices.toList()
        val actualMaxRouteSize = minOf(maxRouteSize, places.size)

        repeat(populationSize) {
            val routeSize = if (minRouteSize == actualMaxRouteSize) {
                minRouteSize
            } else {
                Random.nextInt(minRouteSize, actualMaxRouteSize + 1)
            }

            val genes = availableIndices
                .shuffled()
                .take(routeSize)

            val individual = Individual(genes = genes)

            individual.fitness = fitnessCalculator.calculate(
                individual = individual,
                places = places,
                menuItems = menuItems,
                distances = distances,
                startIndex = startIndex,
                request = request
            )

            population.add(individual)
        }

        return population
    }
}