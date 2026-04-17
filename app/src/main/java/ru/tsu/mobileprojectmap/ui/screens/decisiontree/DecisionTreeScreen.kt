package ru.tsu.mobileprojectmap.ui.screens.decisiontree

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import ru.tsu.mobileprojectmap.domain.algorithms.decisiontree.DecisionTreeNode
import ru.tsu.mobileprojectmap.domain.algorithms.decisiontree.DecisionTreeResult
import ru.tsu.mobileprojectmap.domain.algorithms.decisiontree.PredictionResult
import ru.tsu.mobileprojectmap.domain.usecase.BuildDecisionTreeUseCase
import ru.tsu.mobileprojectmap.domain.usecase.PredictPlaceUseCase

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DecisionTreeScreen(
    onBack: () -> Unit
) {
    val buildDecisionTreeUseCase = remember { BuildDecisionTreeUseCase() }
    val predictPlaceUseCase = remember { PredictPlaceUseCase() }

    var csvText by remember { mutableStateOf(DEFAULT_DECISION_TREE_CSV) }
    var treeResult by remember { mutableStateOf<DecisionTreeResult?>(null) }
    var predictionResult by remember { mutableStateOf<PredictionResult?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val featureInputs = remember {
        mutableStateMapOf<String, String>().apply {
            FEATURE_FIELDS.forEach { field -> put(field.name, "") }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Дерево решений") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
                .testTag("decision_tree_screen"),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Выбор места для обеда",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "Вставьте CSV-выборку, постройте дерево и проверьте полный путь принятия решения.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            OutlinedTextField(
                value = csvText,
                onValueChange = { csvText = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                label = { Text("CSV-данные") }
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        runCatching { buildDecisionTreeUseCase(csvText) }
                            .onSuccess { result ->
                                treeResult = result
                                predictionResult = null
                                errorMessage = null
                            }
                            .onFailure { error ->
                                treeResult = null
                                predictionResult = null
                                errorMessage = error.message ?: "Не удалось построить дерево."
                            }
                    },
                    modifier = Modifier.testTag("decision_tree_build")
                ) {
                    Text("Построить дерево")
                }

                Button(
                    onClick = {
                        csvText = DEFAULT_DECISION_TREE_CSV
                        predictionResult = null
                        errorMessage = null
                    }
                ) {
                    Text("Подставить пример")
                }
            }

            errorMessage?.let { message ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = message,
                        modifier = Modifier.padding(12.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }

            treeResult?.let { result ->
                Card {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Структура дерева",
                            style = MaterialTheme.typography.titleMedium
                        )
                        TreeNodeView(node = result.root, depth = 0, branchLabel = null)
                    }
                }

                Card {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Ввод признаков",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Заполните все поля, чтобы увидеть полный набор введённых данных и проход по дереву.",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        FEATURE_FIELDS.forEach { field ->
                            OutlinedTextField(
                                value = featureInputs[field.name].orEmpty(),
                                onValueChange = { featureInputs[field.name] = it },
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text(field.label) },
                                placeholder = { Text(field.hint) }
                            )
                        }

                        Button(
                            onClick = {
                                runCatching {
                                    predictPlaceUseCase(
                                        root = result.root,
                                        features = featureInputs.toMap()
                                    )
                                }.onSuccess { prediction ->
                                    predictionResult = prediction
                                    errorMessage = null
                                }.onFailure { error ->
                                    predictionResult = null
                                    errorMessage = error.message ?: "Не удалось получить прогноз."
                                }
                            }
                        ) {
                            Text("Получить рекомендацию")
                        }

                        predictionResult?.let { prediction ->
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Рекомендованное заведение: ${prediction.predictedLabel}",
                                style = MaterialTheme.typography.titleSmall
                            )

                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "Введённые признаки",
                                        style = MaterialTheme.typography.titleSmall
                                    )
                                    FEATURE_FIELDS.forEach { field ->
                                        Text(
                                            text = "${field.name}: ${featureInputs[field.name].orEmpty().ifBlank { "<пусто>" }}",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }
                            }

                            Text(
                                text = "Полный проход по дереву:",
                                style = MaterialTheme.typography.titleSmall
                            )
                            prediction.decisionPath.forEach { step ->
                                Text("• $step")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TreeNodeView(
    node: DecisionTreeNode,
    depth: Int,
    branchLabel: String?
) {
    val indent = "  ".repeat(depth)

    when (node) {
        is DecisionTreeNode.DecisionNode -> {
            Text(
                text = buildString {
                    append(indent)
                    branchLabel?.let { append("[$it] ") }
                    append("Признак: ${node.featureName}")
                }
            )

            node.branches.toSortedMap().forEach { (value, childNode) ->
                TreeNodeView(
                    node = childNode,
                    depth = depth + 1,
                    branchLabel = value
                )
            }
        }

        is DecisionTreeNode.LeafNode -> {
            Text(
                text = buildString {
                    append(indent)
                    branchLabel?.let { append("[$it] ") }
                    append("Результат: ${node.label}")
                }
            )
        }
    }
}

private data class FeatureField(
    val name: String,
    val label: String,
    val hint: String
)

private val FEATURE_FIELDS = listOf(
    FeatureField("location", "location", "main_building / second_building / bus_stop / campus_center"),
    FeatureField("budget", "budget", "low / medium / high"),
    FeatureField("time_available", "time_available", "very_short / short / medium"),
    FeatureField("food_type", "food_type", "coffee / pancakes / full_meal / snack"),
    FeatureField("queue_tolerance", "queue_tolerance", "low / medium / high"),
    FeatureField("weather", "weather", "good / bad")
)

private const val DEFAULT_DECISION_TREE_CSV = """
location,budget,time_available,food_type,queue_tolerance,weather,recommended_place
main_building,low,medium,full_meal,medium,good,Main_Cafeteria
main_building,low,short,snack,low,good,Yarche
main_building,medium,short,coffee,low,good,Bus_Stop_Coffee
main_building,high,medium,coffee,medium,good,Starbooks
second_building,low,very_short,snack,low,good,Vending_Machine
second_building,medium,short,coffee,medium,good,Second_Building_Cafe
second_building,medium,medium,full_meal,medium,good,Main_Cafeteria
second_building,low,short,snack,low,bad,Vending_Machine
campus_center,medium,short,pancakes,medium,good,Siberian_Pancakes
bus_stop,low,very_short,coffee,low,bad,Bus_Stop_Coffee
"""
