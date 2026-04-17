package ru.tsu.mobileprojectmap.domain.algorithms.decisiontree

object DecisionTreePredictor {

    fun predict(
        tree: DecisionTreeResult,
        input: Map<String, String>
    ): PredictionResult {
        return predict(tree.root, input)
    }

    fun predict(
        root: DecisionTreeNode,
        input: Map<String, String>
    ): PredictionResult {
        val decisionPath = mutableListOf<String>()
        val predictedLabel = predictNode(root, input, decisionPath)
        return PredictionResult(
            predictedLabel = predictedLabel,
            decisionPath = decisionPath
        )
    }

    private fun predictNode(
        node: DecisionTreeNode,
        input: Map<String, String>,
        decisionPath: MutableList<String>
    ): String {
        return when (node) {
            is DecisionTreeNode.LeafNode -> {
                decisionPath += "Лист дерева: ${node.label}"
                node.label
            }

            is DecisionTreeNode.DecisionNode -> {
                val featureValue = input[node.featureName]?.ifBlank { MISSING_FEATURE_VALUE } ?: MISSING_FEATURE_VALUE
                decisionPath += "Проверяем признак `${node.featureName}`: `$featureValue`"

                val nextNode = node.branches[featureValue]
                if (nextNode == null) {
                    val availableBranches = node.branches.keys.sorted().joinToString()
                    val fallbackLabel = fallbackPrediction(node)
                    decisionPath += "Ветка `$featureValue` не найдена. Доступно: $availableBranches"
                    decisionPath += "Используем fallback: $fallbackLabel"
                    fallbackLabel
                } else {
                    decisionPath += "Переход по ветке `$featureValue`"
                    predictNode(nextNode, input, decisionPath)
                }
            }
        }
    }

    private fun fallbackPrediction(node: DecisionTreeNode.DecisionNode): String {
        val leafResults = collectLeafResults(node)

        return leafResults
            .groupingBy { it }
            .eachCount()
            .maxByOrNull { it.value }
            ?.key
            ?: UNKNOWN_LABEL
    }

    private fun collectLeafResults(node: DecisionTreeNode): List<String> {
        return when (node) {
            is DecisionTreeNode.LeafNode -> listOf(node.label)
            is DecisionTreeNode.DecisionNode -> node.branches.values.flatMap { child ->
                collectLeafResults(child)
            }
        }
    }

    private const val MISSING_FEATURE_VALUE = "<missing>"
    private const val UNKNOWN_LABEL = "Неизвестно"
}
