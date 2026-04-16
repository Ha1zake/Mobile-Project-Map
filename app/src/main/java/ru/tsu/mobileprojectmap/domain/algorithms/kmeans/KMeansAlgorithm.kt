package ru.tsu.mobileprojectmap.domain.algorithms.kmeans

import kotlin.math.sqrt

object KMeansAlgorithm {

    fun cluster(input: KMeansInput): KMeansResult {
        KMeansValidator.validata(input)

        var centroids = initializeCentroids(input.points, input.k)
        var clusters = emptyList<Cluster>()
        var converged = false
        var iterations = 0
        var pointToCluster = emptyMap<String, Int>()

        //проходим пока либо не сойлёмся, либо не упрёмся в лимит
        for (iteration in 0 until input.maxIterations) {
            val assignments = assignPointsToClusters(input.points, centroids)
            val newCentroids = recomputeCentroids(assignments, centroids)
            clusters = buildClusters(assignments, newCentroids)
            pointToCluster = buildPointToClusterMap(assignments)

            val shift = calculateMaxCentroidShift(centroids, newCentroids)
            centroids = newCentroids
            iterations = iteration + 1

            if (shift < input.tolerance) {
                converged = true
                break
            }
        }

        return KMeansResult(
            clusters = clusters,
            pointToCluster = pointToCluster,
            iterations = iterations,
            converged = converged
        )
    }

    private fun initializeCentroids(
        points: List<ClusterPoint>,
        k: Int
    ): List<Centroid> {
        return points.take(k).map { point ->
            Centroid(point.x, point.y)
        }
    }

    private fun assignPointsToClusters(
        points: List<ClusterPoint>,
        centroids: List<Centroid>
    ): Map<Int, List<ClusterPoint>> {
        val assignments = mutableMapOf<Int, MutableList<ClusterPoint>>()

        for (index in centroids.indices) {
            assignments[index] = mutableListOf()
        }

        for (point in points) {
            val nearestCentroidIndex = centroids.indices.minByOrNull { index ->
                distance(point, centroids[index])
            } ?: throw IllegalStateException("Центроиды отсутствуют")

            assignments[nearestCentroidIndex]?.add(point)
        }

        return assignments
    }

    private fun recomputeCentroids(
        assignments: Map<Int, List<ClusterPoint>>,
        previousCentroids: List<Centroid>
    ): List<Centroid> {
        return previousCentroids.indices.map { clusterId ->
            val points = assignments[clusterId].orEmpty()

            if (points.isEmpty()) {
                previousCentroids[clusterId]
            } else {
                val avgX = points.map { it.x }.average()
                val avgY = points.map { it.y }.average()
                Centroid(avgX, avgY)
            }
        }
    }

    private fun buildClusters(
        assignments: Map<Int, List<ClusterPoint>>,
        centroids: List<Centroid>
    ): List<Cluster> {
        return centroids.indices.map { index ->
            Cluster(
                id = index,
                centroid = centroids[index],
                points = assignments[index].orEmpty(),
                colorSeed = index,
            )
        }
    }

    private fun buildPointToClusterMap(
        assignments: Map<Int, List<ClusterPoint>>
    ): Map<String, Int> {
        return buildMap {
            assignments.forEach { (clusterId, points) ->
                points.forEach { point ->
                    put(point.id, clusterId)
                }
            }
        }
    }

    private fun distance(
        point: ClusterPoint,
        centroid: Centroid
    ): Double {
        val dx = point.x - centroid.x
        val dy = point.y - centroid.y
        return sqrt(dx * dx + dy * dy)
    }

    private fun calculateMaxCentroidShift(
        oldCentroids: List<Centroid>,
        newCentroids: List<Centroid>
    ): Double {
        return oldCentroids.indices.maxOf { index ->
            val dx = oldCentroids[index].x - newCentroids[index].x
            val dy = oldCentroids[index].y - newCentroids[index].y
            sqrt(dx * dx + dy * dy)
        }
    }
}
