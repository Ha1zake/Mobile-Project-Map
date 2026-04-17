package ru.tsu.mobileprojectmap.ui.screens.decisiontree

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
    val featureInputs = remember { mutableStateMapOf<String, String>() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Дерево решений") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Вставьте CSV-выборку, постройте дерево и проверьте прогноз для нового набора признаков.",
                style = MaterialTheme.typography.bodyLarge
            )

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
                        runCatching {
                            buildDecisionTreeUseCase(csvText)
                        }.onSuccess { result ->
                            treeResult = result
                            predictionResult = null
                            errorMessage = null
                            featureInputs.clear()
                            extractFeatureNames(result.root).forEach { feature ->
                                featureInputs[feature] = ""
                            }
                        }.onFailure { error ->
                            treeResult = null
                            predictionResult = null
                            errorMessage = error.message ?: "Не удалось построить дерево"
                        }
                    }
                ) {
                    Text("Построить дерево")
                }

                Button(
                    onClick = {
                        csvText = DEFAULT_DECISION_TREE_CSV
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
                            text = "Проверка нового сценария",
                            style = MaterialTheme.typography.titleMedium
                        )

                        featureInputs.keys.sorted().forEach { featureName ->
                            OutlinedTextField(
                                value = featureInputs[featureName].orEmpty(),
                                onValueChange = { featureInputs[featureName] = it },
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text(featureName) }
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
                                    errorMessage = error.message ?: "Не удалось получить прогноз"
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
                            Text("Путь по дереву:")
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
                    branchLabel?.let {
                        append("[$it] ")
                    }
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
                    branchLabel?.let {
                        append("[$it] ")
                    }
                    append("Результат: ${node.label}")
                }
            )
        }
    }
}

private fun extractFeatureNames(node: DecisionTreeNode): Set<String> {
    return when (node) {
        is DecisionTreeNode.LeafNode -> emptySet()
        is DecisionTreeNode.DecisionNode -> {
            setOf(node.featureName) + node.branches.values.flatMap { child ->
                extractFeatureNames(child)
            }
        }
    }
}

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
