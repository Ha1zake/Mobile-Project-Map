package ru.tsu.mobileprojectmap.domain.usecase

import ru.tsu.mobileprojectmap.domain.algorithms.decisiontree.DecisionTreeAlgorithm
import ru.tsu.mobileprojectmap.domain.algorithms.decisiontree.DecisionTreeCsvParser
import ru.tsu.mobileprojectmap.domain.algorithms.decisiontree.DecisionTreeResult
import ru.tsu.mobileprojectmap.domain.algorithms.decisiontree.DecisionTreeValidator

class BuildDecisionTreeUseCase {
    operator fun invoke(csvText: String): DecisionTreeResult {
        DecisionTreeValidator.validateCsv(csvText)
        val samples = DecisionTreeCsvParser.parse(csvText)
        DecisionTreeValidator.validateSamples(samples)
        return DecisionTreeAlgorithm.biuld(samples)
    }
}