package ru.tsu.mobileprojectmap.ui.screens.map

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import ru.tsu.mobileprojectmap.data.reviews.StoreReviewsStorage
import ru.tsu.mobileprojectmap.domain.model.StoreReview

data class PlaceReviewsUiState(
    val placeId: String? = null,
    val reviews: List<StoreReview> = emptyList(),
    val error: String? = null
)

class PlaceReviewsViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val storage = StoreReviewsStorage(application)

    var uiState by mutableStateOf(PlaceReviewsUiState())
        private set

    fun setPlace(placeId: String) {
        if (uiState.placeId == placeId) return

        uiState = uiState.copy(
            placeId = placeId,
            reviews = storage.loadReviews(placeId),
            error = null
        )
    }

    fun addReview(placeId: String, rating: Int, description: String) {
        try {
            require(rating in 0..9) { "Рейтинг должен быть в диапазоне 0..9" }
            require(description.isNotBlank()) { "Описание не может быть пустым" }

            val review = StoreReview(
                placeId = placeId,
                rating = rating,
                description = description.trim()
            )

            storage.addReview(placeId, review)
            uiState = uiState.copy(
                reviews = storage.loadReviews(placeId),
                error = null
            )
        } catch (e: Exception) {
            uiState = uiState.copy(error = e.message ?: "Не удалось сохранить отзыв")
        }
    }
}

