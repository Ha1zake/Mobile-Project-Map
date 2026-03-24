package ru.tsu.mobileprojectmap.domain.algorithms.kmeans

object KMeansValidator {
    fun validata(input : KMeansInput) {
        require(input.points.isNotEmpty()) {
            "Точки не могут быть пустыми"
        }
    }
}