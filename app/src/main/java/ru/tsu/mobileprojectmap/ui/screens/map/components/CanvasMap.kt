package ru.tsu.mobileprojectmap.ui.screens.map.components
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.geometry.Size
import ru.tsu.mobileprojectmap.domain.model.Point
import ru.tsu.mobileprojectmap.ui.screens.map.model.CellType
import ru.tsu.mobileprojectmap.ui.screens.map.model.MapCell


@Composable
fun CanvasMap(
    cells: List<List<MapCell>>,
    path: List<Point>,
    onCellClick: (row: Int, col: Int) -> Unit,
    worldWidth: Dp,
    worldHeight: Dp,
    modifier: Modifier = Modifier
) {
    if (cells.isEmpty() || cells.first().isEmpty()) return

    val rows = cells.size
    val cols = cells.first().size

    Canvas(
        modifier = modifier
            .size(worldWidth, worldHeight)
            .pointerInput(cells) {
                detectTapGestures { offset: Offset ->
                    val cellWidth = size.width / cols
                    val cellHeight = size.height / rows

                    val col = (offset.x / cellWidth).toInt()
                    val row = (offset.y / cellHeight).toInt()

                    if (row in 0 until rows && col in 0 until cols) {
                        onCellClick(row, col)
                    }
                }
            }
    ) {
        val cellWidth = size.width / cols
        val cellHeight = size.height / rows
        cells.forEach { rowList ->
            rowList.forEach { cell ->
                val left = cell.col * cellWidth
                val top = cell.row * cellHeight

                val color = when (cell.type) {
                    CellType.EMPTY -> Color.Transparent
                    CellType.START -> Color(0xAA4CAF50)
                    CellType.FINISH -> Color(0xAAF44336)
                    CellType.OBSTACLE -> Color(0x33000000)
                }

                if (cell.type != CellType.EMPTY) {
                    drawRect(
                        color = color,
                        topLeft = Offset(left, top),
                        size = Size(cellWidth, cellHeight)
                    )
                }
            }
        }
        path.forEach { point ->
            val isStart = cells[point.y][point.x].type == CellType.START
            val isFinish = cells[point.y][point.x].type == CellType.FINISH

            if (!isStart && !isFinish) {
                val left = point.x * cellWidth
                val top = point.y * cellHeight

                drawRect(
                    color = Color(0xAA2196F3),
                    topLeft = Offset(left, top),
                    size = Size(cellWidth, cellHeight)
                )
            }
        }
        for (col in 0..cols) {
            val x = col * cellWidth
            drawLine(
                color = Color.White.copy(alpha = 0.35f),
                start = Offset(x, 0f),
                end = Offset(x, size.height),
                strokeWidth = 1f
            )
        }

        for (row in 0..rows) {
            val y = row * cellHeight
            drawLine(
                color = Color.White.copy(alpha = 0.35f),
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 1f
            )
        }
    }
}