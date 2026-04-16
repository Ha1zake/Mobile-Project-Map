package ru.tsu.mobileprojectmap.ui.screens.neural

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ru.tsu.mobileprojectmap.domain.algorithms.neural.DigitRecognizer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NeuralScreen(
    onBack: () -> Unit
) {
    val gridSize = 5
    val cells = remember {
        mutableStateListOf<Boolean>().apply {
            repeat(gridSize * gridSize) { add(false) }
        }
    }

    var prediction by remember { mutableStateOf<Int?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Нейросеть") },
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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Распознавание цифры",
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Нарисуйте цифру от 0 до 9 на сетке 5x5",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier.padding(8.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    for (row in 0 until gridSize) {
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            for (col in 0 until gridSize) {
                                val index = row * gridSize + col
                                PixelCell(
                                    filled = cells[index],
                                    onClick = {
                                        cells[index] = !cells[index]
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally)
            ) {
                Button(
                    onClick = {
                        for (i in cells.indices) {
                            cells[i] = false
                        }
                        prediction = null
                    }
                ) {
                    Text("Очистить")
                }

                Button(
                    onClick = {
                        prediction = recognizeDigit(cells.toList())
                    },
                    colors = ButtonDefaults.buttonColors()
                ) {
                    Text("Распознать")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = if (prediction != null) {
                    "Результат: $prediction"
                } else {
                    "Результат: —"
                },
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

@Composable
private fun PixelCell(
    filled: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .border(1.dp, Color.Gray, RoundedCornerShape(6.dp))
            .background(
                color = if (filled) Color.Black else Color.White,
                shape = RoundedCornerShape(6.dp)
            )
            .clickable(onClick = onClick)
    )
}

private fun recognizeDigit(cells: List<Boolean>): Int? {
    return DigitRecognizer.recognizeDigit(cells, gridSize = 5)
}
