package ru.tsu.mobileprojectmap.domain.algorithms.decisiontree

import kotlin.math.log2

object DecisionTreeAlgorithm {
    fun biuld(sample: List<TrainingSample>): DecisionTreeResult {
        require(sample.isNotEmpty()) {"Нету данных"}

        val availableFeatures = sample.first().features.keys.toList()
        val root = buildNode(sample, availableFeatures)

        return DecisionTreeResult(root)
    }

    private fun buildNode(
        sample: List<TrainingSample>,
        availableFeatures: List<String>
    ): DecisionTreeNode {

        if (allSamplesHaveSameLabel(sample)) {
            return DecisionTreeNode.LeafNode(getMostCommonLabel(sample))
        }

        if (availableFeatures.isEmpty()) {
            return DecisionTreeNode.LeafNode(getMostCommonLabel(sample))
        }

        val bestFeature = chooseBestFeature(sample, availableFeatures)
        val groupedSamples = groupByFeature(sample, bestFeature)
        val remainingFeatures = availableFeatures - bestFeature

        val branches = groupedSamples.mapValues { (_, subset) -> buildNode(subset, remainingFeatures) }

        return DecisionTreeNode.DecisionNode (
            featureName = bestFeature,
            branches = branches
        )
    }

    private fun allSamplesHaveSameLabel(sample: List<TrainingSample>): Boolean {
        return sample.map { it.label }.distinct().size == 1
    }

    private fun getMostCommonLabel(samples: List<TrainingSample>): String {
        return samples.groupingBy { it.label }
            .eachCount()
            .maxByOrNull { it.value }!!
            .key
    }

    private fun groupByFeature (
        sample: List<TrainingSample>,
        featureName: String
    ): Map<String, List<TrainingSample>> {
        return sample.groupBy { it.features[featureName] ?: "Неизвестный" }
    }

    private fun chooseBestFeature(
        sample: List<TrainingSample>,
        availableFeatures: List<String>
    ): String {
        return availableFeatures.maxByOrNull { feature ->
            calculateInformationsonGain(sample, feature)
        } ?: throw IllegalStateException("Не подхрдящий элемент")
    }

    private fun calculateEntropy(sample: List<TrainingSample>): Double {
        val totalCount = sample.size.toDouble()

        val labelCounts = sample.groupingBy { it.label }.eachCount()

        return labelCounts.values.sumOf() { count ->
            val probability = count / totalCount
            -probability * log2(probability)
        }
    }

    private fun calculateInformationsonGain(
        sample: List<TrainingSample>,
        featureName: String
    ): Double {
        val totalEntropy = calculateEntropy(sample)
        val totalCount = sample.size.toDouble()

        val groupedSamples = groupByFeature(sample, featureName)

        val weightEntropy = groupedSamples.values.sumOf { subset ->
            val sunsetProbably = subset.size / totalCount
            sunsetProbably * calculateEntropy(subset)
        }

        return totalEntropy - weightEntropy
    }

}