package ru.tsu.mobileprojectmap.ui.screens.map


import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import ru.tsu.mobileprojectmap.domain.algorithms.kmeans.ClusterPoint
import ru.tsu.mobileprojectmap.domain.algorithms.kmeans.KMeansFilterType
import ru.tsu.mobileprojectmap.domain.algorithms.kmeans.KMeansInput
import ru.tsu.mobileprojectmap.domain.algorithms.kmeans.KMeansResult
import ru.tsu.mobileprojectmap.domain.model.PlaceType
import ru.tsu.mobileprojectmap.domain.model.SamplePlaces
import ru.tsu.mobileprojectmap.domain.usecase.RunKMeansUseCase
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class KMeansUiState(
    val result: KMeansResult? = null,
    val isRunning: Boolean = false,
    val error: String? = null,
    val filterType: KMeansFilterType = KMeansFilterType.CAFE_ONLY
)


class KMeansViewModel : ViewModel() {

    private val runKMeansUseCase = RunKMeansUseCase()

    var uiState by mutableStateOf(KMeansUiState())
        private set

    fun runKMeans(
        k: Int = 3,
        filterType: KMeansFilterType = uiState.filterType
    ) {
        uiState = uiState.copy(
            isRunning = true,
            error = null,
            filterType = filterType
        )

        viewModelScope.launch(Dispatchers.Default) {
            val filteredPlaces = when (filterType) {
                KMeansFilterType.CAFE_ONLY ->
                    SamplePlaces.places.filter { it.type == PlaceType.CAFE }

                KMeansFilterType.COWORKING_ONLY ->
                    SamplePlaces.places.filter { it.type == PlaceType.COWORKING }

                KMeansFilterType.ALL ->
                    SamplePlaces.places.filter {
                        it.type == PlaceType.CAFE || it.type == PlaceType.COWORKING
                    }
            }

            val points = filteredPlaces.map { place ->
                ClusterPoint(
                    id = place.id,
                    x = place.point.x.toDouble(),
                    y = place.point.y.toDouble(),
                    name = place.name
                )
            }

            val safeK = minOf(k, points.size.coerceAtLeast(1))

            val result = runKMeansUseCase(
                KMeansInput(
                    points = points,
                    k = safeK
                )
            )

            withContext(Dispatchers.Main) {
                uiState = result.fold(
                    onSuccess = {
                        uiState.copy(
                            result = it,
                            isRunning = false,
                            error = null,
                            filterType = filterType
                        )
                    },
                    onFailure = {
                        uiState.copy(
                            result = null,
                            isRunning = false,
                            error = it.message,
                            filterType = filterType
                        )
                    }
                )
            }
        }
    }

    fun clearKMeans() {
        uiState = uiState.copy(result = null, error = null, isRunning = false)
    }
    fun setFilterType(filterType: KMeansFilterType) {
        uiState = uiState.copy(filterType = filterType)
    }
}
