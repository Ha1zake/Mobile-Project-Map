package ru.tsu.mobileprojectmap.domain.algorithms.genetic

data class Individual (
    val genes: List<Int>,
    var fitness: Double = Double.NEGATIVE_INFINITY
)