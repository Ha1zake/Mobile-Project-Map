package ru.tsu.mobileprojectmap.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
                title = {
                    Text("TSU Campus Assistant")
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "Главный экран",
                style = MaterialTheme.typography.headlineMedium
            )

            Text(
                text = "Здесь собраны алгоритмы модуля: A*, кластеризация, генетический, муравьиный, дерево решений и оценка заведений через распознавание цифры в отзывах.",
                style = MaterialTheme.typography.bodyLarge
            )

            AlgorithmCard(
                title = "A* и KMeans",
                description = "Маршруты по карте кампуса, препятствия и кластеризация точек.",
                onOpen = onOpenMap,
                buttonText = "Открыть карту"
            )

            AlgorithmCard(
                title = "Дерево решений",
                description = "Построение дерева по CSV и рекомендация заведения по введённым признакам.",
                onOpen = onOpenDecisionTree,
                buttonText = "Открыть дерево решений"
            )
        }
    }
}

@Composable
private fun AlgorithmCard(
    title: String,
    description: String,
    onOpen: () -> Unit,
    buttonText: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium
            )
            Button(onClick = onOpen) {
                Text(buttonText)
            }
        }
    }
}
