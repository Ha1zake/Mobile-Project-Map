package ru.tsu.mobileprojectmap.ui.screens.map

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
import ru.tsu.mobileprojectmap.domain.model.GridCell
import ru.tsu.mobileprojectmap.domain.model.Point
import ru.tsu.mobileprojectmap.domain.usecase.FindPathUseCase
import ru.tsu.mobileprojectmap.ui.screens.map.model.CellType
import ru.tsu.mobileprojectmap.ui.screens.map.model.MapCell
import ru.tsu.mobileprojectmap.ui.screens.map.model.MapEditMode

class MapViewModel(application: Application) : AndroidViewModel(application) {

    var uiState by mutableStateOf(MapUIState())
        private set

    private val findPathUseCase = FindPathUseCase(AStarPathfinder())
    private var pathJob: Job? = null

    init {
        uiState = uiState.copy(
            cells = createGrid(uiState.rows, uiState.cols)
        )
    }

    private fun createGrid(rows: Int, cols: Int): List<List<MapCell>> {
        val bitmap = BitmapFactory.decodeResource(
            getApplication<Application>().resources,
            R.drawable.campus_map2
        )

        return List(rows) { row ->
            List(cols) { col ->
                val isWalkable = isWalkableFromImage(
                    bitmap = bitmap,
                    row = row,
                    col = col,
                    totalRows = rows,
                    totalCols = cols
                )
                MapCell(
                    row = row,
                    col = col,
                    type = if (isWalkable) CellType.EMPTY else CellType.OBSTACLE,
                    baseType = if (isWalkable) CellType.EMPTY else CellType.OBSTACLE
                )
            }
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
            cells = createGrid(uiState.rows, uiState.cols),
            startCell = null,
            finishCell = null,
            path = emptyList(),
            visitedCells = emptyList(),
            currentCell = null,
            isRunning = false,
            statusMessage = "Карта сброшена",
            pathFound = null
        )
    }

    fun onCellClick(row: Int, col: Int) {
        when (uiState.currentMode) {
            MapEditMode.VIEW -> Unit
            MapEditMode.SET_START -> setStartCell(row, col)
            MapEditMode.SET_FINISH -> setFinishCell(row, col)
            MapEditMode.SET_OBSTACLE -> toggleObstacleCell(row, col)
        }
    }

    fun drawObstacleCell(row: Int, col: Int) {
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
            pathFound = null
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
            pathFound = null
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
            pathFound = null
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
            pathFound = null
        )
    }

    fun findPath() {
        val start = uiState.startCell
        val finish = uiState.finishCell

        if (start == null || finish == null) {
            uiState = uiState.copy(
                path = emptyList(),
                visitedCells = emptyList(),
                currentCell = null,
                isRunning = false,
                statusMessage = "Сначала укажите старт и финиш",
                pathFound = null
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
                pathFound = null
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

    private fun isWalkableFromImage(
        bitmap: Bitmap,
        row: Int,
        col: Int,
        totalRows: Int,
        totalCols: Int
    ): Boolean {
        val startX =
            (col.toFloat() * bitmap.width / totalCols).toInt().coerceIn(0, bitmap.width - 1)
        val endX = (((col + 1).toFloat() * bitmap.width / totalCols).toInt() - 1).coerceIn(
            0,
            bitmap.width - 1
        )

        val startY =
            (row.toFloat() * bitmap.height / totalRows).toInt().coerceIn(0, bitmap.height - 1)
        val endY = (((row + 1).toFloat() * bitmap.height / totalRows).toInt() - 1).coerceIn(
            0,
            bitmap.height - 1
        )

        var purpleCount = 0

        for (x in startX..endX) {
            for (y in startY..endY) {
                val pixel = bitmap.getPixel(x, y)
                val r = android.graphics.Color.red(pixel)
                val g = android.graphics.Color.green(pixel)
                val b = android.graphics.Color.blue(pixel)

                val isPurple =
                    r > 120 &&
                        b > 120 &&
                        g < 140 &&
                        kotlin.math.abs(r - b) < 80

                if (isPurple) {
                    purpleCount++
                }
            }
        }

        return purpleCount >= 2
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
}
