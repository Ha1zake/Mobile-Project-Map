package ru.tsu.mobileprojectmap.domain.algorithms.genetic

import ru.tsu.mobileprojectmap.domain.model.FoodCategory
import ru.tsu.mobileprojectmap.domain.model.MenuItem
import ru.tsu.mobileprojectmap.domain.model.Place

class FitnessCalculator {
    fun calculate(
        individual: Individual,
        places: List<Place>,
        menuItems: List<MenuItem>,
        distances: List<List<Double>>,
        startIndex: Int,
        request: MealRequest
        ) : Double {
        val genes = individual.genes

        if (genes.isEmpty()) return Double.NEGATIVE_INFINITY
        if (genes.toSet().size != genes.size) return Double.NEGATIVE_INFINITY

        val routeDistance =  getRouteDistance(genes, distances, startIndex)

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

        val totalPrice = totalPrice(
            selectedMenuItems,
            request
        )

        val missingCategoriesCount =
            request.requiredCategories.size - coveredCategories.size


        return coveredCategories.size * 1000.0 -
                missingCategoriesCount * 1500.0 -
                routeDistance * 10.0 -
                totalPrice -
                closedPlacesCount * 500.0
    }

    private fun totalPrice(
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

    private fun getCoveredCategories(
        selectedMenuItems: List<MenuItem>,
        request: MealRequest
    ) : Set<FoodCategory> {
        val availableCategories = selectedMenuItems.map { it.category }.toSet()
        return request.requiredCategories.intersect(availableCategories)
    }


    private fun getSelectedMenuItems(
        selectedPlaces: List<Place>,
        menuItems: List<MenuItem>
    ) : List<MenuItem> {
        val placeIds = selectedPlaces.map { it.id }.toSet()
        return menuItems.filter { it.placeId in placeIds }
    }

    private fun getClosedPlacesCount(
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

    private fun getSelectedPlaces(
        genes: List<Int>,
        places: List<Place>
    ) : List<Place> {
        return genes.map { places[it] }
    }

    private fun getRouteDistance(
        genes: List<Int>,
        distances: List<List<Double>>,
        startIndex: Int,
    ) : Double {
        var dist = 0.0

        dist += distances[startIndex][genes[0]]

        for (i in 0 until genes.size - 1) {
            dist += distances[genes[i]][genes[i+1]]
        }

        return dist
    }
}