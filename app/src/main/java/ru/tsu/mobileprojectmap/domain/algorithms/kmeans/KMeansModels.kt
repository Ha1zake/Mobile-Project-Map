package ru.tsu.mobileprojectmap.domain.algorithms.kmeans

data class ClusterPoint (
    val id: String,
    val x: Double,
    val y: Double,
    val name: String? = null
)

data class Centroid (
    val x: Double,
    val y: Double
)

data class Cluster(
    val id: Int,
    val centroid: Centroid,
    val points: List<ClusterPoint>
)

data class KMeansInput(
    val points: List<ClusterPoint>,
    val k: Int,
    val maxIterations: Int = 100,
    val tolerance: Double = 0.001
)

data class KMeansResult (
    val clusters: List<Cluster>,
    val iterations: Int,
    val converged: Boolean
)