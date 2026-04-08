package ru.tsu.mobileprojectmap.ui.screens.map
import ru.tsu.mobileprojectmap.domain.model.Point
import ru.tsu.mobileprojectmap.ui.screens.map.model.MapCell
import ru.tsu.mobileprojectmap.ui.screens.map.model.MapEditMode

data class MapUIState(
    val rows: Int = 200,
    val cols: Int = 200,
    val path: List<Point> = emptyList(),
    val visitedCells: List<Point> = emptyList(),
    val currentCell: Point? = null,
    val isRunning: Boolean = false,
    val cells: List<List<MapCell>> = emptyList(),
    val currentMode: MapEditMode = MapEditMode.SET_OBSTACLE,
    val startCell: MapCell? = null,
    val finishCell: MapCell? = null
)