package ru.tsu.mobileprojectmap.domain.model

data class Place(
    val id: String,
    val name: String,
    val type: PlaceType,
    val point: Point,
    val description: String = "",
    val menuItems: List<String> = emptyList(),
    val openHour: Int = 8,
    val closeHour: Int = 20
)

enum class PlaceType {
    CAFE,
    COWORKING,
    LANDMARK
}