package ru.tsu.mobileprojectmap.domain.algorithms.genetic

import kotlin.random.Random

class Mutation(
    private val mutationRate: Double,
    private val minRouteSize: Int,
    private val maxRouteSize: Int
) {
    fun mutate(
        genes: List<Int>,
        placesCount: Int
    ): List<Int> {
        if (genes.isEmpty()) {
            return listOf(Random.nextInt(placesCount))
        }

        val mutableGenes = genes.toMutableList()

        if (Random.nextDouble() < mutationRate) {
            when (Random.nextInt(3)) {
                0 -> {
                    val replaceIndex = Random.nextInt(mutableGenes.size)
                    val candidates = (0 until placesCount).filter { it !in mutableGenes }
                    if (candidates.isNotEmpty()) {
                        mutableGenes[replaceIndex] = candidates.random()
                    }
                }
                1 -> {
                    if (mutableGenes.size > minRouteSize) {
                        mutableGenes.removeAt(Random.nextInt(mutableGenes.size))
                    }
                }
                2 -> {
                    if (mutableGenes.size < minOf(maxRouteSize, placesCount)) {
                        val candidates = (0 until placesCount).filter { it !in mutableGenes }
                        if (candidates.isNotEmpty()) {
                            mutableGenes.add(candidates.random())
                        }
                    }
                }
            }
        }

        return mutableGenes.distinct().ifEmpty {
            listOf(Random.nextInt(placesCount))
        }
    }
}