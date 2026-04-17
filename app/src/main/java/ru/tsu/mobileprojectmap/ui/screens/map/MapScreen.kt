package ru.tsu.mobileprojectmap.ui.screens.map

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ru.tsu.mobileprojectmap.R
import ru.tsu.mobileprojectmap.domain.algorithms.kmeans.KMeansFilterType
import ru.tsu.mobileprojectmap.domain.algorithms.kmeans.Cluster
import ru.tsu.mobileprojectmap.domain.model.Place
import ru.tsu.mobileprojectmap.domain.model.SamplePlaces
import ru.tsu.mobileprojectmap.ui.screens.map.components.CanvasMap
import ru.tsu.mobileprojectmap.ui.screens.map.components.PlaceDetailsBottomSheet
import ru.tsu.mobileprojectmap.ui.screens.map.model.MapEditMode

@SuppressLint("UnusedBoxWithConstraintsScope")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    onBack: () -> Unit,
    viewModel: MapViewModel = viewModel(),
    kMeansViewModel: KMeansViewModel = viewModel()
) {
    val uiState = viewModel.uiState
    val kMeansState = kMeansViewModel.uiState
    var showBottomSheet by remember { mutableStateOf(false) }
    var selectedCluster by remember { mutableStateOf<Cluster?>(null) }
    var selectedPlace by remember { mutableStateOf<Place?>(null) }

    selectedPlace?.let { place ->
        ModalBottomSheet(
            onDismissRequest = { selectedPlace = null },
        ) {
            PlaceDetailsBottomSheet(
                place = place,
                onClose = { selectedPlace = null }
            )
        }
    }

    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .navigationBarsPadding(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Управление картой",
                    style = MaterialTheme.typography.titleLarge
                )

                Button(
                    onClick = {
                        viewModel.setMode(MapEditMode.SET_START)
                        showBottomSheet = false
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Поставить старт")
                }

                Button(
                    onClick = {
                        viewModel.setMode(MapEditMode.SET_FINISH)
                        showBottomSheet = false
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Поставить финиш")
                }

                Button(
                    onClick = {
                        viewModel.setMode(MapEditMode.SET_OBSTACLE)
                        showBottomSheet = false
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Рисовать препятствия")
                }

                TextButton(
                    onClick = {
                        viewModel.setMode(MapEditMode.VIEW)
                        showBottomSheet = false
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Режим просмотра")
                }

                Button(
                    onClick = {
                        viewModel.findPath()
                        showBottomSheet = false
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Запустить A*")
                }

                Button(
                    onClick = {
                        kMeansViewModel.runKMeans(
                            k = 3,
                            filterType = kMeansState.filterType
                        )
                        showBottomSheet = false
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Запустить KMeans")
                }

                Text(
                    text = "Тип точек для кластеризации",
                    style = MaterialTheme.typography.titleMedium
                )

                Button(
                    onClick = {
                        kMeansViewModel.setFilterType(KMeansFilterType.CAFE_ONLY)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Только кафе")
                }

                Button(
                    onClick = {
                        kMeansViewModel.setFilterType(KMeansFilterType.COWORKING_ONLY)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Только коворкинги")
                }

                Button(
                    onClick = {
                        kMeansViewModel.setFilterType(KMeansFilterType.ALL)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Кафе + коворкинги")
                }

                TextButton(
                    onClick = {
                        kMeansViewModel.clearKMeans()
                        showBottomSheet = false
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Скрыть KMeans")
                }

                TextButton(
                    onClick = {
                        viewModel.resetMap()
                        showBottomSheet = false
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Сбросить карту")
                }
            }
        }
    }

    selectedCluster?.let { cluster ->
        ModalBottomSheet(
            onDismissRequest = { selectedCluster = null }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .navigationBarsPadding(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Кластер ${cluster.id + 1}",
                    style = MaterialTheme.typography.titleLarge
                )
                Text("Точек в кластере: ${cluster.points.size}")

                cluster.points.forEach { point ->
                    Text("• ${point.name ?: point.id}")
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Карта и маршрут") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { innerPadding ->
        val verticalScroll = rememberScrollState()
        val horizontalScroll = rememberScrollState()
        val mapWidth = 1300.dp
        val mapHeight = 946.dp
        var zoomScale by remember { mutableStateOf(1f) }
        var offsetX by remember { mutableStateOf(0f) }
        var offsetY by remember { mutableStateOf(0f) }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Card(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = when (uiState.currentMode) {
                            MapEditMode.VIEW -> "Режим: просмотр"
                            MapEditMode.SET_START -> "Режим: старт"
                            MapEditMode.SET_FINISH -> "Режим: финиш"
                            MapEditMode.SET_OBSTACLE -> "Режим: препятствия"
                        },
                        style = MaterialTheme.typography.labelLarge
                    )
                    Text(
                        text = uiState.statusMessage,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            kMeansState.result?.let { result ->
                Card(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Кластеры: ${result.clusters.size}",
                            style = MaterialTheme.typography.titleMedium
                        )

                        result.clusters.forEach { cluster ->
                            TextButton(
                                onClick = {
                                    selectedCluster = cluster
                                    selectedPlace = null
                                    showBottomSheet = false
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Кластер ${cluster.id + 1}: ${cluster.points.size} точек")
                            }
                        }
                    }
                }
            }

            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF5F5F5))
                    .clipToBounds()
                    .verticalScroll(
                        state = verticalScroll,
                        enabled = uiState.currentMode != MapEditMode.SET_OBSTACLE &&
                            (uiState.currentMode != MapEditMode.VIEW || zoomScale == 1f)
                    )
                    .horizontalScroll(
                        state = horizontalScroll,
                        enabled = uiState.currentMode != MapEditMode.SET_OBSTACLE &&
                            (uiState.currentMode != MapEditMode.VIEW || zoomScale == 1f)
                    )
            )
            {
                val density = LocalDensity.current
                val viewportWidthPx = with(density) { maxWidth.toPx() }
                val viewportHeightPx = with(density) { maxHeight.toPx() }
                val mapWidthPx = with(density) { mapWidth.toPx() }
                val mapHeightPx = with(density) { mapHeight.toPx() }

                Box(
                    modifier = Modifier
                        .graphicsLayer {
                            scaleX = zoomScale
                            scaleY = zoomScale
                            translationX = offsetX
                            translationY = offsetY
                        }
                        .pointerInput(uiState.currentMode) {
                            detectTransformGestures { _, pan, zoom, _ ->
                                if (uiState.currentMode != MapEditMode.VIEW) return@detectTransformGestures

                                val newScale = (zoomScale * zoom).coerceIn(1f, 4f)
                                val maxOffsetX = ((mapWidthPx * newScale) - viewportWidthPx).coerceAtLeast(0f) / 2f
                                val maxOffsetY = ((mapHeightPx * newScale) - viewportHeightPx).coerceAtLeast(0f) / 2f

                                zoomScale = newScale

                                if (newScale > 1f) {
                                    offsetX = (offsetX + pan.x).coerceIn(-maxOffsetX, maxOffsetX)
                                    offsetY = (offsetY + pan.y).coerceIn(-maxOffsetY, maxOffsetY)
                                } else {
                                    offsetX = 0f
                                    offsetY = 0f
                                }
                            }
                        }
                )
                {
                    Image(
                        painter = painterResource(id = R.drawable.campus_map),
                        contentDescription = "Карта кампуса",
                        modifier = Modifier.size(mapWidth, mapHeight),
                        contentScale = ContentScale.Fit
                    )

                    CanvasMap(
                        cells = uiState.cells,
                        path = uiState.path,
                        visitedCells = uiState.visitedCells,
                        currentCell = uiState.currentCell,
                        places = SamplePlaces.places,
                        currentMode = uiState.currentMode,
                        kMeansResult = kMeansState.result,
                        onCellClick = { row, col ->
                            viewModel.onCellClick(row, col)
                        },
                        onObstacleDrag = { row, col ->
                            viewModel.drawObstacleCell(row, col)
                        },
                        onClusterClick = { cluster ->
                            selectedCluster = cluster
                            selectedPlace = null
                            showBottomSheet = false
                        },
                        onPlaceClick = { place ->
                            // Клик по точке магазина/заведения открывает карточку заведения.
                            selectedPlace = place
                            selectedCluster = null
                            showBottomSheet = false
                        },
                        worldWidth = mapWidth,
                        worldHeight = mapHeight,
                        modifier = Modifier
                    )
                }
            }

            FloatingActionButton(
                onClick = {
                    // При открытии меню закрываем остальные карточки.
                    showBottomSheet = true
                    selectedPlace = null
                    selectedCluster = null
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
                    .navigationBarsPadding()
            ) {
                Icon(Icons.Default.Menu, contentDescription = "Меню")
            }
        }
    }
}
