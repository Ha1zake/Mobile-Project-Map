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
import ru.tsu.mobileprojectmap.domain.model.Place
import ru.tsu.mobileprojectmap.domain.model.Point
import ru.tsu.mobileprojectmap.ui.screens.map.model.CellType
import ru.tsu.mobileprojectmap.ui.screens.map.model.MapCell
import ru.tsu.mobileprojectmap.domain.model.PlaceType
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import android.graphics.Paint
import ru.tsu.mobileprojectmap.domain.algorithms.kmeans.KMeansResult
import ru.tsu.mobileprojectmap.domain.algorithms.kmeans.getClusterForPoint


@Composable
fun CanvasMap(
    cells: List<List<MapCell>>,
    path: List<Point>,
    visitedCells: List<Point>,
    currentCell: Point?,
    places: List<Place>,
    kMeansResult: KMeansResult? = null,
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
                detectTapGestures { tapOffset: Offset ->
                    val cellWidth = size.width / cols
                    val cellHeight = size.height / rows

                    val col = (tapOffset.x / cellWidth).toInt()
                    val row = (tapOffset.y / cellHeight).toInt()

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

                val shouldDraw = when (cell.type) {
                    CellType.START, CellType.FINISH -> true
                    CellType.OBSTACLE -> cell.baseType == CellType.EMPTY
                    CellType.EMPTY -> false
                }

                val color = when (cell.type) {
                    CellType.EMPTY -> Color.Transparent
                    CellType.START -> Color(0xAA4CAF50)
                    CellType.FINISH -> Color(0xAAF44336)
                    CellType.OBSTACLE -> Color(0x88000000)
                }

                if (shouldDraw) {
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
        val textPaint = Paint().apply {
            color = android.graphics.Color.BLACK
            textSize = 28f
            isAntiAlias = true
            style = Paint.Style.FILL
        }

        val textBgPaint = Paint().apply {
            color = android.graphics.Color.WHITE
            alpha = 220
            style = Paint.Style.FILL
            isAntiAlias = true
        }

        val clusterColors = listOf(
            Color(0xFFE53935),
            Color(0xFF1E88E5),
            Color(0xFF43A047),
            Color(0xFF8E24AA),
            Color(0xFFFB8C00)
        )
        places.forEach { place ->
            val centerX = place.point.x * cellWidth + cellWidth / 2f
            val centerY = place.point.y * cellHeight + cellHeight / 2f

            val cluster = kMeansResult?.getClusterForPoint(place.id)

            val placeColor = when (place.type) {
                PlaceType.CAFE -> {
                    if (cluster != null) {
                        clusterColors[cluster.id % clusterColors.size]
                    } else {
                        Color(0xFF1976D2)
                    }
                }
                PlaceType.COWORKING -> Color(0xFF7B1FA2)
                PlaceType.LANDMARK -> Color(0xFFFF9800)
            }


            drawCircle(
                color = Color.White,
                radius = minOf(cellWidth, cellHeight) * 3.2f,
                center = Offset(centerX, centerY)
            )

            drawCircle(
                color = placeColor,
                radius = minOf(cellWidth, cellHeight) * 2.4f,
                center = Offset(centerX, centerY)
            )

            drawContext.canvas.nativeCanvas.apply {
                val text = place.name
                val textWidth = textPaint.measureText(text)
                val textX = centerX + 18f
                val textY = centerY - 18f

                drawRoundRect(
                    textX - 10f,
                    textY - 30f,
                    textX + textWidth + 10f,
                    textY + 10f,
                    10f,
                    10f,
                    textBgPaint
                )

                drawText(
                    text,
                    textX,
                    textY,
                    textPaint
                )
            }

        }
        kMeansResult?.clusters?.forEach { cluster ->
            val centerX = cluster.centroid.x.toFloat() * cellWidth + cellWidth / 2f
            val centerY = cluster.centroid.y.toFloat() * cellHeight + cellHeight / 2f

            drawCircle(
                color = Color.Black,
                radius = minOf(cellWidth, cellHeight) * 3.5f,
                center = Offset(centerX, centerY)
            )

            drawCircle(
                color = Color.White,
                radius = minOf(cellWidth, cellHeight) * 2.2f,
                center = Offset(centerX, centerY)
            )
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