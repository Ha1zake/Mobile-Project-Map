package ru.tsu.mobileprojectmap.domain.usecase

import ru.tsu.mobileprojectmap.domain.algorithms.decisiontree.DecisionTreeNode
import ru.tsu.mobileprojectmap.domain.algorithms.decisiontree.DecisionTreePredictor
import ru.tsu.mobileprojectmap.domain.algorithms.decisiontree.PredictionResult

class PredictPlaceUseCase {
    operator fun invoke (
        root: DecisionTreeNode,
        feature: Map<String, String>,
    ): PredictionResult {
        return DecisionTreePredictor.predict(root, feature)
    }
}