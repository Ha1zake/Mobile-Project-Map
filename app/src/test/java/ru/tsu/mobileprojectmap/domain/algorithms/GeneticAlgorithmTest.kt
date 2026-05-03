package ru.tsu.mobileprojectmap.domain.algorithms

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import ru.tsu.mobileprojectmap.domain.algorithms.genetic.GeneticAlgorithm
import ru.tsu.mobileprojectmap.domain.algorithms.genetic.MealRequest
import ru.tsu.mobileprojectmap.domain.model.FoodCategory
import ru.tsu.mobileprojectmap.domain.model.MenuItem
import ru.tsu.mobileprojectmap.domain.model.Place
import ru.tsu.mobileprojectmap.domain.model.PlaceType
import ru.tsu.mobileprojectmap.domain.model.Point

class GeneticAlgorithmTest {

    @Test
    fun `genetic algorithm covers requested meal categories`() {
        val places = listOf(
            Place(
                id = "coffee_spot",
                name = "Coffee Spot",
                type = PlaceType.CAFE,
                point = Point(0, 0),
                openHour = 8,
                closeHour = 22
            ),
            Place(
                id = "pancake_house",
                name = "Pancake House",
                type = PlaceType.CAFE,
                point = Point(2, 1),
                openHour = 8,
                closeHour = 22
            ),
            Place(
                id = "yarche",
                name = "Yarche",
                type = PlaceType.CAFE,
                point = Point(3, 3),
                openHour = 8,
                closeHour = 22
            )
        )

        val menuItems = listOf(
            MenuItem("coffee", "Coffee", FoodCategory.COFFEE, "coffee_spot", 150.0),
            MenuItem("pancakes", "Pancakes", FoodCategory.PANCAKES, "pancake_house", 220.0),
            MenuItem("tableware", "Tableware", FoodCategory.DISPOSABLE_TABLEWARE, "yarche", 60.0)
        )

        val request = MealRequest(
            requiredCategories = setOf(
                FoodCategory.COFFEE,
                FoodCategory.PANCAKES,
                FoodCategory.DISPOSABLE_TABLEWARE
            ),
            maxBudget = 600.0,
            currentHour = 12
        )

        val distances = listOf(
            listOf(0.0, 2.0, 4.0),
            listOf(2.0, 0.0, 3.0),
            listOf(4.0, 3.0, 0.0)
        )

        val result = GeneticAlgorithm().solve(
            places = places,
            menuItems = menuItems,
            distances = distances,
            request = request,
            startDistances = listOf(1.0, 2.0, 3.0)
        )

        assertTrue(result.bestRoute.isNotEmpty())
        assertEquals(request.requiredCategories, result.coveredCategories)
        assertTrue(result.totalPrice <= request.maxBudget)
    }
}
