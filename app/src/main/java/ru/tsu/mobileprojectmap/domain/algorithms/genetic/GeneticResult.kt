package ru.tsu.mobileprojectmap.domain.algorithms.genetic

import ru.tsu.mobileprojectmap.domain.model.FoodCategory

data class GeneticResult(
    val bestRoute: List<Int>,
    val bestFitness: Double,
    val coveredCategories: Set<FoodCategory>,
    val totalPrice: Double,
    val isValid: Boolean
)

