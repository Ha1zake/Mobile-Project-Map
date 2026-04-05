package ru.tsu.mobileprojectmap.domain.algorithms.decisiontree

import kotlin.math.log2

object DecisionTreeAlgorithm {
    fun build(samples: List<TrainingSample>): DecisionTreeResult {
        require(samples.isNotEmpty()) { "Нету данных" }

        val availableFeatures = samples.first().features.keys.toList()
        val root = buildNode(samples, availableFeatures)

        return DecisionTreeResult(root)
    }

    private fun buildNode(
        samples: List<TrainingSample>,
        availableFeatures: List<String>
    ): DecisionTreeNode {
        if (allSamplesHaveSameLabel(samples)) {
            return DecisionTreeNode.LeafNode(getMostCommonLabel(samples))
        }

        if (availableFeatures.isEmpty()) {
            return DecisionTreeNode.LeafNode(getMostCommonLabel(samples))
        }

        val bestFeature = chooseBestFeature(samples, availableFeatures)
        val groupedSamples = groupByFeature(samples, bestFeature)
        val remainingFeatures = availableFeatures - bestFeature

        val branches = groupedSamples.mapValues { (_, subset) ->
            buildNode(subset, remainingFeatures)
        }

        return DecisionTreeNode.DecisionNode(
            featureName = bestFeature,
            branches = branches
        )
    }

    private fun allSamplesHaveSameLabel(samples: List<TrainingSample>): Boolean {
        return samples.map { it.label }.distinct().size == 1
    }

    private fun getMostCommonLabel(samples: List<TrainingSample>): String {
        return samples.groupingBy { it.label }
            .eachCount()
            .maxByOrNull { it.value }
            ?.key
            ?: error("Не удалось определить метку")
    }

    private fun groupByFeature(
        samples: List<TrainingSample>,
        featureName: String
    ): Map<String, List<TrainingSample>> {
        return samples.groupBy { it.features[featureName] ?: UNKNOWN_FEATURE_VALUE }
    }

    private fun chooseBestFeature(
        samples: List<TrainingSample>,
        availableFeatures: List<String>
    ): String {
        return availableFeatures.maxByOrNull { feature ->
            calculateInformationGain(samples, feature)
        } ?: throw IllegalStateException("Не подходящий элемент")
    }

    private fun calculateEntropy(samples: List<TrainingSample>): Double {
        val totalCount = samples.size.toDouble()
        val labelCounts = samples.groupingBy { it.label }.eachCount()

        return labelCounts.values.sumOf { count ->
            val probability = count / totalCount
            -probability * log2(probability)
        }
    }

    private fun calculateInformationGain(
        samples: List<TrainingSample>,
        featureName: String
    ): Double {
        val totalEntropy = calculateEntropy(samples)
        val totalCount = samples.size.toDouble()
        val groupedSamples = groupByFeature(samples, featureName)

        val weightedEntropy = groupedSamples.values.sumOf { subset ->
            val subsetProbability = subset.size / totalCount
            subsetProbability * calculateEntropy(subset)
        }

        return totalEntropy - weightedEntropy
    }

    private const val UNKNOWN_FEATURE_VALUE = "Неизвестный"
}
