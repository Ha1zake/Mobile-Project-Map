package ru.tsu.mobileprojectmap.domain.algorithms

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import ru.tsu.mobileprojectmap.domain.algorithms.antColony.AntColonySolver
import ru.tsu.mobileprojectmap.domain.algorithms.astar.AStarPathfinder
import ru.tsu.mobileprojectmap.domain.algorithms.decisiontree.DecisionTreeAlgorithm
import ru.tsu.mobileprojectmap.domain.algorithms.decisiontree.DecisionTreePredictor
import ru.tsu.mobileprojectmap.domain.algorithms.decisiontree.TrainingSample
import ru.tsu.mobileprojectmap.domain.algorithms.kmeans.ClusterPoint
import ru.tsu.mobileprojectmap.domain.algorithms.kmeans.KMeansAlgorithm
import ru.tsu.mobileprojectmap.domain.algorithms.kmeans.KMeansInput
import ru.tsu.mobileprojectmap.domain.algorithms.neural.DigitRecognizer
import ru.tsu.mobileprojectmap.domain.model.GridCell
import ru.tsu.mobileprojectmap.domain.model.Landmark
import ru.tsu.mobileprojectmap.domain.model.Point

class AlgorithmCoreTest {

    @Test
    fun `astar builds path around obstacles`() {
        val blockedCells = setOf(Point(1, 1), Point(2, 1), Point(3, 1))
        val grid = buildList {
            for (y in 0..4) {
                for (x in 0..4) {
                    add(
                        GridCell(
                            point = Point(x, y),
                            isWalkable = Point(x, y) !in blockedCells
                        )
                    )
                }
            }
        }

        val path = AStarPathfinder().findPath(
            grid = grid,
            start = Point(0, 0),
            end = Point(4, 4)
        )

        assertTrue(path.isNotEmpty())
        assertEquals(Point(0, 0), path.first())
        assertEquals(Point(4, 4), path.last())
        assertFalse(path.any { it in blockedCells })
    }

    @Test
    fun `kmeans splits points into two non empty clusters`() {
        val result = KMeansAlgorithm.cluster(
            KMeansInput(
                points = listOf(
                    ClusterPoint("a", 0.0, 0.0),
                    ClusterPoint("b", 0.5, 0.2),
                    ClusterPoint("c", 10.0, 10.0),
                    ClusterPoint("d", 10.5, 9.8)
                ),
                k = 2
            )
        )

        assertEquals(2, result.clusters.size)
        assertTrue(result.clusters.all { it.points.isNotEmpty() })
    }

    @Test
    fun `decision tree predicts known class`() {
        val samples = listOf(
            TrainingSample(
                features = mapOf("budget" to "low", "food_type" to "coffee"),
                label = "Bus_Stop_Coffee"
            ),
            TrainingSample(
                features = mapOf("budget" to "high", "food_type" to "coffee"),
                label = "Starbooks"
            ),
            TrainingSample(
                features = mapOf("budget" to "low", "food_type" to "pancakes"),
                label = "Siberian_Pancakes"
            )
        )

        val tree = DecisionTreeAlgorithm.build(samples)
        val prediction = DecisionTreePredictor.predict(
            tree.root,
            mapOf("budget" to "low", "food_type" to "pancakes")
        )

        assertEquals("Siberian_Pancakes", prediction.predictedLabel)
        assertTrue(prediction.decisionPath.isNotEmpty())
    }

    @Test
    fun `digit recognizer reads template for eight`() {
        val eight = listOf(
            false, true, true, true, false,
            true, false, false, false, true,
            false, true, true, true, false,
            true, false, false, false, true,
            false, true, true, true, false
        )

        val digit = DigitRecognizer.recognizeDigit(eight, gridSize = 5)

        assertEquals(8, digit)
    }

    @Test
    fun `ant colony returns loop through all landmarks`() {
        val landmarks = listOf(
            Landmark("a", "A", Point(0, 0)),
            Landmark("b", "B", Point(5, 0)),
            Landmark("c", "C", Point(2, 4))
        )
        val distances = listOf(
            listOf(0.0, 5.0, 4.5),
            listOf(5.0, 0.0, 5.0),
            listOf(4.5, 5.0, 0.0)
        )

        val route = AntColonySolver().solve(landmarks, distances, landmarks.first())

        assertTrue(route.size >= 4)
        assertEquals(route.first().id, route.last().id)
        assertEquals(setOf("a", "b", "c"), route.dropLast(1).map { it.id }.toSet())
    }
}
