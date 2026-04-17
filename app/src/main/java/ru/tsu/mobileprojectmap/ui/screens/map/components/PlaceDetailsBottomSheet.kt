package ru.tsu.mobileprojectmap.ui.screens.map.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ru.tsu.mobileprojectmap.domain.algorithms.neural.DigitRecognizer
import ru.tsu.mobileprojectmap.domain.model.Place
import ru.tsu.mobileprojectmap.domain.model.PlaceType
import ru.tsu.mobileprojectmap.domain.model.StoreReview
import ru.tsu.mobileprojectmap.ui.screens.map.PlaceReviewsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaceDetailsBottomSheet(
    place: Place,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val placeReviewsViewModel: PlaceReviewsViewModel = viewModel()
    val reviewsState = placeReviewsViewModel.uiState
    var selectedTabIndex by remember(place.id) { mutableStateOf(0) }

    LaunchedEffect(place.id) {
        placeReviewsViewModel.setPlace(place.id)
    }


    var isAddingReview by remember(place.id) { mutableStateOf(false) }
    var formError by remember(place.id) { mutableStateOf<String?>(null) }
    var reviewText by remember(place.id) { mutableStateOf("") }
    var rating by remember(place.id) { mutableStateOf<Int?>(null) }

    val gridSize = 5
    val cells = remember(place.id, isAddingReview) {
        mutableStateListOf<Boolean>().apply {
            repeat(gridSize * gridSize) { add(false) }
        }
    }

    fun resetForm() {
        formError = null
        reviewText = ""
        rating = null
        for (i in cells.indices) cells[i] = false
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 20.dp),
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 0.dp, end = 0.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Закрыть"
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 34.dp)
            ) {
                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 0.dp)
                ) {
                    Tab(
                        selected = selectedTabIndex == 0,
                        onClick = { selectedTabIndex = 0 },
                        text = { Text("Инфо") }
                    )
                    Tab(
                        selected = selectedTabIndex == 1,
                        onClick = { selectedTabIndex = 1 },
                        text = { Text("Отзывы") }
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = place.name,
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Часы работы: ${place.openHour}:00 - ${place.closeHour}:00",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (selectedTabIndex) {
            0 -> {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (place.description.isNotBlank()) {
                        Text(
                            text = place.description,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    if (place.type == PlaceType.CAFE) {
                        val itemsText = place.menuItems
                            .filter { it.isNotBlank() }
                            .joinToString(", ")
                        if (itemsText.isNotBlank()) {
                            Text(
                                text = "Есть в меню: $itemsText",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                }
            }

            1 -> {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (placeReviewsViewModel.uiState.error != null) {
                        Text(
                            text = placeReviewsViewModel.uiState.error ?: "",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    if (reviewsState.reviews.isEmpty()) {
                        Text(
                            text = "Пока нет отзывов.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    } else {
                        val avg = reviewsState.reviews.map { it.rating }.average()
                        Text(
                            text = "Средняя оценка: %.2f / 9".format(avg),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    reviewsState.reviews.take(10).forEach { review ->
                        ReviewCard(review = review)
                    }

                    if (place.type != PlaceType.CAFE) {
                        Text(
                            text = "Оставлять отзывы можно только для кафе/магазинов.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    } else {
                        if (!isAddingReview) {
                            TextButton(
                                onClick = {
                                    isAddingReview = true
                                    resetForm()
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Оставить отзыв")
                            }
                        } else {
                            // Форма отзыва
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = "Оценка (рисунок цифры 0..9 на 5x5):",
                                    style = MaterialTheme.typography.bodyMedium
                                )

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
                                                        onClick = { cells[index] = !cells[index] }
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    TextButton(onClick = {
                                        resetForm()
                                    }) {
                                        Text("Очистить")
                                    }

                                    TextButton(
                                        onClick = {
                                            formError = null
                                            rating = DigitRecognizer.recognizeDigit(cells.toList(), gridSize = gridSize)
                                            if (rating == null) {
                                                formError = "Не удалось распознать цифру. Попробуйте ещё раз."
                                            }
                                        }
                                    ) {
                                        Text("Распознать")
                                    }
                                }

                                Text(
                                    text = "Рейтинг: ${rating?.toString() ?: "—"}",
                                    style = MaterialTheme.typography.bodyMedium
                                )

                                OutlinedTextField(
                                    value = reviewText,
                                    onValueChange = { reviewText = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    label = { Text("Описание отзыва") },
                                    minLines = 2,
                                    maxLines = 4
                                )

                                formError?.let { err ->
                                    Text(
                                        text = err,
                                        color = MaterialTheme.colorScheme.error,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    TextButton(
                                        onClick = {
                                            isAddingReview = false
                                            resetForm()
                                        }
                                    ) {
                                        Text("Отмена")
                                    }
                                    TextButton(
                                        onClick = {
                                            formError = null
                                            val r = rating
                                            if (r == null) {
                                                formError = "Сначала распознайте рейтинг (0..9)."
                                                return@TextButton
                                            }
                                            if (reviewText.isBlank()) {
                                                formError = "Введите описание отзыва."
                                                return@TextButton
                                            }
                                            placeReviewsViewModel.addReview(
                                                placeId = place.id,
                                                rating = r,
                                                description = reviewText
                                            )
                                            isAddingReview = false
                                            resetForm()
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Сохранить")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReviewCard(review: StoreReview) {
    Card(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "Оценка: ${review.rating} / 9",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = review.description,
                style = MaterialTheme.typography.bodySmall
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
            .size(34.dp)
            .border(1.dp, Color.Gray, RoundedCornerShape(6.dp))
            .background(
                color = if (filled) Color.Black else Color.White,
                shape = RoundedCornerShape(6.dp)
            )
            .clickable(onClick = onClick)
    )
}

