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
                decisionPath += "Результат: ${node.label}"
                node.label
            }

            is DecisionTreeNode.DecisionNode -> {
                val featureValue = input[node.featureName]
                decisionPath += "${node.featureName} = ${featureValue ?: MISSING_FEATURE_VALUE}"

                val nextNode = featureValue?.let { node.branches[it] }
                if (nextNode == null) {
                    val fallbackLabel = fallbackPrediction(node)
                    decisionPath += "Использован fallback: $fallbackLabel"
                    fallbackLabel
                } else {
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
