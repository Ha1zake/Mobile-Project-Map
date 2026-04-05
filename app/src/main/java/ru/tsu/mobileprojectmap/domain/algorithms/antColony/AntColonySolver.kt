package ru.tsu.mobileprojectmap.domain.algorithms.antColony

import ru.tsu.mobileprojectmap.domain.model.Landmark
import kotlin.math.pow
import kotlin.random.Random

class AntColonySolver {
    private val iterations = 100
    private val antsCount = 20
    private val evaporation = 0.3
    private val alpha = 1.0
    private val beta = 2.0
    private val q = 100.0


    private fun chooseNextIndex(
        current: Int,
        visited: List<Int>,
        distances: List<List<Double>>,
        pheromones: List<List<Double>>
    ) : Int {
        val candidates = mutableListOf<Pair<Int, Double>>()
        for (ind in 0 until distances.size) {
            if (ind in visited) continue
            val distance = distances[current][ind]
            if (distance <= 0.0) continue
            val score = pheromones[current][ind].pow(alpha) * (1 / distance).pow(beta)
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
        pheromones: List<List<Double>>
    ): Ant {
        val visited = mutableListOf<Int>()
        visited.add(startIndex)

        var routeLength = 0.0
        var current = startIndex


        while (visited.size != distances.size) {
            val next = chooseNextIndex(current, visited, distances, pheromones)
            require (next != -1) { "Next not found" }

            routeLength += distances[current][next]
            visited.add(next)
            current = next
        }

        return Ant(visited, current, routeLength)
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