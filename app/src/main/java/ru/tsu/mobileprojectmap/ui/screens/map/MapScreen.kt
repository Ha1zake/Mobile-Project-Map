package ru.tsu.mobileprojectmap.ui.screens.map

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ru.tsu.mobileprojectmap.R
import ru.tsu.mobileprojectmap.domain.algorithms.kmeans.Cluster
import ru.tsu.mobileprojectmap.domain.algorithms.kmeans.KMeansFilterType
import ru.tsu.mobileprojectmap.domain.model.FoodCategory
import ru.tsu.mobileprojectmap.domain.model.Place
import ru.tsu.mobileprojectmap.domain.model.SamplePlaces
import ru.tsu.mobileprojectmap.ui.screens.map.components.CanvasMap
import ru.tsu.mobileprojectmap.ui.screens.map.components.PlaceDetailsBottomSheet
import ru.tsu.mobileprojectmap.ui.screens.map.model.MapEditMode

private enum class MapPanelTab {
    ASTAR,
    CLUSTERS,
    GENETIC,
    ANT
}

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
    var activeTab by remember { mutableStateOf(MapPanelTab.ASTAR) }
    var zoom by remember { mutableFloatStateOf(1f) }
    val selectedLandmarkIds = remember {
        mutableStateListOf<String>().apply {
            addAll(SamplePlaces.landmarks.map { it.id })
        }
    }

    selectedPlace?.let { place ->
        ModalBottomSheet(onDismissRequest = { selectedPlace = null }) {
            PlaceDetailsBottomSheet(
                place = place,
                onClose = { selectedPlace = null }
            )
        }
    }

    selectedCluster?.let { cluster ->
        ModalBottomSheet(onDismissRequest = { selectedCluster = null }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .navigationBarsPadding(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Кластер ${cluster.id + 1}",
                    style = MaterialTheme.typography.titleLarge
                )
                Text("Точек в кластере: ${cluster.points.size}")
                Text("Состав кластера:")
                cluster.points.forEach { point ->
                    Text("• ${point.name ?: point.id}")
                }
            }
        }
    }

    if (showBottomSheet) {
        ModalBottomSheet(onDismissRequest = { showBottomSheet = false }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .navigationBarsPadding()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                SheetSection(title = "Товары для генетического алгоритма") {
                    Text(
                        text = "Выберите товары, которые нужно собрать:",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    FoodCategory.values().forEach { category ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = category in uiState.selectedMealCategories,
                                onCheckedChange = { checked ->
                                    viewModel.setMealCategorySelected(category, checked)
                                }
                            )
                            Text(category.label())
                        }
                    }
                }

                SheetSection(title = "Достопримечательности для муравьиного алгоритма") {
                    Text(
                        text = "Выберите точки обхода:",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    SamplePlaces.landmarks.forEach { place ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = place.id in selectedLandmarkIds,
                                onCheckedChange = { checked ->
                                    if (checked) {
                                        if (place.id !in selectedLandmarkIds) {
                                            selectedLandmarkIds.add(place.id)
                                        }
                                    } else {
                                        selectedLandmarkIds.remove(place.id)
                                    }
                                }
                            )
                            Text(place.name)
                        }
                    }
                }

                SheetSection(title = "Заведения и отзывы") {
                    Text(
                        text = "Откройте карточку заведения, чтобы посмотреть описание и отзывы.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    SamplePlaces.cafes.forEach { place ->
                        OutlinedButton(
                            onClick = {
                                selectedPlace = place
                                showBottomSheet = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(place.name)
                        }
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Карта кампуса ТГУ") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Назад"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFEAF4FF))
                .testTag("map_screen")
        ) {
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val aspectRatio = 1100f / 800f
                val viewportWidth = maxWidth
                val viewportHeight = maxHeight
                val baseWidth = if ((viewportWidth / aspectRatio) <= viewportHeight) {
                    viewportWidth
                } else {
                    viewportHeight * aspectRatio
                }
                val baseHeight = if ((viewportWidth / aspectRatio) <= viewportHeight) {
                    viewportWidth / aspectRatio
                } else {
                    viewportHeight
                }
                val mapWidth = baseWidth * zoom
                val mapHeight = baseHeight * zoom
                val contentWidth = if (mapWidth < viewportWidth) viewportWidth else mapWidth
                val contentHeight = if (mapHeight < viewportHeight) viewportHeight else mapHeight
                val verticalScroll = rememberScrollState()
                val horizontalScroll = rememberScrollState()

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(verticalScroll)
                        .horizontalScroll(horizontalScroll)
                ) {
                    Box(
                        modifier = Modifier.size(contentWidth, contentHeight)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(mapWidth, mapHeight)
                                .align(Alignment.Center)
                                .background(
                                    color = Color.White,
                                    shape = RoundedCornerShape(24.dp)
                                )
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.campus_map),
                                contentDescription = "Карта кампуса",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.FillBounds
                            )

                            CanvasMap(
                                cells = uiState.cells,
                                path = uiState.path,
                                visitedCells = uiState.visitedCells,
                                currentCell = uiState.currentCell,
                                places = SamplePlaces.places,
                                currentMode = uiState.currentMode,
                                kMeansResult = kMeansState.result,
                                allowPlaceTap = true,
                                showPlaceLabels = zoom >= 1.25f,
                                onCellClick = { row, col -> viewModel.onCellClick(row, col) },
                                onObstacleDrag = { row, col -> viewModel.drawObstacleCell(row, col) },
                                onClusterClick = { cluster ->
                                    selectedCluster = cluster
                                    selectedPlace = null
                                    showBottomSheet = false
                                },
                                onPlaceClick = { place ->
                                    selectedPlace = place
                                    selectedCluster = null
                                    showBottomSheet = false
                                },
                                worldWidth = mapWidth,
                                worldHeight = mapHeight
                            )
                        }
                    }
                }
            }

            StatusCard(
                mode = when (uiState.currentMode) {
                    MapEditMode.VIEW -> "Просмотр"
                    MapEditMode.SET_START -> "Старт"
                    MapEditMode.SET_FINISH -> "Финиш"
                    MapEditMode.SET_OBSTACLE -> "Ограждения"
                },
                status = uiState.statusMessage,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 12.dp, top = 12.dp)
                    .testTag("map_status_card")
            )

            SummaryColumn(
                kMeansSummary = kMeansState.result?.let {
                    "Кластеры: ${it.clusters.size}, итерации: ${it.iterations}"
                },
                geneticSummary = uiState.geneticSummary,
                antSummary = uiState.antSummary,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(start = 12.dp, end = 12.dp, bottom = 148.dp)
            )

            ZoomCard(
                zoom = zoom,
                onZoomOut = { zoom = (zoom - 0.25f).coerceIn(0.8f, 3.6f) },
                onZoomIn = { zoom = (zoom + 0.25f).coerceIn(0.8f, 3.6f) },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 12.dp, end = 12.dp)
            )

            AlgorithmChooserPanel(
                activeTab = activeTab,
                selectedGoods = uiState.selectedMealCategories,
                selectedLandmarksCount = selectedLandmarkIds.size,
                currentClusterFilter = kMeansState.filterType,
                onSelectTab = { activeTab = it },
                onSetStart = { viewModel.setMode(MapEditMode.SET_START) },
                onSetFinish = { viewModel.setMode(MapEditMode.SET_FINISH) },
                onRunAStar = { viewModel.findPath() },
                onSetObstacles = { viewModel.setMode(MapEditMode.SET_OBSTACLE) },
                onResetMap = { viewModel.resetMap() },
                onRunKMeans = { kMeansViewModel.runKMeans(3, kMeansState.filterType) },
                onClearKMeans = { kMeansViewModel.clearKMeans() },
                onSelectKMeansFilter = { filter -> kMeansViewModel.setFilterType(filter) },
                onRunGenetic = { viewModel.runGeneticMealRoute() },
                onRunAnt = { viewModel.runAntLandmarksRoute(selectedLandmarkIds.toSet()) },
                onOpenSettings = { showBottomSheet = true },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(start = 12.dp, end = 12.dp, bottom = 16.dp)
            )

            if (uiState.isMapLoading || uiState.isRunning || kMeansState.isRunning) {
                Card(
                    modifier = Modifier.align(Alignment.Center),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        CircularProgressIndicator()
                        Text(
                            text = if (uiState.isMapLoading) {
                                "Подготавливаем карту..."
                            } else {
                                "Алгоритм выполняется..."
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusCard(
    mode: String,
    status: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.widthIn(max = 360.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)
        )
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = mode,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = status,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun ZoomCard(
    zoom: Float,
    onZoomOut: () -> Unit,
    onZoomIn: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f)
        )
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "Масштаб ${"%.1f".format(zoom)}x",
                style = MaterialTheme.typography.bodySmall
            )
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(onClick = onZoomOut) {
                    Text("−")
                }
                IconButton(onClick = onZoomIn) {
                    Icon(Icons.Default.Add, contentDescription = "Увеличить")
                }
            }
        }
    }
}

@Composable
private fun SummaryColumn(
    kMeansSummary: String?,
    geneticSummary: String?,
    antSummary: String?,
    modifier: Modifier = Modifier
) {
    if (kMeansSummary == null && geneticSummary == null && antSummary == null) return

    Column(
        modifier = modifier.widthIn(max = 640.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        kMeansSummary?.let { SummaryCard(title = "Кластеризация", value = it) }
        geneticSummary?.let { SummaryCard(title = "Генетический алгоритм", value = it) }
        antSummary?.let { SummaryCard(title = "Муравьиный алгоритм", value = it) }
    }
}

@Composable
private fun SummaryCard(
    title: String,
    value: String
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f)
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(title, style = MaterialTheme.typography.titleSmall)
            Text(value, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun AlgorithmChooserPanel(
    activeTab: MapPanelTab,
    selectedGoods: Set<FoodCategory>,
    selectedLandmarksCount: Int,
    currentClusterFilter: KMeansFilterType,
    onSelectTab: (MapPanelTab) -> Unit,
    onSetStart: () -> Unit,
    onSetFinish: () -> Unit,
    onRunAStar: () -> Unit,
    onSetObstacles: () -> Unit,
    onResetMap: () -> Unit,
    onRunKMeans: () -> Unit,
    onClearKMeans: () -> Unit,
    onSelectKMeansFilter: (KMeansFilterType) -> Unit,
    onRunGenetic: () -> Unit,
    onRunAnt: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .widthIn(max = 860.dp)
            .navigationBarsPadding()
            .testTag("map_panel"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.97f)
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PanelChip("A*", activeTab == MapPanelTab.ASTAR) { onSelectTab(MapPanelTab.ASTAR) }
                PanelChip("Кластеры", activeTab == MapPanelTab.CLUSTERS) { onSelectTab(MapPanelTab.CLUSTERS) }
                PanelChip("Генетический", activeTab == MapPanelTab.GENETIC) { onSelectTab(MapPanelTab.GENETIC) }
                PanelChip("Муравьиный", activeTab == MapPanelTab.ANT) { onSelectTab(MapPanelTab.ANT) }
            }

            when (activeTab) {
                MapPanelTab.ASTAR -> {
                    Text(
                        text = "Поставьте старт и финиш на карте, затем запустите A*.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(onClick = onSetStart, modifier = Modifier.weight(1f)) {
                            Text("Старт")
                        }
                        Button(onClick = onSetFinish, modifier = Modifier.weight(1f)) {
                            Text("Финиш")
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(onClick = onSetObstacles, modifier = Modifier.weight(1f)) {
                            Text("Ограждения")
                        }
                        OutlinedButton(onClick = onResetMap, modifier = Modifier.weight(1f)) {
                            Text("Сброс")
                        }
                        Button(
                            onClick = onRunAStar,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("map_run_astar")
                        ) {
                            Text("Запустить A*")
                        }
                    }
                }

                MapPanelTab.CLUSTERS -> {
                    Text(
                        text = "Запустите кластеризацию и нажмите на круг кластера для подробностей.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = currentClusterFilter == KMeansFilterType.CAFE_ONLY,
                            onClick = { onSelectKMeansFilter(KMeansFilterType.CAFE_ONLY) },
                            label = { Text("Кафе") }
                        )
                        FilterChip(
                            selected = currentClusterFilter == KMeansFilterType.COWORKING_ONLY,
                            onClick = { onSelectKMeansFilter(KMeansFilterType.COWORKING_ONLY) },
                            label = { Text("Коворкинги") }
                        )
                        FilterChip(
                            selected = currentClusterFilter == KMeansFilterType.ALL,
                            onClick = { onSelectKMeansFilter(KMeansFilterType.ALL) },
                            label = { Text("Все точки") }
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(onClick = onRunKMeans, modifier = Modifier.weight(1f)) {
                            Text("Запустить")
                        }
                        OutlinedButton(onClick = onClearKMeans, modifier = Modifier.weight(1f)) {
                            Text("Очистить")
                        }
                        OutlinedButton(onClick = onOpenSettings, modifier = Modifier.weight(1f)) {
                            Text("Заведения")
                        }
                    }
                }

                MapPanelTab.GENETIC -> {
                    Text(
                        text = if (selectedGoods.isEmpty()) {
                            "Товары ещё не выбраны."
                        } else {
                            "Выбрано: ${selectedGoods.joinToString { it.label() }}"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(onClick = onOpenSettings, modifier = Modifier.weight(1f)) {
                            Text("Товары и кафе")
                        }
                        Button(
                            onClick = onRunGenetic,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("map_run_genetic")
                        ) {
                            Text("Запустить")
                        }
                    }
                }

                MapPanelTab.ANT -> {
                    Text(
                        text = "Выбрано достопримечательностей: $selectedLandmarksCount",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(onClick = onOpenSettings, modifier = Modifier.weight(1f)) {
                            Text("Точки маршрута")
                        }
                        Button(
                            onClick = onRunAnt,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("map_run_ant")
                        ) {
                            Text("Запустить")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RowScope.PanelChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) }
    )
}

@Composable
private fun SheetSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium
        )
        content()
    }
}

private fun FoodCategory.label(): String {
    return when (this) {
        FoodCategory.COFFEE -> "Кофе"
        FoodCategory.PANCAKES -> "Блины"
        FoodCategory.FULL_MEAL -> "Полный обед"
        FoodCategory.SNACK -> "Перекус"
        FoodCategory.DISPOSABLE_TABLEWARE -> "Одноразовая посуда"
    }
}
