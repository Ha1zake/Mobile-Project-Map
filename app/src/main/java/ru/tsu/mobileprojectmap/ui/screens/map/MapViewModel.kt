package ru.tsu.mobileprojectmap.ui.screens.map

import android.app.Application
import android.graphics.BitmapFactory
import android.graphics.Color
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.tsu.mobileprojectmap.R
import ru.tsu.mobileprojectmap.domain.algorithms.antColony.AntColonySolver
import ru.tsu.mobileprojectmap.domain.algorithms.astar.AStarPathfinder
import ru.tsu.mobileprojectmap.domain.algorithms.genetic.GeneticAlgorithm
import ru.tsu.mobileprojectmap.domain.algorithms.genetic.MealRequest
import ru.tsu.mobileprojectmap.domain.model.FoodCategory
import ru.tsu.mobileprojectmap.domain.model.GridCell
import ru.tsu.mobileprojectmap.domain.model.Landmark
import ru.tsu.mobileprojectmap.domain.model.MenuItem
import ru.tsu.mobileprojectmap.domain.model.Place
import ru.tsu.mobileprojectmap.domain.model.Point
import ru.tsu.mobileprojectmap.domain.model.SamplePlaces
import ru.tsu.mobileprojectmap.domain.usecase.FindPathUseCase
import ru.tsu.mobileprojectmap.ui.screens.map.model.CellType
import ru.tsu.mobileprojectmap.ui.screens.map.model.MapCell
import ru.tsu.mobileprojectmap.ui.screens.map.model.MapEditMode
import java.util.Calendar
import kotlin.math.abs
import kotlin.math.roundToInt

class MapViewModel(application: Application) : AndroidViewModel(application) {

    var uiState by mutableStateOf(MapUIState())
        private set

    private val pathFinder = AStarPathfinder()
    private val findPathUseCase = FindPathUseCase(pathFinder)
    private val geneticAlgorithm = GeneticAlgorithm()
    private val antColonySolver = AntColonySolver()
    private var pathJob: Job? = null
    private var statusResetJob: Job? = null
    private var baseGrid: List<List<MapCell>> = emptyList()

    init {
        loadGrid()
    }

    fun setMode(mode: MapEditMode) {
        cancelStatusReset()
        pathJob?.cancel()
        uiState = uiState.copy(
            currentMode = mode,
            visitedCells = emptyList(),
            currentCell = null,
            isRunning = false,
            statusMessage = defaultStatusMessage(mode)
        )
    }

    fun setMealCategorySelected(category: FoodCategory, selected: Boolean) {
        cancelStatusReset()
        val updatedCategories = uiState.selectedMealCategories.toMutableSet().apply {
            if (selected) add(category) else remove(category)
        }

        uiState = uiState.copy(
            selectedMealCategories = updatedCategories,
            geneticSummary = null,
            statusMessage = if (updatedCategories.isEmpty()) {
                "Выберите хотя бы один товар."
            } else {
                "Список товаров обновлён."
            }
        )
        scheduleStatusReset()
    }

    fun resetMap() {
        cancelStatusReset()
        pathJob?.cancel()
        uiState = uiState.copy(
            cells = cloneGrid(baseGrid),
            startCell = null,
            finishCell = null,
            path = emptyList(),
            visitedCells = emptyList(),
            currentCell = null,
            isRunning = false,
            currentMode = MapEditMode.VIEW,
            statusMessage = "Карта сброшена.",
            pathFound = null,
            geneticSummary = null,
            antSummary = null
        )
        scheduleStatusReset()
    }

    fun onCellClick(row: Int, col: Int) {
        if (uiState.isMapLoading || uiState.cells.isEmpty()) return
        cancelStatusReset()

        when (uiState.currentMode) {
            MapEditMode.VIEW -> Unit
            MapEditMode.SET_START -> setStartCell(row, col)
            MapEditMode.SET_FINISH -> setFinishCell(row, col)
            MapEditMode.SET_OBSTACLE -> toggleObstacleCell(row, col)
        }
    }

    fun drawObstacleCell(row: Int, col: Int) {
        if (uiState.isMapLoading || uiState.currentMode != MapEditMode.SET_OBSTACLE) return

        val selectedCell = uiState.cells[row][col]
        if (selectedCell.type == CellType.START || selectedCell.type == CellType.FINISH) return
        if (selectedCell.type == CellType.OBSTACLE || selectedCell.baseType != CellType.EMPTY) return

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
            pathFound = null,
            geneticSummary = null,
            antSummary = null,
            statusMessage = "Ограждения обновлены."
        )
        scheduleStatusReset()
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
                statusMessage = "Сначала выберите старт и финиш.",
                pathFound = null
            )
            scheduleStatusReset()
            return
        }

        cancelStatusReset()
        val grid = currentGrid()
        pathJob?.cancel()

        pathJob = viewModelScope.launch {
            withContext(Dispatchers.Main) {
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
            }

            findPathUseCase.executeWithSteps(
                grid = grid,
                start = Point(start.col, start.row),
                end = Point(finish.col, finish.row)
            ).collectLatest { step ->
                withContext(Dispatchers.Main) {
                    val finalPath = if (step.isFinished) step.path else emptyList()
                    uiState = uiState.copy(
                        visitedCells = if (step.isFinished) emptyList() else step.closedSet,
                        currentCell = if (step.isFinished) null else step.current,
                        path = finalPath,
                        isRunning = !step.isFinished,
                        statusMessage = if (step.isFinished) {
                            if (finalPath.isNotEmpty()) "Маршрут построен." else "Маршрут не найден."
                        } else {
                            "A* проверяет клетки..."
                        },
                        pathFound = if (step.isFinished) finalPath.isNotEmpty() else null
                    )

                    if (step.isFinished) {
                        scheduleStatusReset()
                    }
                }
            }
        }
    }

    fun runGeneticMealRoute() {
        if (uiState.selectedMealCategories.isEmpty()) {
            uiState = uiState.copy(
                path = emptyList(),
                geneticSummary = "Выберите хотя бы один товар.",
                statusMessage = "Генетический алгоритм не запущен."
            )
            scheduleStatusReset()
            return
        }

        cancelStatusReset()
        viewModelScope.launch(Dispatchers.Default) {
            withContext(Dispatchers.Main) {
                uiState = uiState.copy(
                    path = emptyList(),
                    visitedCells = emptyList(),
                    currentCell = null,
                    isRunning = true,
                    geneticSummary = null,
                    antSummary = null,
                    statusMessage = "Подбираем маршрут для сбора еды..."
                )
            }

            val cafes = SamplePlaces.cafes
            val grid = currentGrid()
            val menuItems = buildMenuItems(cafes)
            val startPoint = getRouteStartPoint()
            val distances = buildAStarDistanceMatrix(cafes.map { it.point }, grid)
            val startDistances = buildAStarDistanceVector(startPoint, cafes.map { it.point }, grid)
            val request = MealRequest(
                requiredCategories = uiState.selectedMealCategories,
                maxBudget = 900.0,
                currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
            )

            val result = geneticAlgorithm.solve(
                places = cafes,
                menuItems = menuItems,
                distances = distances,
                request = request,
                startDistances = startDistances
            )

            val routePlaces = result.bestRoute.mapNotNull { index -> cafes.getOrNull(index) }
            val routeStops = listOf(startPoint) + routePlaces.map { it.point }
            val fullRoute = buildAStarPathForStops(routeStops, grid)
            val selectedGoods = request.requiredCategories.joinToString { it.displayName() }

            withContext(Dispatchers.Main) {
                uiState = uiState.copy(
                    path = fullRoute,
                    visitedCells = emptyList(),
                    currentCell = null,
                    isRunning = false,
                    currentMode = MapEditMode.VIEW,
                    geneticSummary = when {
                        routePlaces.isEmpty() -> "Не удалось подобрать заведения под выбранные товары."
                        fullRoute.isEmpty() -> "Точки найдены, но связать их маршрутом A* не получилось."
                        result.isValid -> "Маршрут: ${routePlaces.joinToString(" -> ") { it.name }}. Товары: $selectedGoods. Бюджет: ${result.totalPrice.toInt()} ₽."
                        else -> "Лучший найденный маршрут: ${routePlaces.joinToString(" -> ") { it.name }}. Товары: $selectedGoods."
                    },
                    statusMessage = if (fullRoute.isNotEmpty()) {
                        "Маршрут для сбора еды готов."
                    } else {
                        "Не удалось построить полный маршрут."
                    }
                )
                scheduleStatusReset()
            }
        }
    }

    fun runAntLandmarksRoute(selectedLandmarkIds: Set<String> = emptySet()) {
        cancelStatusReset()
        viewModelScope.launch(Dispatchers.Default) {
            withContext(Dispatchers.Main) {
                uiState = uiState.copy(
                    path = emptyList(),
                    visitedCells = emptyList(),
                    currentCell = null,
                    isRunning = true,
                    antSummary = null,
                    geneticSummary = null,
                    statusMessage = "Строим экскурсионный маршрут..."
                )
            }

            val landmarks = SamplePlaces.landmarks
                .filter { selectedLandmarkIds.isEmpty() || it.id in selectedLandmarkIds }
                .map { Landmark(it.id, it.name, it.point, it.description) }

            if (landmarks.size < 2) {
                withContext(Dispatchers.Main) {
                    uiState = uiState.copy(
                        isRunning = false,
                        antSummary = "Выберите минимум две достопримечательности.",
                        statusMessage = "Недостаточно точек для построения."
                    )
                    scheduleStatusReset()
                }
                return@launch
            }

            val grid = currentGrid()
            val startPoint = getRouteStartPoint()
            val startLandmark = landmarks.minByOrNull { landmark ->
                aStarDistance(startPoint, landmark.point, grid)
            } ?: landmarks.first()

            val distances = buildAStarDistanceMatrix(landmarks.map { it.point }, grid)
            val route = antColonySolver.solve(landmarks, distances, startLandmark)
            val displayStops = buildList {
                add(startPoint)
                addAll(route.map { it.point })
            }
            val fullRoute = buildAStarPathForStops(displayStops, grid)

            withContext(Dispatchers.Main) {
                uiState = uiState.copy(
                    path = fullRoute,
                    visitedCells = emptyList(),
                    currentCell = null,
                    isRunning = false,
                    currentMode = MapEditMode.VIEW,
                    antSummary = when {
                        route.isEmpty() -> "Маршрут не найден."
                        fullRoute.isEmpty() -> "Точки найдены, но A* между ними маршрут не построил."
                        else -> "Маршрут: ${route.joinToString(" -> ") { it.name }}."
                    },
                    statusMessage = if (fullRoute.isNotEmpty()) {
                        "Маршрут по достопримечательностям готов."
                    } else {
                        "Не удалось построить экскурсионный маршрут."
                    }
                )
                scheduleStatusReset()
            }
        }
    }

    private fun loadGrid() {
        viewModelScope.launch(Dispatchers.Default) {
            withContext(Dispatchers.Main) {
                uiState = uiState.copy(
                    isMapLoading = true,
                    statusMessage = "Подготавливаем карту..."
                )
            }

            val cached = cachedBaseGrid
            val grid = cached ?: createGridFast(uiState.rows, uiState.cols).also {
                cachedBaseGrid = it
            }
            baseGrid = grid

            withContext(Dispatchers.Main) {
                uiState = uiState.copy(
                    cells = cloneGrid(baseGrid),
                    isMapLoading = false,
                    statusMessage = "Карта готова."
                )
                scheduleStatusReset()
            }
        }
    }

    private fun setStartCell(row: Int, col: Int) {
        val targetCell = findNearestWalkableCell(row, col) ?: return
        val updatedCells = clearPreviousStart(uiState.cells).map { rowList ->
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
            pathFound = null,
            geneticSummary = null,
            antSummary = null,
            statusMessage = "Старт установлен."
        )
        scheduleStatusReset()
    }

    private fun setFinishCell(row: Int, col: Int) {
        val targetCell = findNearestWalkableCell(row, col) ?: return
        val updatedCells = clearPreviousFinish(uiState.cells).map { rowList ->
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
            pathFound = null,
            geneticSummary = null,
            antSummary = null,
            statusMessage = "Финиш установлен."
        )
        scheduleStatusReset()
    }

    private fun toggleObstacleCell(row: Int, col: Int) {
        val selectedCell = uiState.cells[row][col]
        if (selectedCell.type == CellType.START || selectedCell.type == CellType.FINISH) return

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
            pathFound = null,
            geneticSummary = null,
            antSummary = null,
            statusMessage = "Ограждения обновлены."
        )
        scheduleStatusReset()
    }

    private fun createGridFast(rows: Int, cols: Int): List<List<MapCell>> {
        val bitmap = BitmapFactory.decodeResource(
            getApplication<Application>().resources,
            R.drawable.campus_map2,
            BitmapFactory.Options().apply {
                inPreferredConfig = BitmapFactory.Options().inPreferredConfig
                inScaled = false
            }
        )

        val sourceWidth = bitmap.width
        val sourceHeight = bitmap.height
        val walkable = Array(rows) { BooleanArray(cols) }

        for (row in 0 until rows) {
            val centerY = ((row + 0.5f) * sourceHeight / rows).roundToInt()
            for (col in 0 until cols) {
                val centerX = ((col + 0.5f) * sourceWidth / cols).roundToInt()
                var isRoad = false

                for (offsetY in -4..4) {
                    if (isRoad) break
                    for (offsetX in -4..4) {
                        val sampleX = centerX + offsetX
                        val sampleY = centerY + offsetY
                        if (sampleX !in 0 until sourceWidth || sampleY !in 0 until sourceHeight) {
                            continue
                        }
                        if (isWalkablePixel(bitmap.getPixel(sampleX, sampleY))) {
                            isRoad = true
                            break
                        }
                    }
                }

                walkable[row][col] = isRoad
            }
        }

        bitmap.recycle()
        val expandedWalkable = expandRoads(walkable)

        return List(rows) { row ->
            List(cols) { col ->
                val type = if (expandedWalkable[row][col]) CellType.EMPTY else CellType.OBSTACLE
                MapCell(row = row, col = col, type = type, baseType = type)
            }
        }
    }

    private fun isWalkablePixel(pixel: Int): Boolean {
        val red = Color.red(pixel)
        val green = Color.green(pixel)
        val blue = Color.blue(pixel)
        return red > 150 && blue > 150 && green < 190 && abs(red - blue) < 100
    }

    private fun expandRoads(source: Array<BooleanArray>): Array<BooleanArray> {
        val rows = source.size
        val cols = source.firstOrNull()?.size ?: 0
        val result = Array(rows) { BooleanArray(cols) }

        for (row in 0 until rows) {
            for (col in 0 until cols) {
                if (!source[row][col]) continue
                result[row][col] = true

                val neighbours = listOf(
                    row - 1 to col,
                    row + 1 to col,
                    row to col - 1,
                    row to col + 1
                )

                neighbours.forEach { (newRow, newCol) ->
                    if (newRow in 0 until rows && newCol in 0 until cols) {
                        result[newRow][newCol] = true
                    }
                }
            }
        }

        return result
    }

    private fun currentGrid(): List<GridCell> {
        return uiState.cells.flatten().map { cell ->
            GridCell(
                point = Point(cell.col, cell.row),
                isWalkable = cell.type != CellType.OBSTACLE
            )
        }
    }

    private fun getRouteStartPoint(): Point {
        return uiState.startCell?.let { Point(it.col, it.row) } ?: Point(78, 72)
    }

    private fun buildMenuItems(cafes: List<Place>): List<MenuItem> {
        val priceByCategory = mapOf(
            FoodCategory.COFFEE to 180.0,
            FoodCategory.PANCAKES to 220.0,
            FoodCategory.FULL_MEAL to 360.0,
            FoodCategory.SNACK to 140.0,
            FoodCategory.DISPOSABLE_TABLEWARE to 60.0
        )

        return cafes.flatMap { place ->
            place.menuItems.mapNotNull { rawItem ->
                val category = rawItem.toFoodCategory() ?: return@mapNotNull null
                MenuItem(
                    id = "${place.id}_$rawItem",
                    name = rawItem.replace('_', ' '),
                    category = category,
                    placeId = place.id,
                    price = priceByCategory.getValue(category)
                )
            }
        }
    }

    private fun String.toFoodCategory(): FoodCategory? {
        return when (this) {
            "coffee" -> FoodCategory.COFFEE
            "pancakes" -> FoodCategory.PANCAKES
            "full_meal" -> FoodCategory.FULL_MEAL
            "snack" -> FoodCategory.SNACK
            "disposable_tableware" -> FoodCategory.DISPOSABLE_TABLEWARE
            else -> null
        }
    }

    private fun buildAStarDistanceMatrix(
        points: List<Point>,
        grid: List<GridCell>
    ): List<List<Double>> {
        val matrix = MutableList(points.size) { MutableList(points.size) { 0.0 } }

        for (row in points.indices) {
            for (col in row until points.size) {
                val distance = if (row == col) {
                    0.0
                } else {
                    aStarDistance(points[row], points[col], grid)
                }
                matrix[row][col] = distance
                matrix[col][row] = distance
            }
        }

        return matrix
    }

    private fun buildAStarDistanceVector(
        start: Point,
        points: List<Point>,
        grid: List<GridCell>
    ): List<Double> {
        return points.map { point ->
            aStarDistance(start, point, grid)
        }
    }

    private fun aStarDistance(
        from: Point,
        to: Point,
        grid: List<GridCell>
    ): Double {
        val path = buildAStarPathSegment(from, to, grid)
        return if (path.isEmpty()) 100_000.0 else path.size.toDouble()
    }

    private fun buildAStarPathForStops(
        stops: List<Point>,
        grid: List<GridCell>
    ): List<Point> {
        if (stops.isEmpty()) return emptyList()
        if (stops.size == 1) return listOf(stops.first())

        val fullPath = mutableListOf<Point>()
        for (index in 0 until stops.lastIndex) {
            val segment = buildAStarPathSegment(stops[index], stops[index + 1], grid)
            if (segment.isEmpty()) return emptyList()

            if (fullPath.isEmpty()) {
                fullPath.addAll(segment)
            } else {
                fullPath.addAll(segment.drop(1))
            }
        }
        return fullPath
    }

    private fun buildAStarPathSegment(
        from: Point,
        to: Point,
        grid: List<GridCell>
    ): List<Point> {
        val start = findNearestWalkableCell(from.y, from.x)?.let { Point(it.col, it.row) } ?: return emptyList()
        val finish = findNearestWalkableCell(to.y, to.x)?.let { Point(it.col, it.row) } ?: return emptyList()
        if (start == finish) return listOf(start)

        return findPathUseCase.execute(
            grid = grid,
            start = start,
            end = finish
        )
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

            for ((offsetRow, offsetCol) in directions) {
                val newRow = currentRow + offsetRow
                val newCol = currentCol + offsetCol

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

    private fun defaultStatusMessage(mode: MapEditMode = uiState.currentMode): String {
        return when (mode) {
            MapEditMode.VIEW -> "Карта готова к работе."
            MapEditMode.SET_START -> "Нажмите на карту, чтобы поставить старт."
            MapEditMode.SET_FINISH -> "Нажмите на карту, чтобы поставить финиш."
            MapEditMode.SET_OBSTACLE -> "Проведите по карте, чтобы добавить ограждения."
        }
    }

    private fun cancelStatusReset() {
        statusResetJob?.cancel()
        statusResetJob = null
    }

    private fun scheduleStatusReset(delayMs: Long = 2600L) {
        cancelStatusReset()
        statusResetJob = viewModelScope.launch {
            delay(delayMs)
            if (!uiState.isRunning) {
                uiState = uiState.copy(statusMessage = defaultStatusMessage())
            }
        }
    }

    companion object {
        private var cachedBaseGrid: List<List<MapCell>>? = null
    }
}

private fun FoodCategory.displayName(): String {
    return when (this) {
        FoodCategory.COFFEE -> "Кофе"
        FoodCategory.PANCAKES -> "Блины"
        FoodCategory.FULL_MEAL -> "Полный обед"
        FoodCategory.SNACK -> "Перекус"
        FoodCategory.DISPOSABLE_TABLEWARE -> "Посуда"
    }
}
