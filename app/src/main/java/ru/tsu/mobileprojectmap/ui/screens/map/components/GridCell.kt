package ru.tsu.mobileprojectmap.ui.screens.map.components
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ru.tsu.mobileprojectmap.ui.screens.map.model.CellType

@Composable
fun GridCell(
    cellType: CellType,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val color = when (cellType){
        CellType.EMPTY -> Color(0xFFE0E0E0)
        CellType.START -> Color(0xFF4CAF50)
        CellType.FINISH -> Color(0xFFF44336)
        CellType.OBSTACLE -> Color(0xFF424242)
    }
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .background(color)
            .clickable(onClick = onClick)
    )
}
