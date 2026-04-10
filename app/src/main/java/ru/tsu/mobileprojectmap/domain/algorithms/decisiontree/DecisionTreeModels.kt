package ru.tsu.mobileprojectmap.domain.algorithms.decisiontree

//обучающая структура
data class TrainingSample (
    val features: Map<String, String>,
    val label: String,
)

//Узлы дерева
sealed class DecisionTreeNode {

    data class DecisionNode(
        val featureName: String,
        val branches: Map<String, DecisionTreeNode>
    ): DecisionTreeNode()

    data class LeafNode(
        val label: String
    ): DecisionTreeNode()
}

data class DecisionTreeResult(
    val root: DecisionTreeNode
)

data class PredictionResult (
    val predictedLabel: String,
    val decisionPath: List<String>,
)