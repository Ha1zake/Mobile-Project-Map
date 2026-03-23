package ru.tsu.mobileprojectmap.ui.screens.map
import ru.tsu.mobileprojectmap.ui.screens.map.components.GridMap
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Button
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
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ru.tsu.mobileprojectmap.ui.screens.map.model.MapEditMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    onBack: () -> Unit,
    viewModel: MapViewModel = viewModel()
) {

    val uiState = viewModel.uiState
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
                    onClick = { viewModel.setMode(MapEditMode.SET_START)
                        showBottomSheet = false},
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Поставить старт")
                }

                Button(
                    onClick = {viewModel.setMode(MapEditMode.SET_FINISH)
                        showBottomSheet = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Поставить финиш")
                }

                Button(
                    onClick = { viewModel.setMode(MapEditMode.SET_OBSTACLE)
                        showBottomSheet = false},
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Добавить препятствие")
                }

                Button(
                    onClick = { },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Запустить A*")
                }

                TextButton(
                    onClick = {viewModel.resetMap() },
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
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Назад"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { innerPadding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
                    .background(Color(0xFFF5F5F5))
                    .padding(8.dp),
                contentAlignment = Alignment.Center

            ) {
                GridMap(
                    cells = uiState.cells,
                    onCellClick = {row,col ->
                        viewModel.onCellClick(row,col)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                )
            }


            FloatingActionButton(
                onClick = { showBottomSheet = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 10.dp)
                    .padding(bottom = 24.dp)
                    .navigationBarsPadding()
                    .size(56.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Открыть меню"
                )
            }
        }
    }
}