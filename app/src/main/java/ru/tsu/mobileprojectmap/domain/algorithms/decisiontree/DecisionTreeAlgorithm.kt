package ru.tsu.mobileprojectmap.domain.algorithms.decisiontree

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
        TODO();
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
        return availableFeatures.first()
    }

}