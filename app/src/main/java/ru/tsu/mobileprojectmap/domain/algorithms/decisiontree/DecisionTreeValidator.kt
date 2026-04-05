package ru.tsu.mobileprojectmap.domain.algorithms.decisiontree

object DecisionTreeValidator {
    fun validateSamples(samples: List<TrainingSample>) {
        require(samples.isNotEmpty()) { "Обучающая выборка не должна быть пустой" }

        val expectedFeatureKeys = samples.first().features.keys
        require(expectedFeatureKeys.isNotEmpty()) { "В выборке должен быть хотя бы один признак" }

        samples.forEachIndexed { index, sample ->
            require(sample.label.isNotBlank()) {
                "У записи #${index + 1} пустая целевая метка"
            }
            require(sample.features.isNotEmpty()) {
                "У записи #${index + 1} отсутствуют признаки"
            }
            require(sample.features.keys == expectedFeatureKeys) {
                "У записи #${index + 1} набор признаков не совпадает с заголовком CSV"
            }
            require(sample.features.values.none { it.isBlank() }) {
                "У записи #${index + 1} есть пустые значения признаков"
            }
        }
    }

    fun validateCsv(csv: String) {
        val lines = csv
            .trim()
            .lines()
            .filter { it.isNotBlank() }

        require(lines.size >= 2) { "CSV должен содержать заголовок и хотя бы одну строку данных" }

        val header = lines.first().split(',').map { it.trim() }
        require(header.size >= 2) { "CSV должен содержать хотя бы один признак и целевой столбец" }
        require(header.last().isNotBlank()) { "Последний столбец CSV должен быть целевым признаком" }
        require(header.toSet().size == header.size) { "CSV содержит дублирующиеся названия столбцов" }

        lines.drop(1).forEachIndexed { index, line ->
            val values = line.split(',').map { it.trim() }
            require(values.size == header.size) {
                "Строка #${index + 2} содержит ${values.size} значений вместо ${header.size}"
            }
        }
    }
}
