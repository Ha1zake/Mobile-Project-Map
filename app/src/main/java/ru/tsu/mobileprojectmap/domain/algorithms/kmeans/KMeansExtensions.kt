package ru.tsu.mobileprojectmap.domain.algorithms.kmeans

fun KMeansResult.getClusterForPoint(pointId: String): Cluster? {
    val clusterId = pointToCluster[pointId]
    if (clusterId != null) {
        return clusters.find { it.id == clusterId }
    }

    return clusters.firstOrNull { cluster ->
        cluster.points.any { point -> point.id == pointId }
    }
}
