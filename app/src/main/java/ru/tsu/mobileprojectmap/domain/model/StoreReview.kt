package ru.tsu.mobileprojectmap.domain.model

data class StoreReview(
    val placeId: String,
    val rating: Int,
    val description: String,
    val createdAtEpochMillis: Long = System.currentTimeMillis()
)

