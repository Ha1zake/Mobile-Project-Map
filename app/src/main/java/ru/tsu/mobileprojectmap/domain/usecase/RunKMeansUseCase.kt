package ru.tsu.mobileprojectmap.domain.usecase

import ru.tsu.mobileprojectmap.domain.algorithms.kmeans.KMeansAlgorithm
import ru.tsu.mobileprojectmap.domain.algorithms.kmeans.KMeansInput
import ru.tsu.mobileprojectmap.domain.algorithms.kmeans.KMeansResult

class RunKMeansUseCase {

    operator fun invoke(input: KMeansInput): Result<KMeansResult> {
        return try {
            Result.success(KMeansAlgorithm.cluster(input))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}