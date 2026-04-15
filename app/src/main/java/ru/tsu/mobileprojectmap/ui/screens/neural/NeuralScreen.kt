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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun NeuralScreen() {
    val gridSize = 5
    val cells = remember {
        mutableStateListOf<Boolean>().apply {
            repeat(gridSize * gridSize) { add(false) }
        }
    }

    var prediction by remember { mutableStateOf<Int?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Распознавание цифры",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Нарисуйте цифру от 0 до 9 на сетке 5×5",
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

/**
 * Пока это заглушка, чтобы экран уже работал.
 */
private fun recognizeDigit(cells: List<Boolean>): Int {
    val filledCount = cells.count { it }

    return when {
        filledCount == 0 -> -1
        filledCount <= 3 -> 1
        filledCount <= 6 -> 7
        filledCount <= 9 -> 4
        filledCount <= 12 -> 2
        filledCount <= 15 -> 3
        else -> 8
    }
}