package ru.tsu.mobileprojectmap.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onOpenMap: () -> Unit,
    onOpenDecisionTree: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Навигация ТГУ") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.background,
                            Color(0xFFDCEEFF)
                        )
                    )
                )
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
                .testTag("home_screen"),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF1E6CBD),
                                Color(0xFF77B4F0)
                            )
                        ),
                        shape = RoundedCornerShape(28.dp)
                    )
                    .padding(24.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Кампус ТГУ в одном приложении",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White
                    )
                    Text(
                        text = "Навигация по роще, выбор заведений, коворкингов и визуализация алгоритмов для готовой защиты проекта.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White
                    )
                }
            }

            FeatureCard(
                title = "Карта и алгоритмы",
                description = "A*, кластеризация, генетический и муравьиный алгоритмы работают на карте кампуса и помогают строить понятные маршруты.",
                buttonText = "Открыть карту",
                buttonTag = "home_open_map",
                onOpen = onOpenMap
            )

            FeatureCard(
                title = "Дерево решений",
                description = "Загрузите CSV, постройте дерево решений и посмотрите полный набор введённых признаков и весь проход по дереву.",
                buttonText = "Открыть дерево решений",
                buttonTag = "home_open_tree",
                onOpen = onOpenDecisionTree
            )
        }
    }
}

@Composable
private fun FeatureCard(
    title: String,
    description: String,
    buttonText: String,
    buttonTag: String,
    onOpen: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium
            )
            Button(
                onClick = onOpen,
                modifier = Modifier.testTag(buttonTag)
            ) {
                Text(buttonText)
            }
        }
    }
}
