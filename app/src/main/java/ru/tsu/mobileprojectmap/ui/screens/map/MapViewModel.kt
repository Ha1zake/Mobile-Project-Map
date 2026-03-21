package ru.tsu.mobileprojectmap.ui.screens.map

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import ru.tsu.mobileprojectmap.ui.screens.map.model.CellType
import ru.tsu.mobileprojectmap.ui.screens.map.model.MapCell
import ru.tsu.mobileprojectmap.ui.screens.map.model.MapEditMode

class MapViewModel : ViewModel(){
    var uiState by mutableStateOf(MapUIState())
        private set
    init{
        uiState = uiState.copy(
            cells = createGrid(uiState.rows, uiState.cols)
        )
    }
    private fun createGrid(rows: Int, cols: Int): List<List<MapCell>>{
        return List(rows){row ->
            List(cols) { col ->
                MapCell(
                    row = row,
                    col = col,
                    type = CellType.EMPTY
                )
            }
        }
    }
    fun setMode(mode: MapEditMode) {
        uiState = uiState.copy(currentMode = mode)
    }
    fun resetMap(){
        uiState = uiState.copy(
            cells = createGrid(uiState.rows,uiState.cols),
            startCell = null,
            finishCell = null
        )
    }
    fun onCellClick(row: Int, col: Int){
        when(uiState.currentMode){
            MapEditMode.SET_START -> setStartCell(row,col)
            MapEditMode.SET_FINISH -> setFinishCell(row,col)
            MapEditMode.SET_OBSTACLE -> setObstacleCell(row,col)
        }
    }
    private fun setStartCell(row: Int, col: Int) {
        var updatedCells = uiState.cells
        uiState.startCell?.let { oldStart ->
            updatedCells = updatedCells.map { rowList ->
                rowList.map { cell ->
                    if (cell.row == oldStart.row && cell.col == oldStart.col) {
                        cell.copy(type = CellType.START)
                    } else {
                        cell
                    }
                }
            }

            val newStart = updatedCells[row][col]
            uiState = uiState.copy(
                cells = updatedCells,
                startCell = newStart
            )
        }
    }
    private fun setFinishCell(row: Int, col:Int){
        var updatedCells = uiState.cells
        uiState.finishCell?.let {oldFinish ->
            updatedCells = updatedCells.map {rowList ->
                rowList.map {cell ->
                    if(cell.row == oldFinish.row && cell.col == oldFinish.col){
                        cell.copy(type = CellType.FINISH)
                    } else{
                        cell
                    }
                }
            }
            val newFinish = updatedCells[row][col]
            uiState = uiState.copy(
                cells = updatedCells,
                startCell = newFinish
            )
        }
    }
    private fun setObstacleCell(row: Int, col: Int){
        val updatedCells = uiState.cells.map { rowList ->
            rowList.map { cell ->
                if (cell.row == row && cell.col == col) {
                    cell.copy(type = CellType.OBSTACLE)
                } else {
                    cell
                }
            }
        }
        uiState = uiState.copy(cells = updatedCells)
    }
}

