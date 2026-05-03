package ru.tsu.mobileprojectmap.ui.screens.map

import ru.tsu.mobileprojectmap.domain.model.FoodCategory
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
    val currentMode: MapEditMode = MapEditMode.VIEW,
    val startCell: MapCell? = null,
    val finishCell: MapCell? = null,
    val statusMessage: String = "Выберите алгоритм и начните работу с картой.",
    val pathFound: Boolean? = null,
    val isMapLoading: Boolean = true,
    val selectedMealCategories: Set<FoodCategory> = setOf(
        FoodCategory.COFFEE,
        FoodCategory.PANCAKES,
        FoodCategory.DISPOSABLE_TABLEWARE
    ),
    val geneticSummary: String? = null,
    val antSummary: String? = null
)
