package ru.tsu.mobileprojectmap.domain.model

data class MenuItem(
    val id: String,
    val name: String,
    val category: FoodCategory,
    val placeId: String,
    val price: Double
)

enum class FoodCategory {
    COFFEE,
    PANCAKES,
    FULL_MEAL,
    SNACK,
    DISPOSABLE_TABLEWARE
}