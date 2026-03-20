package ru.tsu.mobileprojectmap.domain.algorithms.astar

import ru.tsu.mobileprojectmap.domain.model.Point

data class Node(
    val point : Point,
    var g : Double = Double.MAX_VALUE,
    var h : Double = 0.0,
    var parent : Node? = null
) {
    val f : Double
        get() = g + h
}
