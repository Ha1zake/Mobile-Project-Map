package ru.tsu.mobileprojectmap.domain.algorithms.genetic

import kotlin.random.Random

class Crossover(
    private val crossoverRate: Double,
    private val minRouteSize: Int,
    private val maxRouteSize: Int
) {
    fun crossover(
        parent1: Individual,
        parent2: Individual,
        placesCount: Int
    ): List<Int> {
        if (Random.nextDouble() > crossoverRate) {
            return parent1.genes.toList()
        }

        val combinedGenes = mutableListOf<Int>()

        for (gene in parent1.genes) {
            if (gene !in combinedGenes) {
                combinedGenes.add(gene)
            }
        }

        for (gene in parent2.genes) {
            if (gene !in combinedGenes) {
                combinedGenes.add(gene)
            }
        }

        if (combinedGenes.isEmpty()) {
            combinedGenes.add(Random.nextInt(placesCount))
        }

        val routeSize = minOf(
            maxOf(parent1.genes.size, parent2.genes.size, minRouteSize),
            minOf(maxRouteSize, placesCount)
        )

        return combinedGenes.shuffled().take(routeSize)
    }
}