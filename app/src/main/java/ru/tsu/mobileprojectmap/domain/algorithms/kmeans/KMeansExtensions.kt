package ru.tsu.mobileprojectmap.domain.algorithms.kmeans

fun KMeansResult.getClusterForPoint(pointId: String): Cluster? {
    val clusterId = pointToCluster[pointId] ?: return null
    return clusters.find { it.id == clusterId }
}