package ru.tsu.mobileprojectmap.domain.algorithms.antColony

import ru.tsu.mobileprojectmap.domain.model.Landmark
import kotlin.math.pow
import kotlin.random.Random

class AntColonySolver {
    private val iterations = 100
    private val antsCount = 10
    private val evaporation = 0.3
    private val alpha = 1.0
    private val beta = 2.0
    private val q = 50.0

    private fun createVisibilityMatrix(
        distances: List<List<Double>>
    ): List<List<Double>> {
        return List(distances.size) { i ->
            List(distances[i].size) { j ->
                val d = distances[i][j]
                if (i == j || d <= 0.0) 0.0 else 1.0 / d
            }
        }
    }
    private fun chooseNextIndex(
        current: Int,
        visitedFlag: BooleanArray,
        distances: List<List<Double>>,
        pheromones: List<List<Double>>,
        visibility: List<List<Double>>
    ) : Int {
        val candidates = mutableListOf<Pair<Int, Double>>()
        for (ind in 0 until visibility.size) {
            if (visitedFlag[ind]) continue
            val distance = distances[current][ind]
            if (distance <= 0.0) continue
            val score = pheromones[current][ind].pow(alpha) * visibility[current][ind].pow(beta)
            candidates.add(ind to score)
        }
        if (candidates.isEmpty()) return -1
        var totalScore = 0.0

        for ((_, score) in candidates) {
            totalScore += score
        }

        if (totalScore == 0.0) {
            return candidates.first().first
        }

        val randomValue = Random.nextDouble(0.0, totalScore)
        var cumulative = 0.0

        for ((index, score) in candidates) {
            cumulative += score
            if (randomValue <= cumulative) {
                return index
            }
        }
        return candidates.last().first
    }

    private fun buildRoute(
        startIndex: Int,
        distances: List<List<Double>>,
        pheromones: List<List<Double>>,
        visibility: List<List<Double>>
    ): Ant {
        val size = distances.size
        val visited = mutableListOf<Int>()
        val visitedFlag = BooleanArray(size)
        visited.add(startIndex)
        visitedFlag[startIndex] = true

        var routeLength = 0.0
        var current = startIndex

        while (visited.size != size) {
            val next = chooseNextIndex(current, visitedFlag, distances, pheromones, visibility)
            require (next != -1) { "Next not found" }

            routeLength += distances[current][next]
            visited.add(next)
            visitedFlag[next] = true
            current = next
        }

        if (size > 1 && distances[current][startIndex] > 0.0) {
            routeLength += distances[current][startIndex]
            visited.add(startIndex)
        }

        return Ant(visited, current, routeLength)
    }

    private fun evaporatePheromones(
        pheromones: MutableList<MutableList<Double>>,
        size: Int
    ) {
        for (i in 0 until size) {
            for (j in 0 until size) {
                if (i == j) continue
                pheromones[i][j] = pheromones[i][j] * (1 - evaporation)
            }
        }
    }

    private fun depositPheromones(
        pheromones: MutableList<MutableList<Double>>,
        ants: List<Ant>
    ) {
        for (ant in ants) {
            if (ant.routeLength <= 0.0) continue
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
    ): List<Landmark> {
        if (landmarks.isEmpty()) return emptyList()

        val startIndex = landmarks.indexOf(start)
        require(startIndex != -1) {"Start landmark not found"}

        val size = landmarks.size

        val pheromones = MutableList(size) {
            MutableList(size) { 1.0 }
        }

        val visibility = createVisibilityMatrix(distances)
        var bestRoute: List<Int> = emptyList()
        var bestLength = Double.MAX_VALUE

        repeat(iterations) {
            val ants = mutableListOf<Ant>()

            repeat(antsCount) {
                val ant = buildRoute(
                    startIndex = startIndex,
                    distances = distances,
                    pheromones = pheromones,
                    visibility = visibility
                )

                ants.add(ant)

                if (ant.routeLength < bestLength) {
                    bestLength = ant.routeLength
                    bestRoute = ant.visited.toList()
                }
            }

            evaporatePheromones(pheromones, size)
            depositPheromones(pheromones, ants)
        }
        return bestRoute.map { landmarks[it] }
    }
}
