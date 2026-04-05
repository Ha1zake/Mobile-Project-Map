package ru.tsu.mobileprojectmap.domain.algorithms.antColony

import android.util.Log.v
import ru.tsu.mobileprojectmap.domain.model.Landmark

class AntColonySolver {
    private val iterations = 100
    private val antsCount = 20
    private val evaporation = 0.3
    private val alpha = 1.0
    private val beta = 2.0
    private val q = 100.0


    private fun buildRoute(
        startIndex: Int,
        distances: List<List<Double>>,
        pheromones: List<List<Double>>
    ): Ant {
        val visited = MutableList<Int>()
        visited.add(startIndex)

        var current = startIndex
        var length = 0.0

        while (visited.size < distances.size) {

        }
    }

    private fun evaporatePheromones(pheromones: List<List<Double>>) {
        TODO()
    }

    private fun depositPheromones(
        pheromones: List<List<Double>>,
         ants: List<Ant>
    ) {
        TODO()
    }

    fun solve (
        landmarks: List<Landmark>,
        distances: List<List<Double>>,
        start: Landmark
    ): AntColonyResult {
        if (landmarks.isEmpty()) return AntColonyResult(emptyList(), -1.0)

        val startIndex = landmarks.indexOf(start)
        require(startIndex != -1) {"Start landmark not found"}

        val size = landmarks.size

        val pheromones = MutableList(size) {
            MutableList(size) { 1.0 }
        }

        var bestRoute: List<Int> = emptyList()
        var bestLength = Double.MAX_VALUE

        repeat(iterations) {
            val ants = mutableListOf<Ant>()

            repeat(antsCount) {
                val ant = buildRoute(
                    startIndex = startIndex,
                    distances = distances,
                    pheromones = pheromones
                )

                ants.add(ant)

                if (ant.routeLength < bestLength) {
                    bestLength = ant.routeLength
                    bestRoute = ant.visited.toList()
                }
            }

            evaporatePheromones(pheromones)
            depositPheromones(pheromones, ants)
        }
        return bestRoute.map { landmarks[it] }
    }
}