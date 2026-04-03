package ru.tsu.mobileprojectmap.domain.algorithms.antColony

data class Ant (
    val visited : MutableList<Int>,
    var currentIndex: Int,
    var routeLength: Double = 0.0
)