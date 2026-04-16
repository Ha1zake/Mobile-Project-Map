package ru.tsu.mobileprojectmap.data.reviews

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import ru.tsu.mobileprojectmap.domain.model.StoreReview

class StoreReviewsStorage(
    context: Context
) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun loadReviews(placeId: String): List<StoreReview> {
        val key = reviewsKey(placeId)
        val raw = prefs.getString(key, null) ?: return emptyList()

        return try {
            val array = JSONArray(raw)
            List(array.length()) { idx ->
                val obj = array.getJSONObject(idx)
                StoreReview(
                    placeId = placeId,
                    rating = obj.optInt("rating", 0),
                    description = obj.optString("description", ""),
                    createdAtEpochMillis = obj.optLong("createdAt", System.currentTimeMillis())
                )
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun addReview(placeId: String, review: StoreReview) {
        val current = loadReviews(placeId).toMutableList()
        current.add(review)
        saveAll(placeId, current)
    }

    private fun saveAll(placeId: String, reviews: List<StoreReview>) {
        val array = JSONArray()
        reviews.forEach { review ->
            val obj = JSONObject().apply {
                put("rating", review.rating)
                put("description", review.description)
                put("createdAt", review.createdAtEpochMillis)
            }
            array.put(obj)
        }
        prefs.edit()
            .putString(reviewsKey(placeId), array.toString())
            .apply()
    }

    private fun reviewsKey(placeId: String): String = "reviews_$placeId"

    private companion object {
        const val PREFS_NAME = "store_reviews"
    }
}

