package ru.tsu.mobileprojectmap.domain.algorithms.genetic

import ru.tsu.mobileprojectmap.domain.model.MenuItem
import ru.tsu.mobileprojectmap.domain.model.Place
import kotlin.random.Random

class GeneticAlgorithm {
    private val fitnessCalculator = FitnessCalculator()
    private val populationSize = 100
    private val generations = 150
    private val mutationRate = 0.1
    private val crossoverRate = 0.8
    private val tournamentSize = 3
    private val minRouteSize = 1
    private val maxRouteSize = 4
    private val selection = Selection(tournamentSize)
    private val crossover = Crossover(crossoverRate, minRouteSize, maxRouteSize)
    private val mutation = Mutation(mutationRate, minRouteSize, maxRouteSize)

    fun solve(
        places: List<Place>,
        menuItems: List<MenuItem>,
        distances: List<List<Double>>,
        request: MealRequest,
        startDistances: List<Double>
    ) : GeneticResult {
        if (places.isEmpty()) {
            return GeneticResult(
                bestRoute = emptyList(),
                bestFitness = Double.NEGATIVE_INFINITY,
                coveredCategories = emptySet(),
                totalPrice = 0.0,
                isValid = false
            )
        }

        require(startDistances.size == places.size) {
            "Start distances count must match places count"
        }
        validateDistances(places, distances)

        var population = generateInitialPopulation(
            places,
            menuItems,
            distances,
            startDistances,
            request
        )

        if (population.isEmpty()) {
            return GeneticResult(
                bestRoute = emptyList(),
                bestFitness = Double.NEGATIVE_INFINITY,
                coveredCategories = emptySet(),
                totalPrice = 0.0,
                isValid = false
            )
        }

        var bestIndividual = selection.selectBestIndividual(
            population = population,
            places = places,
            menuItems = menuItems,
            request = request,
            validator = ::isValidSolution
        )

        repeat(generations) {
            val newPopulation = mutableListOf<Individual>()
            newPopulation.add(bestIndividual.copy())

            while (newPopulation.size < populationSize) {
                val parent1 = selection.selectParent(population)
                val parent2 = selection.selectParent(population)

                val childGenes = crossover.crossover(parent1, parent2, places.size)
                val mutatedGenes = mutation.mutate(childGenes, places.size)
                val child = createIndividual(
                    genes = mutatedGenes,
                    places = places,
                    menuItems = menuItems,
                    distances = distances,
                    startDistances = startDistances,
                    request = request
                )

                newPopulation.add(child)
            }

            population = newPopulation

            val generationBest = selection.selectBestIndividual(
                population = population,
                places = places,
                menuItems = menuItems,
                request = request,
                validator = ::isValidSolution
            )
            if (
                selection.isBetter(
                    candidate = generationBest,
                    currentBest = bestIndividual,
                    places = places,
                    menuItems = menuItems,
                    request = request,
                    validator = ::isValidSolution
                )
            ) {
                bestIndividual = generationBest
            }
        }

        val selectedPlaces = bestIndividual.genes.map { places[it] }
        val selectedMenuItems = menuItems.filter { item ->
            selectedPlaces.any { place -> place.id == item.placeId }
        }
        val coveredCategories = request.requiredCategories.intersect(
            selectedMenuItems.map { it.category }.toSet()
        )
        val totalPrice = calculateTotalPrice(selectedMenuItems, request)

        return GeneticResult(
            bestRoute = bestIndividual.genes,
            bestFitness = bestIndividual.fitness,
            coveredCategories = coveredCategories,
            totalPrice = totalPrice,
            isValid = isValidSolution(bestIndividual, places, menuItems, request)
        )
    }

    fun solve(
        places: List<Place>,
        menuItems: List<MenuItem>,
        distances: List<List<Double>>,
        request: MealRequest,
        startIndex: Int
    ): GeneticResult {
        require(startIndex in places.indices) { "Start index is out of bounds" }

        return solve(
            places = places,
            menuItems = menuItems,
            distances = distances,
            request = request,
            startDistances = distances[startIndex]
        )
    }


    private fun generateInitialPopulation(
        places: List<Place>,
        menuItems: List<MenuItem>,
        distances: List<List<Double>>,
        startDistances: List<Double>,
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
                startDistances = startDistances,
                request = request
            )

            population.add(individual)
        }

        return population
    }

    private fun createIndividual(
        genes: List<Int>,
        places: List<Place>,
        menuItems: List<MenuItem>,
        distances: List<List<Double>>,
        startDistances: List<Double>,
        request: MealRequest
    ): Individual {
        val individual = Individual(genes = genes)
        individual.fitness = fitnessCalculator.calculate(
            individual = individual,
            places = places,
            menuItems = menuItems,
            distances = distances,
            startDistances = startDistances,
            request = request
        )
        return individual
    }

    private fun validateDistances(
        places: List<Place>,
        distances: List<List<Double>>
    ) {
        require(distances.size == places.size) {
            "Distances matrix size must match places count"
        }

        require(distances.all { row -> row.size == places.size }) {
            "Distances matrix must be square"
        }
    }

    private fun isValidSolution(
        individual: Individual,
        places: List<Place>,
        menuItems: List<MenuItem>,
        request: MealRequest
    ): Boolean {
        val selectedPlaces = individual.genes.map { places[it] }

        if (selectedPlaces.any { request.currentHour < it.openHour || request.currentHour >= it.closeHour }) {
            return false
        }

        val selectedPlaceIds = selectedPlaces.map { it.id }.toSet()
        val selectedMenuItems = menuItems.filter { it.placeId in selectedPlaceIds }
        val coveredCategories = request.requiredCategories.intersect(
            selectedMenuItems.map { it.category }.toSet()
        )

        if (coveredCategories.size != request.requiredCategories.size) {
            return false
        }

        if (calculateTotalPrice(selectedMenuItems, request) > request.maxBudget) {
            return false
        }

        return true
    }

    private fun calculateTotalPrice(
        selectedMenuItems: List<MenuItem>,
        request: MealRequest
    ): Double {
        var total = 0.0

        for (category in request.requiredCategories) {
            val cheapestItem = selectedMenuItems
                .filter { it.category == category }
                .minByOrNull { it.price }

            if (cheapestItem != null) {
                total += cheapestItem.price
            }
        }

        return total
    }
}
