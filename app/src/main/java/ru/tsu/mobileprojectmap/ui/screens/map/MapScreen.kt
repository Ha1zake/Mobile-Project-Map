package ru.tsu.mobileprojectmap.ui.screens.map
import ru.tsu.mobileprojectmap.domain.model.SamplePlaces
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ru.tsu.mobileprojectmap.R
import ru.tsu.mobileprojectmap.domain.algorithms.kmeans.KMeansFilterType
import ru.tsu.mobileprojectmap.ui.screens.map.components.CanvasMap
import ru.tsu.mobileprojectmap.ui.screens.map.model.MapEditMode

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
                    Text("Добавить препятствие")
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
                    onClick = { viewModel.resetMap() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Сбросить карту")
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

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            val mapWidth = 1300.dp
            val mapHeight = 946.dp

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF5F5F5))
                    .verticalScroll(verticalScroll)
                    .horizontalScroll(horizontalScroll)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.campus_map),
                    contentDescription = "Карта",
                    modifier = Modifier.size(mapWidth, mapHeight),
                    contentScale = ContentScale.Fit
                )

                CanvasMap(
                    cells = uiState.cells,
                    path = uiState.path,
                    visitedCells = uiState.visitedCells,
                    kMeansResult = kMeansState.result,
                    currentCell = uiState.currentCell,
                    places = SamplePlaces.places,
                    onCellClick = { row, col ->
                        viewModel.onCellClick(row, col)
                    },
                    worldWidth = mapWidth,
                    worldHeight = mapHeight,
                    modifier = Modifier
                )
            }

            FloatingActionButton(
                onClick = { showBottomSheet = true },
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