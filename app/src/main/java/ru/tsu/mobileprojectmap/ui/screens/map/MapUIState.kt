package ru.tsu.mobileprojectmap.ui.screens.map
import ru.tsu.mobileprojectmap.ui.screens.map.model.MapCell
import ru.tsu.mobileprojectmap.ui.screens.map.model.MapEditMode

data class MapUIState(
    val rows: Int = 10,
    val cols: Int = 10,
    val cells: List<List<MapCell>> = emptyList(),
    val currentMode: MapEditMode = MapEditMode.SET_OBSTACLE,
    val startCell: MapCell? = null,
    val finishCell: MapCell? = null
)
