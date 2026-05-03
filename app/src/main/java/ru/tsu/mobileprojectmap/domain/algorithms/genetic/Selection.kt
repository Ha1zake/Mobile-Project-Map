package ru.tsu.mobileprojectmap.domain.algorithms.genetic

import ru.tsu.mobileprojectmap.domain.model.MenuItem
import ru.tsu.mobileprojectmap.domain.model.Place

class Selection(
    private val tournamentSize: Int
) {
    fun selectParent(
        population: List<Individual>
    ): Individual {
        val candidates = List(minOf(tournamentSize, population.size)) {
            population.random()
        }

        return candidates.maxByOrNull { it.fitness } ?: population.first()
    }

    fun selectBestIndividual(
        population: List<Individual>,
        places: List<Place>,
        menuItems: List<MenuItem>,
        request: MealRequest,
        validator: (Individual, List<Place>, List<MenuItem>, MealRequest) -> Boolean
    ): Individual {
        return population.maxWithOrNull { left, right ->
            compareIndividuals(left, right, places, menuItems, request, validator)
        } ?: population.first()
    }

    fun isBetter(
        candidate: Individual,
        currentBest: Individual,
        places: List<Place>,
        menuItems: List<MenuItem>,
        request: MealRequest,
        validator: (Individual, List<Place>, List<MenuItem>, MealRequest) -> Boolean
    ): Boolean {
        return compareIndividuals(
            left = candidate,
            right = currentBest,
            places = places,
            menuItems = menuItems,
            request = request,
            validator = validator
        ) > 0
    }

    private fun compareIndividuals(
        left: Individual,
        right: Individual,
        places: List<Place>,
        menuItems: List<MenuItem>,
        request: MealRequest,
        validator: (Individual, List<Place>, List<MenuItem>, MealRequest) -> Boolean
    ): Int {
        val leftValid = validator(left, places, menuItems, request)
        val rightValid = validator(right, places, menuItems, request)

        if (leftValid && !rightValid) return 1
        if (!leftValid && rightValid) return -1

        return left.fitness.compareTo(right.fitness)
    }
}