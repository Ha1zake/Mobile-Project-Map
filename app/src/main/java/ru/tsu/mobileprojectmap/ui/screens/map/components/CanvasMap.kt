package ru.tsu.mobileprojectmap.ui.screens.map.components

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import ru.tsu.mobileprojectmap.domain.algorithms.kmeans.KMeansResult
import ru.tsu.mobileprojectmap.domain.algorithms.kmeans.getClusterForPoint
import ru.tsu.mobileprojectmap.domain.algorithms.kmeans.Cluster
import ru.tsu.mobileprojectmap.domain.model.Place
import ru.tsu.mobileprojectmap.domain.model.PlaceType
import ru.tsu.mobileprojectmap.domain.model.Point
import ru.tsu.mobileprojectmap.ui.screens.map.model.CellType
import ru.tsu.mobileprojectmap.ui.screens.map.model.MapCell
import ru.tsu.mobileprojectmap.ui.screens.map.model.MapEditMode
import kotlin.math.sqrt

@Composable
fun CanvasMap(
    cells: List<List<MapCell>>,
    path: List<Point>,
    visitedCells: List<Point>,
    currentCell: Point?,
    places: List<Place>,
    currentMode: MapEditMode,
    kMeansResult: KMeansResult? = null,
    onCellClick: (row: Int, col: Int) -> Unit,
    onObstacleDrag: (row: Int, col: Int) -> Unit,
    onClusterClick: (cluster: Cluster) -> Unit,
    onPlaceClick: (place: Place) -> Unit,
    worldWidth: Dp,
    worldHeight: Dp,
    modifier: Modifier = Modifier
) {
    if (cells.isEmpty() || cells.first().isEmpty()) return

    val rows = cells.size
    val cols = cells.first().size
    var lastDraggedCell by remember { mutableStateOf<Pair<Int, Int>?>(null) }

    Canvas(
        modifier = modifier
            .size(worldWidth, worldHeight)
            // Ключи должны включать `kMeansResult`, иначе обработчик кликов может "залипнуть"
            // на старом значении (например, когда кластеры были ещё null).
            .pointerInput(cells, currentMode, kMeansResult) {
                detectTapGestures { tapOffset ->
                    val cellWidth = size.width / cols
                    val cellHeight = size.height / rows

                    val tappedCluster = if (currentMode == MapEditMode.VIEW) {
                        kMeansResult?.clusters?.firstOrNull { cluster ->
                            val centerX = cluster.centroid.x.toFloat() * cellWidth + cellWidth / 2f
                            val centerY = cluster.centroid.y.toFloat() * cellHeight + cellHeight / 2f
                            val dx = tapOffset.x - centerX
                            val dy = tapOffset.y - centerY
                            val distance = sqrt(dx * dx + dy * dy)
                            // Попадание делаем ближе к тому, как визуально рисуется кластер.
                            distance <= minOf(cellWidth, cellHeight) * 5.8f
                        }
                    } else {
                        null
                    }

                    if (tappedCluster != null) {
                        onClusterClick(tappedCluster)
                        return@detectTapGestures
                    }

                    val tappedPlace = if (currentMode == MapEditMode.VIEW) {
                        var bestPlace: Place? = null
                        var bestDistance = Float.MAX_VALUE
                        val maxTapRadius = minOf(cellWidth, cellHeight) * 3.4f

                        places.forEach { place ->
                            val centerX = place.point.x * cellWidth + cellWidth / 2f
                            val centerY = place.point.y * cellHeight + cellHeight / 2f
                            val dx = tapOffset.x - centerX
                            val dy = tapOffset.y - centerY
                            val dist = sqrt(dx * dx + dy * dy)
                            if (dist <= maxTapRadius && dist < bestDistance) {
                                bestDistance = dist
                                bestPlace = place
                            }
                        }
                        bestPlace
                    } else {
                        null
                    }

                    if (tappedPlace != null) {
                        onPlaceClick(tappedPlace)
                        return@detectTapGestures
                    }

                    val col = (tapOffset.x / cellWidth).toInt()
                    val row = (tapOffset.y / cellHeight).toInt()

                    if (row in 0 until rows && col in 0 until cols) {
                        onCellClick(row, col)
                    }
                }
            }
            .pointerInput(cells, currentMode) {
                if (currentMode != MapEditMode.SET_OBSTACLE) return@pointerInput

                detectDragGestures(
                    onDragStart = { offset ->
                        val cellWidth = size.width / cols
                        val cellHeight = size.height / rows
                        val col = (offset.x / cellWidth).toInt()
                        val row = (offset.y / cellHeight).toInt()

                        if (row in 0 until rows && col in 0 until cols) {
                            lastDraggedCell = row to col
                            onObstacleDrag(row, col)
                        }
                    },
                    onDragEnd = {
                        lastDraggedCell = null
                    },
                    onDragCancel = {
                        lastDraggedCell = null
                    },
                    onDrag = { change, _ ->
                        val cellWidth = size.width / cols
                        val cellHeight = size.height / rows
                        val col = (change.position.x / cellWidth).toInt()
                        val row = (change.position.y / cellHeight).toInt()

                        if (row !in 0 until rows || col !in 0 until cols) return@detectDragGestures

                        val draggedCell = row to col
                        if (lastDraggedCell != draggedCell) {
                            lastDraggedCell = draggedCell
                            onObstacleDrag(row, col)
                        }
                    }
                )
            }
    ) {
        val cellWidth = size.width / cols
        val cellHeight = size.height / rows

        cells.forEach { rowList ->
            rowList.forEach { cell ->
                val left = cell.col * cellWidth
                val top = cell.row * cellHeight

                val shouldDraw = when (cell.type) {
                    CellType.START, CellType.FINISH -> false
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

        cells.forEach { rowList ->
            rowList.forEach { cell ->
                if (cell.type != CellType.START && cell.type != CellType.FINISH) return@forEach

                val centerX = cell.col * cellWidth + cellWidth / 2f
                val centerY = cell.row * cellHeight + cellHeight / 2f
                val outerRadius = minOf(cellWidth, cellHeight) * 2.3f
                val innerRadius = minOf(cellWidth, cellHeight) * 1.8f
                val markerColor = when (cell.type) {
                    CellType.START -> Color(0xFF43A047)
                    CellType.FINISH -> Color(0xFFE53935)
                    else -> Color.Transparent
                }

                drawCircle(
                    color = Color.White,
                    radius = outerRadius,
                    center = Offset(centerX, centerY)
                )

                drawCircle(
                    color = markerColor,
                    radius = innerRadius,
                    center = Offset(centerX, centerY)
                )
            }
        }

        visitedCells.forEach { point ->
            val isStart = cells[point.y][point.x].type == CellType.START
            val isFinish = cells[point.y][point.x].type == CellType.FINISH

            if (!isStart && !isFinish) {
                val left = point.x * cellWidth
                val top = point.y * cellHeight

                drawRect(
                    color = Color(0x55FFB300),
                    topLeft = Offset(left, top),
                    size = Size(cellWidth, cellHeight)
                )
            }
        }

        currentCell?.let { point ->
            val isStart = cells[point.y][point.x].type == CellType.START
            val isFinish = cells[point.y][point.x].type == CellType.FINISH

            if (!isStart && !isFinish) {
                val left = point.x * cellWidth
                val top = point.y * cellHeight

                drawRect(
                    color = Color(0xAAFF5722),
                    topLeft = Offset(left, top),
                    size = Size(cellWidth, cellHeight)
                )
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
            val clusterColor = cluster?.let { clusterColors[it.id % clusterColors.size] }

            val placeColor = when (place.type) {
                PlaceType.CAFE -> clusterColor ?: Color(0xFF1976D2)
                PlaceType.COWORKING -> clusterColor ?: Color(0xFF7B1FA2)
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

                drawText(text, textX, textY, textPaint)
            }
        }

        kMeansResult?.clusters?.forEach { cluster ->
            val centerX = cluster.centroid.x.toFloat() * cellWidth + cellWidth / 2f
            val centerY = cluster.centroid.y.toFloat() * cellHeight + cellHeight / 2f
            val clusterColor = clusterColors[cluster.id % clusterColors.size]

            drawCircle(
                color = clusterColor.copy(alpha = 0.35f),
                radius = minOf(cellWidth, cellHeight) * 5.2f,
                center = Offset(centerX, centerY)
            )

            drawCircle(
                color = clusterColor,
                radius = minOf(cellWidth, cellHeight) * 3.2f,
                center = Offset(centerX, centerY)
            )

            drawCircle(
                color = Color.White,
                radius = minOf(cellWidth, cellHeight) * 1.4f,
                center = Offset(centerX, centerY)
            )
        }

        // Постоянная отрисовка всей сетки 200x200 заметно нагружает Canvas.
        // Оставляем её только в режимах, где сетка действительно помогает взаимодействию.
        if (currentMode != MapEditMode.VIEW) {
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
}
