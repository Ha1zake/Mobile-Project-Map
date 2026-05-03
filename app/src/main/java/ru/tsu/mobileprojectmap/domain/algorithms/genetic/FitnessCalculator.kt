package ru.tsu.mobileprojectmap.domain.algorithms.genetic

import kotlin.math.max
import ru.tsu.mobileprojectmap.domain.model.FoodCategory
import ru.tsu.mobileprojectmap.domain.model.MenuItem
import ru.tsu.mobileprojectmap.domain.model.Place

class FitnessCalculator {
    fun calculate(
        individual: Individual,
        places: List<Place>,
        menuItems: List<MenuItem>,
        distances: List<List<Double>>,
        startDistances: List<Double>,
        request: MealRequest
        ) : Double {
        val genes = individual.genes

        if (genes.isEmpty()) return Double.NEGATIVE_INFINITY
        if (genes.toSet().size != genes.size) return Double.NEGATIVE_INFINITY
        if (startDistances.size != places.size) return Double.NEGATIVE_INFINITY

        val routeDistance =  getRouteDistance(genes, distances, startDistances)

        val selectedPlaces = getSelectedPlaces(genes, places)

        val closedPlacesCount = getClosedPlacesCount(
            selectedPlaces,
            request.currentHour
        )

        val selectedMenuItems = getSelectedMenuItems(
            selectedPlaces,
            menuItems
        )

        val coveredCategories = getCoveredCategories(
            selectedMenuItems,
            request
        )

        val totalPrice = getTotalPrice(
            selectedMenuItems,
            request
        )

        val budgetOverflow = max(0.0, totalPrice - request.maxBudget)
        val missingCategoriesCount =
            request.requiredCategories.size - coveredCategories.size
        val closingSoonBonus = selectedPlaces.sumOf { place ->
            val hoursLeft = place.closeHour - request.currentHour
            when {
                hoursLeft <= 0 -> 0.0
                hoursLeft == 1 -> 180.0
                hoursLeft == 2 -> 90.0
                else -> 0.0
            }
        }


        return coveredCategories.size * 1000.0 -
                missingCategoriesCount * 1500.0 -
                routeDistance * 10.0 -
                totalPrice -
                closedPlacesCount * 500.0 -
                budgetOverflow * 4.0 +
                closingSoonBonus
    }

    fun getTotalPrice(
        selectedMenuItems: List<MenuItem>,
        request: MealRequest
    ) : Double {
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

    fun getCoveredCategories(
        selectedMenuItems: List<MenuItem>,
        request: MealRequest
    ) : Set<FoodCategory> {
        val availableCategories = selectedMenuItems.map { it.category }.toSet()
        return request.requiredCategories.intersect(availableCategories)
    }


    fun getSelectedMenuItems(
        selectedPlaces: List<Place>,
        menuItems: List<MenuItem>
    ) : List<MenuItem> {
        val placeIds = selectedPlaces.map { it.id }.toSet()
        return menuItems.filter { it.placeId in placeIds }
    }

    fun getClosedPlacesCount(
        selectedPlaces: List<Place>,
        currentHour: Int
    ) : Int {
        var count = 0

        for (place in selectedPlaces) {
            val isOpen = currentHour >= place.openHour && currentHour < place.closeHour
            if (!isOpen) count++
        }

        return count
    }

    fun getSelectedPlaces(
        genes: List<Int>,
        places: List<Place>
    ) : List<Place> {
        return genes.map { places[it] }
    }

    private fun getRouteDistance(
        genes: List<Int>,
        distances: List<List<Double>>,
        startDistances: List<Double>,
    ) : Double {
        var dist = 0.0

        dist += startDistances[genes[0]]

        for (i in 0 until genes.size - 1) {
            dist += distances[genes[i]][genes[i+1]]
        }

        return dist
    }
}
