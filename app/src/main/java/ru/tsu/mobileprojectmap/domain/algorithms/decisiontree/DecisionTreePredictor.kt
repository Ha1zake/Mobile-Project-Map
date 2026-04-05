package ru.tsu.mobileprojectmap.domain.algorithms.decisiontree

object DecisionTreePredictor {

    fun predict(
        tree: DecisionTreeResult,
        input: Map<String, String>
    ): String {
        return predictNode(tree.root, input)
    }

    private fun predictNode(
        node: DecisionTreeNode,
        input: Map<String, String>
    ): String {
        return when (node) {
            is DecisionTreeNode.LeafNode -> node.label

            is DecisionTreeNode.DecisionNode -> {
                val featureValue = input[node.featureName]
                    ?: return fallbackPrediction(node)

                val nextNode = node.branches[featureValue]
                    ?: return fallbackPrediction(node)

                predictNode(nextNode, input)
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
            ?: "Неизвестно"
    }

    private fun collectLeafResults(node: DecisionTreeNode): List<String> {
        return when (node) {
            is DecisionTreeNode.LeafNode -> listOf(node.label)
            is DecisionTreeNode.DecisionNode -> node.branches.values.flatMap { child ->
                collectLeafResults(child)
            }
        }
    }
}