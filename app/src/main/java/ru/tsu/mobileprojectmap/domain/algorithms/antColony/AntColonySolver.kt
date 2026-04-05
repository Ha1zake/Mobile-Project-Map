package ru.tsu.mobileprojectmap.domain.algorithms.antColony

import android.util.Log.v
import ru.tsu.mobileprojectmap.domain.model.Landmark
import java.lang.Math.pow

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
        val visited = mutableListOf<Int>()
        visited.add(startIndex)

        var routeLength = 0.0
        var current = startIndex


        while (visited.size != distances.size) {
            var next = -1
            var bestScore = Double.MIN_VALUE

            for (i in 0 until distances.size) {
                if (i in visited) continue
                val score = pow(pheromones[current][i],alpha) * pow((1 / distances[current][i]), beta)

                if (score > bestScore) {
                    bestScore = score
                    next = i
                }
            }
            routeLength += distances[current][next]
            visited.add(next)
            current = next
        }
        val ant = Ant(visited, current, routeLength)

        return ant
    }

    private fun evaporatePheromones(pheromones: MutableList<MutableList<Double>>) {
        val size = pheromones.size
        for (i in 0 until size) {
            for (j in 0 until size) {
                pheromones[i][j] = pheromones[i][j] * (1 - evaporation)
            }
        }
    }

    private fun depositPheromones(
        pheromones: MutableList<MutableList<Double>>,
        ants: List<Ant>
    ) {
        for (ant in ants) {
            val delta = q/ant.routeLength

            for (v in 0 until ant.visited.size - 1) {
                val from = ant.visited[v]
                val to = ant.visited[v+1]
                pheromones[from][to] += delta
                pheromones[to][from] += delta
            }
        }
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
        return AntColonyResult(bestRoute, bestLength)
    }
}