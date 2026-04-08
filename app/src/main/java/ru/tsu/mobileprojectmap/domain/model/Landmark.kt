package ru.tsu.mobileprojectmap.domain.model

data class Landmark(
    val id: String,
    val name: String,
    val point: Point,
    val description: String = ""
)