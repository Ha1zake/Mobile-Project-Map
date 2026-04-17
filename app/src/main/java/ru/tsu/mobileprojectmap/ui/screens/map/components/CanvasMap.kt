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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import ru.tsu.mobileprojectmap.domain.algorithms.kmeans.Cluster
import ru.tsu.mobileprojectmap.domain.algorithms.kmeans.KMeansResult
import ru.tsu.mobileprojectmap.domain.algorithms.kmeans.getClusterForPoint
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
    modifier: Modifier = Modifier,
    kMeansResult: KMeansResult? = null,
    allowPlaceTap: Boolean = false,
    showPlaceLabels: Boolean = true,
    onCellClick: (row: Int, col: Int) -> Unit,
    onObstacleDrag: (row: Int, col: Int) -> Unit,
    onClusterClick: (cluster: Cluster) -> Unit,
    onPlaceClick: (place: Place) -> Unit,
    worldWidth: Dp,
    worldHeight: Dp
) {
    if (cells.isEmpty() || cells.first().isEmpty()) return

    val rows = cells.size
    val cols = cells.first().size
    val interactionsEnabled = currentMode != MapEditMode.VIEW || allowPlaceTap
    var lastDraggedCell by remember { mutableStateOf<Pair<Int, Int>?>(null) }

    Canvas(
        modifier = modifier
            .size(worldWidth, worldHeight)
            .then(
                if (!interactionsEnabled) {
                    Modifier
                } else {
                    Modifier.pointerInput(cells, currentMode, kMeansResult, allowPlaceTap) {
                        detectTapGestures { tapOffset ->
                            val cellWidth = size.width / cols
                            val cellHeight = size.height / rows

                            if (allowPlaceTap && currentMode == MapEditMode.VIEW) {
                                val tappedCluster = kMeansResult?.clusters?.firstOrNull { cluster ->
                                    val centerX = cluster.centroid.x.toFloat() * cellWidth + cellWidth / 2f
                                    val centerY = cluster.centroid.y.toFloat() * cellHeight + cellHeight / 2f
                                    val dx = tapOffset.x - centerX
                                    val dy = tapOffset.y - centerY
                                    sqrt(dx * dx + dy * dy) <= minOf(cellWidth, cellHeight) * 8.2f
                                }

                                if (tappedCluster != null) {
                                    onClusterClick(tappedCluster)
                                    return@detectTapGestures
                                }

                                val tappedPlace = places.minByOrNull { place ->
                                    val centerX = place.point.x * cellWidth + cellWidth / 2f
                                    val centerY = place.point.y * cellHeight + cellHeight / 2f
                                    val dx = tapOffset.x - centerX
                                    val dy = tapOffset.y - centerY
                                    sqrt(dx * dx + dy * dy)
                                }

                                if (tappedPlace != null) {
                                    val centerX = tappedPlace.point.x * cellWidth + cellWidth / 2f
                                    val centerY = tappedPlace.point.y * cellHeight + cellHeight / 2f
                                    val dx = tapOffset.x - centerX
                                    val dy = tapOffset.y - centerY
                                    val distance = sqrt(dx * dx + dy * dy)
                                    if (distance <= minOf(cellWidth, cellHeight) * 3.4f) {
                                        onPlaceClick(tappedPlace)
                                        return@detectTapGestures
                                    }
                                }
                            }

                            val col = (tapOffset.x / cellWidth).toInt()
                            val row = (tapOffset.y / cellHeight).toInt()

                            if (row in 0 until rows && col in 0 until cols) {
                                onCellClick(row, col)
                            }
                        }
                    }
                }
            )
            .then(
                if (currentMode != MapEditMode.SET_OBSTACLE) {
                    Modifier
                } else {
                    Modifier.pointerInput(cells, currentMode) {
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
                            onDragEnd = { lastDraggedCell = null },
                            onDragCancel = { lastDraggedCell = null },
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
                }
            )
    ) {
        val cellWidth = size.width / cols
        val cellHeight = size.height / rows
        val markerRadius = minOf(cellWidth, cellHeight)

        cells.forEach { rowList ->
            rowList.forEach { cell ->
                if (cell.type == CellType.OBSTACLE && cell.baseType == CellType.EMPTY) {
                    drawRect(
                        color = Color(0x88000000),
                        topLeft = Offset(cell.col * cellWidth, cell.row * cellHeight),
                        size = Size(cellWidth, cellHeight)
                    )
                }
            }
        }

        if (path.size > 1) {
            for (index in 0 until path.lastIndex) {
                val from = path[index]
                val to = path[index + 1]
                drawLine(
                    color = Color(0xFF1E88E5),
                    start = Offset(
                        x = from.x * cellWidth + cellWidth / 2f,
                        y = from.y * cellHeight + cellHeight / 2f
                    ),
                    end = Offset(
                        x = to.x * cellWidth + cellWidth / 2f,
                        y = to.y * cellHeight + cellHeight / 2f
                    ),
                    strokeWidth = markerRadius * 1.8f + 2f,
                    cap = StrokeCap.Round
                )
            }
        }

        visitedCells.forEach { point ->
            val cell = cells[point.y][point.x]
            if (cell.type != CellType.START && cell.type != CellType.FINISH) {
                drawRect(
                    color = Color(0x55FFB300),
                    topLeft = Offset(point.x * cellWidth, point.y * cellHeight),
                    size = Size(cellWidth, cellHeight)
                )
            }
        }

        currentCell?.let { point ->
            val cell = cells[point.y][point.x]
            if (cell.type != CellType.START && cell.type != CellType.FINISH) {
                drawRect(
                    color = Color(0xAAFF7043),
                    topLeft = Offset(point.x * cellWidth, point.y * cellHeight),
                    size = Size(cellWidth, cellHeight)
                )
            }
        }

        cells.forEach { rowList ->
            rowList.forEach { cell ->
                if (cell.type != CellType.START && cell.type != CellType.FINISH) return@forEach

                val center = Offset(
                    x = cell.col * cellWidth + cellWidth / 2f,
                    y = cell.row * cellHeight + cellHeight / 2f
                )
                val markerColor = when (cell.type) {
                    CellType.START -> Color(0xFF2E7D32)
                    CellType.FINISH -> Color(0xFFC62828)
                    else -> Color.Transparent
                }

                drawCircle(color = Color.White, radius = markerRadius * 2.8f, center = center)
                drawCircle(color = markerColor, radius = markerRadius * 2f, center = center)
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
            Color(0xFFC62828),
            Color(0xFF1565C0),
            Color(0xFF2E7D32),
            Color(0xFF6A1B9A),
            Color(0xFFEF6C00)
        )

        places.forEach { place ->
            val centerX = place.point.x * cellWidth + cellWidth / 2f
            val centerY = place.point.y * cellHeight + cellHeight / 2f
            val cluster = kMeansResult?.getClusterForPoint(place.id)
            val clusterColor = cluster?.let { clusterColors[it.id % clusterColors.size] }

            val placeColor = when (place.type) {
                PlaceType.CAFE -> clusterColor ?: Color(0xFF0D47A1)
                PlaceType.COWORKING -> clusterColor ?: Color(0xFF6A1B9A)
                PlaceType.LANDMARK -> Color(0xFFF57C00)
            }

            drawCircle(
                color = Color.White,
                radius = markerRadius * 3.1f,
                center = Offset(centerX, centerY)
            )
            drawCircle(
                color = placeColor,
                radius = markerRadius * 2.3f,
                center = Offset(centerX, centerY)
            )

            if (place.type == PlaceType.CAFE) {
                val iconSize = markerRadius * 3.0f
                val cartLeft = centerX - iconSize * 0.42f
                val cartTop = centerY - iconSize * 0.30f
                val cartRight = centerX + iconSize * 0.38f
                val cartBottom = centerY + iconSize * 0.18f
                val cartColor = Color.White
                val cartStroke = (markerRadius * 0.42f).coerceAtLeast(2f)

                drawLine(
                    color = cartColor,
                    start = Offset(cartLeft, cartTop),
                    end = Offset(cartLeft + iconSize * 0.18f, cartBottom),
                    strokeWidth = cartStroke,
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = cartColor,
                    start = Offset(cartLeft + iconSize * 0.16f, cartTop + iconSize * 0.12f),
                    end = Offset(cartRight, cartTop + iconSize * 0.12f),
                    strokeWidth = cartStroke,
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = cartColor,
                    start = Offset(cartLeft + iconSize * 0.23f, cartBottom),
                    end = Offset(cartRight - iconSize * 0.05f, cartBottom),
                    strokeWidth = cartStroke,
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = cartColor,
                    start = Offset(cartRight, cartTop + iconSize * 0.12f),
                    end = Offset(cartRight - iconSize * 0.09f, cartBottom),
                    strokeWidth = cartStroke,
                    cap = StrokeCap.Round
                )
                drawCircle(
                    color = cartColor,
                    radius = markerRadius * 0.38f,
                    center = Offset(cartLeft + iconSize * 0.30f, centerY + iconSize * 0.35f)
                )
                drawCircle(
                    color = cartColor,
                    radius = markerRadius * 0.38f,
                    center = Offset(cartRight - iconSize * 0.16f, centerY + iconSize * 0.35f)
                )
            } else if (place.type == PlaceType.LANDMARK) {
                val iconSize = markerRadius * 3.0f
                val statueColor = Color.White
                val statueStroke = (markerRadius * 0.36f).coerceAtLeast(2f)
                val topY = centerY - iconSize * 0.36f
                val baseY = centerY + iconSize * 0.34f
                val leftX = centerX - iconSize * 0.38f
                val rightX = centerX + iconSize * 0.38f

                drawLine(
                    color = statueColor,
                    start = Offset(centerX, topY),
                    end = Offset(leftX, centerY - iconSize * 0.10f),
                    strokeWidth = statueStroke,
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = statueColor,
                    start = Offset(centerX, topY),
                    end = Offset(rightX, centerY - iconSize * 0.10f),
                    strokeWidth = statueStroke,
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = statueColor,
                    start = Offset(leftX + iconSize * 0.10f, centerY - iconSize * 0.02f),
                    end = Offset(rightX - iconSize * 0.10f, centerY - iconSize * 0.02f),
                    strokeWidth = statueStroke,
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = statueColor,
                    start = Offset(centerX - iconSize * 0.18f, centerY - iconSize * 0.02f),
                    end = Offset(centerX - iconSize * 0.18f, baseY - iconSize * 0.14f),
                    strokeWidth = statueStroke,
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = statueColor,
                    start = Offset(centerX, centerY - iconSize * 0.02f),
                    end = Offset(centerX, baseY - iconSize * 0.14f),
                    strokeWidth = statueStroke,
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = statueColor,
                    start = Offset(centerX + iconSize * 0.18f, centerY - iconSize * 0.02f),
                    end = Offset(centerX + iconSize * 0.18f, baseY - iconSize * 0.14f),
                    strokeWidth = statueStroke,
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = statueColor,
                    start = Offset(leftX + iconSize * 0.04f, baseY - iconSize * 0.08f),
                    end = Offset(rightX - iconSize * 0.04f, baseY - iconSize * 0.08f),
                    strokeWidth = statueStroke,
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = statueColor,
                    start = Offset(leftX, baseY),
                    end = Offset(rightX, baseY),
                    strokeWidth = statueStroke,
                    cap = StrokeCap.Round
                )
            }

            if (showPlaceLabels) {
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
        }

        kMeansResult?.clusters?.forEach { cluster ->
            val centerX = cluster.centroid.x.toFloat() * cellWidth + cellWidth / 2f
            val centerY = cluster.centroid.y.toFloat() * cellHeight + cellHeight / 2f
            val clusterColor = clusterColors[cluster.id % clusterColors.size]

            drawCircle(
                color = clusterColor.copy(alpha = 0.24f),
                radius = markerRadius * 6f,
                center = Offset(centerX, centerY)
            )
            drawCircle(
                color = clusterColor,
                radius = markerRadius * 3.4f,
                center = Offset(centerX, centerY)
            )
            drawCircle(
                color = Color.White,
                radius = markerRadius * 1.5f,
                center = Offset(centerX, centerY)
            )
        }

        if (currentMode != MapEditMode.VIEW) {
            for (col in 0..cols) {
                val x = col * cellWidth
                drawLine(
                    color = Color.White.copy(alpha = 0.28f),
                    start = Offset(x, 0f),
                    end = Offset(x, size.height),
                    strokeWidth = 1f
                )
            }

            for (row in 0..rows) {
                val y = row * cellHeight
                drawLine(
                    color = Color.White.copy(alpha = 0.28f),
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1f
                )
            }
        }
    }
}
