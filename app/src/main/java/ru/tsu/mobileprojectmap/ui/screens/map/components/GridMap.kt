package ru.tsu.mobileprojectmap.ui.screens.map.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun GridMap(
    rows: Int,
    cols: Int,
    selectedCells: Set<Pair<Int, Int>>,
    onCellClick: (row: Int, col: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(2.dp)

    ) {
        for (row in 0 until rows) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(2.dp)

            ) {
                for (col in 0 until cols) {
                    GridCell(
                        isSelected = selectedCells.contains(row to col),
                        onClick = { onCellClick(row, col) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}
