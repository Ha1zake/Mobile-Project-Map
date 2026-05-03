package ru.tsu.mobileprojectmap.domain.algorithms.genetic

import ru.tsu.mobileprojectmap.domain.model.FoodCategory

data class MealRequest(
    val requiredCategories: Set<FoodCategory>,
    val maxBudget: Double,
    val currentHour: Int
)
