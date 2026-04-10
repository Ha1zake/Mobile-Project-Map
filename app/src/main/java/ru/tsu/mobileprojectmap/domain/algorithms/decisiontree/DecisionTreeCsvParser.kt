package ru.tsu.mobileprojectmap.domain.algorithms.decisiontree

object DecisionTreeCsvParser {

    fun parse(csvText: String): List<TrainingSample> {
        val lines = csvText
            .trim()
            .lines()
            .filter { it.isNotBlank() }

        require(lines.size >= 2) { "CSV должен содержать заголовок и хотя бы одну строку данных" }

        val header = lines.first().split(",").map { it.trim() }
        val dataLines = lines.drop(1)

        return dataLines.map { line ->
            val values = line.split(",").map { it.trim() }

            require(values.size == header.size) {
                "Неверный формат CSV: количество значений не совпадает с заголовком"
            }

            val featureNames = header.dropLast(1)
            val featureValues = values.dropLast(1)
            val features = featureNames.zip(featureValues).toMap()
            val label = values.last()

            TrainingSample(
                features = features,
                label = label
            )
        }
    }
}