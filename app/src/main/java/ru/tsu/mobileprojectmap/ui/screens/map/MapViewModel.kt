package ru.tsu.mobileprojectmap.ui.screens.map

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import ru.tsu.mobileprojectmap.R
import ru.tsu.mobileprojectmap.domain.algorithms.astar.AStarPathfinder
import ru.tsu.mobileprojectmap.domain.algorithms.antColony.AntColonySolver
import ru.tsu.mobileprojectmap.domain.algorithms.genetic.GeneticAlgorithm
import ru.tsu.mobileprojectmap.domain.algorithms.genetic.MealRequest
import ru.tsu.mobileprojectmap.domain.model.GridCell
import ru.tsu.mobileprojectmap.domain.model.Landmark
import ru.tsu.mobileprojectmap.domain.model.MenuItem
import ru.tsu.mobileprojectmap.domain.model.Point
import ru.tsu.mobileprojectmap.domain.model.SamplePlaces
import ru.tsu.mobileprojectmap.domain.model.FoodCategory
import ru.tsu.mobileprojectmap.domain.model.Place
import ru.tsu.mobileprojectmap.domain.model.PlaceType
import ru.tsu.mobileprojectmap.domain.usecase.FindPathUseCase
import ru.tsu.mobileprojectmap.ui.screens.map.model.CellType
import ru.tsu.mobileprojectmap.ui.screens.map.model.MapCell
import ru.tsu.mobileprojectmap.ui.screens.map.model.MapEditMode
import java.time.LocalTime
import kotlin.math.hypot

class MapViewModel(application: Application) : AndroidViewModel(application) {

    var uiState by mutableStateOf(MapUIState())
        private set

    private val findPathUseCase = FindPathUseCase(AStarPathfinder())
    private val geneticAlgorithm = GeneticAlgorithm()
    private val antColonySolver = AntColonySolver()
    private var pathJob: Job? = null
    private var baseGrid: List<List<MapCell>> = emptyList()

    init {
        loadGrid()
    }

    private fun loadGrid() {
        viewModelScope.launch(Dispatchers.Default) {
            uiState = uiState.copy(
                isMapLoading = true,
                statusMessage = "Подготавливаем карту..."
            )
            val grid = createGridFast(uiState.rows, uiState.cols)
            baseGrid = grid
            uiState = uiState.copy(
                cells = cloneGrid(baseGrid),
                isMapLoading = false,
                statusMessage = "Карта готова. Выберите режим в меню."
            )
        }
    }

    fun setMode(mode: MapEditMode) {
        uiState = uiState.copy(
            currentMode = mode,
            statusMessage = when (mode) {
                MapEditMode.VIEW -> "Режим: просмотр карты"
                MapEditMode.SET_START -> "Режим: установка старта"
                MapEditMode.SET_FINISH -> "Режим: установка финиша"
                MapEditMode.SET_OBSTACLE -> "Режим: рисование препятствий"
            }
        )
    }

    fun resetMap() {
        pathJob?.cancel()
        uiState = uiState.copy(
            cells = cloneGrid(baseGrid),
            startCell = null,
            finishCell = null,
            path = emptyList(),
            visitedCells = emptyList(),
            currentCell = null,
            isRunning = false,
            statusMessage = "Карта сброшена",
            pathFound = null,
            geneticSummary = null,
            antSummary = null
        )
    }

    fun onCellClick(row: Int, col: Int) {
        if (uiState.isMapLoading) return
        when (uiState.currentMode) {
            MapEditMode.VIEW -> Unit
            MapEditMode.SET_START -> setStartCell(row, col)
            MapEditMode.SET_FINISH -> setFinishCell(row, col)
            MapEditMode.SET_OBSTACLE -> toggleObstacleCell(row, col)
        }
    }

    fun drawObstacleCell(row: Int, col: Int) {
        if (uiState.isMapLoading) return
        if (uiState.currentMode != MapEditMode.SET_OBSTACLE) return

        val selectedCell = uiState.cells[row][col]
        if (selectedCell.type == CellType.START || selectedCell.type == CellType.FINISH) {
            return
        }

        if (selectedCell.type == CellType.OBSTACLE || selectedCell.baseType != CellType.EMPTY) {
            return
        }

        val updatedCells = uiState.cells.map { rowList ->
            rowList.map { cell ->
                if (cell.row == row && cell.col == col) {
                    cell.copy(type = CellType.OBSTACLE, baseType = CellType.EMPTY)
                } else {
                    cell
                }
            }
        }

        uiState = uiState.copy(
            cells = updatedCells,
            path = emptyList(),
            visitedCells = emptyList(),
            currentCell = null,
            isRunning = false,
            statusMessage = "Препятствия обновлены",
            pathFound = null,
            geneticSummary = null,
            antSummary = null
        )
    }

    private fun setStartCell(row: Int, col: Int) {
        val targetCell = findNearestWalkableCell(row, col) ?: return
        var updatedCells = clearPreviousStart(uiState.cells)

        updatedCells = updatedCells.map { rowList ->
            rowList.map { cell ->
                if (cell.row == targetCell.row && cell.col == targetCell.col) {
                    cell.copy(type = CellType.START)
                } else {
                    cell
                }
            }
        }

        uiState = uiState.copy(
            cells = updatedCells,
            startCell = updatedCells[targetCell.row][targetCell.col],
            path = emptyList(),
            visitedCells = emptyList(),
            currentCell = null,
            isRunning = false,
            statusMessage = "Старт установлен",
            pathFound = null,
            geneticSummary = null,
            antSummary = null
        )
    }

    private fun setFinishCell(row: Int, col: Int) {
        val targetCell = findNearestWalkableCell(row, col) ?: return
        var updatedCells = clearPreviousFinish(uiState.cells)

        updatedCells = updatedCells.map { rowList ->
            rowList.map { cell ->
                if (cell.row == targetCell.row && cell.col == targetCell.col) {
                    cell.copy(type = CellType.FINISH)
                } else {
                    cell
                }
            }
        }

        uiState = uiState.copy(
            cells = updatedCells,
            finishCell = updatedCells[targetCell.row][targetCell.col],
            path = emptyList(),
            visitedCells = emptyList(),
            currentCell = null,
            isRunning = false,
            statusMessage = "Финиш установлен",
            pathFound = null,
            geneticSummary = null,
            antSummary = null
        )
    }

    private fun toggleObstacleCell(row: Int, col: Int) {
        val selectedCell = uiState.cells[row][col]
        if (selectedCell.type == CellType.START || selectedCell.type == CellType.FINISH) {
            return
        }

        val updatedCells = uiState.cells.map { rowList ->
            rowList.map { cell ->
                if (cell.row == row && cell.col == col) {
                    when {
                        cell.type == CellType.OBSTACLE && cell.baseType == CellType.EMPTY -> {
                            cell.copy(type = CellType.EMPTY, baseType = CellType.EMPTY)
                        }

                        cell.type == CellType.EMPTY && cell.baseType == CellType.EMPTY -> {
                            cell.copy(type = CellType.OBSTACLE, baseType = CellType.EMPTY)
                        }

                        else -> cell
                    }
                } else {
                    cell
                }
            }
        }

        uiState = uiState.copy(
            cells = updatedCells,
            path = emptyList(),
            visitedCells = emptyList(),
            currentCell = null,
            isRunning = false,
            statusMessage = "Препятствия обновлены",
            pathFound = null,
            geneticSummary = null,
            antSummary = null
        )
    }

    fun findPath() {
        if (uiState.isMapLoading) return
        val start = uiState.startCell
        val finish = uiState.finishCell

        if (start == null || finish == null) {
            uiState = uiState.copy(
                path = emptyList(),
                visitedCells = emptyList(),
                currentCell = null,
                isRunning = false,
                statusMessage = "Сначала укажите старт и финиш",
                pathFound = null,
                geneticSummary = null,
                antSummary = null
            )
            return
        }

        val grid = uiState.cells.flatten().map { cell ->
            GridCell(
                point = Point(x = cell.col, y = cell.row),
                isWalkable = cell.type != CellType.OBSTACLE
            )
        }

        pathJob?.cancel()

        pathJob = viewModelScope.launch(Dispatchers.Default) {
            uiState = uiState.copy(
                path = emptyList(),
                visitedCells = emptyList(),
                currentCell = null,
                isRunning = true,
                statusMessage = "A* ищет маршрут...",
                pathFound = null,
                geneticSummary = null,
                antSummary = null
            )

            findPathUseCase.executeWithSteps(
                grid = grid,
                start = Point(x = start.col, y = start.row),
                end = Point(x = finish.col, y = finish.row)
            ).collectLatest { step ->
                uiState = uiState.copy(
                    visitedCells = step.closedSet,
                    currentCell = step.current,
                    path = if (step.isFinished) step.path else emptyList(),
                    isRunning = !step.isFinished,
                    statusMessage = if (step.isFinished) {
                        if (step.path.isNotEmpty()) {
                            "Маршрут найден. Длина: ${step.path.size} клеток"
                        } else {
                            "Маршрут не найден"
                        }
                    } else {
                        "A* анализирует клетку (${step.current.x}, ${step.current.y})"
                    },
                    pathFound = if (step.isFinished) step.path.isNotEmpty() else null
                )
            }
        }
    }

    fun runGeneticMealRoute() {

        viewModelScope.launch(Dispatchers.Default) {
            uiState = uiState.copy(
                path = emptyList(),
                geneticSummary = null,
                statusMessage = "Генетический алгоритм запущен..."
            )

            val cafes = SamplePlaces.places.filter { it.type == PlaceType.CAFE }
            println("CAFES SIZE = ${cafes.size}")
            if (cafes.size < 2) {
                uiState = uiState.copy(statusMessage = "Недостаточно точек для генетического алгоритма")
                return@launch
            }
            val menuItems = buildMenuItems()
            val distances = buildDistanceMatrix(cafes)
            val startDistances = distances.first()
            val request = MealRequest(
                requiredCategories = setOf(
                    FoodCategory.COFFEE,
                    FoodCategory.PANCAKES,
                    FoodCategory.DISPOSABLE_TABLEWARE
                ),
                maxBudget = 900.0,
                currentHour = LocalTime.now().hour
            )
            val result = geneticAlgorithm.solve(cafes, menuItems, distances, request, startDistances)
            val routeNames = result.bestRoute.mapNotNull { index -> cafes.getOrNull(index)?.name }
            val routePoints = result.bestRoute.mapNotNull { index ->
                cafes.getOrNull(index)?.point
            }

            uiState = uiState.copy(
                path = routePoints,
                geneticSummary = if (routeNames.isNotEmpty()) {
                    "Маршрут: ${routeNames.joinToString(" -> ")} | Бюджет: %.0f | Покрытие: %d/%d".format(
                        result.totalPrice,
                        result.coveredCategories.size,
                        request.requiredCategories.size
                    )
                } else {
                    "Подходящий маршрут не найден"
                },
                statusMessage = "Генетический алгоритм завершен"
            )
        }
    }

    fun runAntLandmarksRoute() {
        viewModelScope.launch(Dispatchers.Default) {
            uiState = uiState.copy(
                path = emptyList(),
                antSummary = null,
                statusMessage = "Муравьиный алгоритм запущен..."
            )

            val landmarks = SamplePlaces.places
                .filter { it.type == PlaceType.LANDMARK }
                .map { Landmark(it.id, it.name, it.point, it.description) }
            if (landmarks.isEmpty()) {
                uiState = uiState.copy(statusMessage = "Нет достопримечательностей для обхода")
                return@launch
            }
            val distances = buildDistanceMatrixForLandmarks(landmarks)
            val route = antColonySolver.solve(landmarks, distances, landmarks.first())
            val length = calculateLandmarksLength(route)
            val routePoints = route.map { it.point }

            uiState = uiState.copy(
                path = routePoints,
                antSummary = if (route.isNotEmpty()) {
                    "Маршрут: ${route.joinToString(" -> ") { it.name }} | Длина: %.1f".format(length)
                } else {
                    "Маршрут не найден"
                },
                statusMessage = "Муравьиный алгоритм завершен"
            )
        }
    }

    private fun createGridFast(rows: Int, cols: Int): List<List<MapCell>> {
        val options = BitmapFactory.Options().apply { inPreferredConfig = Bitmap.Config.ARGB_8888 }
        val bitmap = BitmapFactory.decodeResource(
            getApplication<Application>().resources,
            R.drawable.campus_map2,
            options
        )
        val width = bitmap.width
        val height = bitmap.height
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
        bitmap.recycle()

        fun pixelAt(x: Int, y: Int): Int = pixels[y * width + x]

        return List(rows) { row ->
            List(cols) { col ->
                val sampleX = ((col + 0.5f) * width / cols).toInt().coerceIn(0, width - 1)
                val sampleY = ((row + 0.5f) * height / rows).toInt().coerceIn(0, height - 1)
                val pixel = pixelAt(sampleX, sampleY)
                val r = Color.red(pixel)
                val g = Color.green(pixel)
                val b = Color.blue(pixel)
                val isPurple = r > 120 && b > 120 && g < 140 && kotlin.math.abs(r - b) < 80
                val type = if (isPurple) CellType.EMPTY else CellType.OBSTACLE
                MapCell(row = row, col = col, type = type, baseType = type)
            }
        }
    }

    private fun findNearestWalkableCell(row: Int, col: Int): MapCell? {
        val rows = uiState.cells.size
        val cols = uiState.cells.firstOrNull()?.size ?: return null

        if (row !in 0 until rows || col !in 0 until cols) return null
        if (uiState.cells[row][col].type != CellType.OBSTACLE) {
            return uiState.cells[row][col]
        }

        val visited = mutableSetOf<Pair<Int, Int>>()
        val queue = ArrayDeque<Pair<Int, Int>>()
        queue.add(row to col)
        visited.add(row to col)

        val directions = listOf(
            -1 to 0,
            1 to 0,
            0 to -1,
            0 to 1,
            -1 to -1,
            -1 to 1,
            1 to -1,
            1 to 1
        )

        while (queue.isNotEmpty()) {
            val (currentRow, currentCol) = queue.removeFirst()

            for ((dr, dc) in directions) {
                val newRow = currentRow + dr
                val newCol = currentCol + dc

                if (newRow in 0 until rows && newCol in 0 until cols) {
                    val key = newRow to newCol
                    if (key !in visited) {
                        visited.add(key)

                        val cell = uiState.cells[newRow][newCol]
                        if (cell.type != CellType.OBSTACLE) {
                            return cell
                        }

                        queue.add(key)
                    }
                }
            }
        }

        return null
    }

    private fun clearPreviousStart(cells: List<List<MapCell>>): List<List<MapCell>> {
        val oldStart = uiState.startCell ?: return cells

        return cells.map { rowList ->
            rowList.map { cell ->
                if (cell.row == oldStart.row && cell.col == oldStart.col) {
                    cell.copy(type = cell.baseType)
                } else {
                    cell
                }
            }
        }
    }

    private fun clearPreviousFinish(cells: List<List<MapCell>>): List<List<MapCell>> {
        val oldFinish = uiState.finishCell ?: return cells

        return cells.map { rowList ->
            rowList.map { cell ->
                if (cell.row == oldFinish.row && cell.col == oldFinish.col) {
                    cell.copy(type = cell.baseType)
                } else {
                    cell
                }
            }
        }
    }

    private fun cloneGrid(source: List<List<MapCell>>): List<List<MapCell>> {
        return source.map { row -> row.map { it.copy() } }
    }

    private fun buildMenuItems(): List<MenuItem> {
        return listOf(
            MenuItem("coffee_starbooks", "Кофе латте", FoodCategory.COFFEE, "starbooks", 220.0),
            MenuItem("pancake_siberian", "Блин с мясом", FoodCategory.PANCAKES, "siberian_pancakes", 180.0),
            MenuItem("meal_cafe", "Комплексный обед", FoodCategory.FULL_MEAL, "main_cafeteria", 350.0),
            MenuItem("snack_yarche", "Перекус", FoodCategory.SNACK, "yarche", 130.0),
            MenuItem("tableware_yarche", "Одноразовая посуда", FoodCategory.DISPOSABLE_TABLEWARE, "yarche", 60.0)
        )
    }

    private fun buildDistanceMatrix(places: List<Place>): List<List<Double>> {
        return places.map { from ->
            places.map { to ->
                val dx = (from.point.x - to.point.x).toDouble()
                val dy = (from.point.y - to.point.y).toDouble()
                hypot(dx, dy)
            }
        }
    }

    private fun buildDistanceMatrixForLandmarks(landmarks: List<Landmark>): List<List<Double>> {
        return landmarks.map { from ->
            landmarks.map { to ->
                val dx = (from.point.x - to.point.x).toDouble()
                val dy = (from.point.y - to.point.y).toDouble()
                hypot(dx, dy)
            }
        }
    }

    private fun calculateLandmarksLength(route: List<Landmark>): Double {
        if (route.size < 2) return 0.0
        var sum = 0.0
        for (i in 0 until route.size - 1) {
            val dx = (route[i].point.x - route[i + 1].point.x).toDouble()
            val dy = (route[i].point.y - route[i + 1].point.y).toDouble()
            sum += hypot(dx, dy)
        }
        return sum
    }
}
