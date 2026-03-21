package ru.tsu.mobileprojectmap.ui.screens.map.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.tsu.mobileprojectmap.ui.screens.map.model.MapCell

@Composable
fun GridMap(
    cells: List<List<MapCell>>,
    onCellClick: (row: Int, col: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(1.dp)

    ) {
        cells.forEach { rowList ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(1.dp)

            ) {
                rowList.forEach { cell ->
                    GridCell(
                        cellType = cell.type,
                        onClick = { onCellClick(cell.row, cell.col) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}
