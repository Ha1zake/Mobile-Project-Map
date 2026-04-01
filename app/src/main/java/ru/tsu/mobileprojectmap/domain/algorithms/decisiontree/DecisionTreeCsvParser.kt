package ru.tsu.mobileprojectmap.domain.algorithms.decisiontree

object DecisionTreeCsvParser {
    fun parse(csvText: String): List<TrainingSample> {

        val lines = csvText.lines()
            .map{it.trim()}
            .filter { it.isNotEmpty() }

        if (lines.isEmpty()) {
            throw IllegalArgumentException("csv пуст")
        }
        val header = lines.first().split(",")
        val datalines = lines.drop(1)

        return datalines.map { lines ->
            val values = lines.split(",")

            if (values.size != header.size) {
                throw IllegalArgumentException("неверный формат CSV")
            }

            val featureName = header.dropLast(1)
            val featureValues = header.drop(1)

            val feature = featureName.zip(featureValues).toMap()
            val label = values.last()

            TrainingSample (
                features = feature,
                label = label,
            )

        }

    }
}