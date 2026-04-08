package ru.tsu.mobileprojectmap.ui.screens.map

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import androidx.lifecycle.ViewModel
import ru.tsu.mobileprojectmap.ui.screens.map.model.CellType
import ru.tsu.mobileprojectmap.ui.screens.map.model.MapCell
import ru.tsu.mobileprojectmap.ui.screens.map.model.MapEditMode
import androidx.lifecycle.AndroidViewModel
import ru.tsu.mobileprojectmap.R
import ru.tsu.mobileprojectmap.domain.algorithms.astar.AStarPathfinder
import ru.tsu.mobileprojectmap.domain.model.GridCell
import ru.tsu.mobileprojectmap.domain.model.Point
import ru.tsu.mobileprojectmap.domain.usecase.FindPathUseCase
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
class MapViewModel(application: Application) : AndroidViewModel(application) {

    var uiState by mutableStateOf(MapUIState())
        private set

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
        uiState = uiState.copy(currentMode = mode)
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
            isRunning = false
        )
    }

    fun onCellClick(row: Int, col: Int) {
        when (uiState.currentMode) {
            MapEditMode.SET_START -> setStartCell(row, col)
            MapEditMode.SET_FINISH -> setFinishCell(row, col)
            MapEditMode.SET_OBSTACLE -> toggleObstacleCell(row, col)
        }
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
            isRunning = false
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
            isRunning = false
        )
    }

    private fun toggleObstacleCell(row: Int, col: Int) {
        val currentCell = uiState.cells[row][col]

        if (currentCell.type == CellType.START || currentCell.type == CellType.FINISH) {
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
            isRunning = false
        )
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
        var totalCount = 0

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
                totalCount++
            }
        }


        return purpleCount >= 2
    }
    private val findPathUseCase = FindPathUseCase(AStarPathfinder())
    private var pathJob: Job? = null
    fun findPath() {
        val start = uiState.startCell
        val finish = uiState.finishCell

        if (start == null || finish == null) {
            uiState = uiState.copy(
                path = emptyList(),
                visitedCells = emptyList(),
                currentCell = null,
                isRunning = false
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
                isRunning = true
            )

            val resultPath = findPathUseCase.execute(
                grid = grid,
                start = Point(x = start.col, y = start.row),
                end = Point(x = finish.col, y = finish.row)
            )

            uiState = uiState.copy(
                path = resultPath,
                visitedCells = emptyList(),
                currentCell = null,
                isRunning = false
            )
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
}


