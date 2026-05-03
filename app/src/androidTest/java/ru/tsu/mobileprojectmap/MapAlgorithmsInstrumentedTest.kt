package ru.tsu.mobileprojectmap

import android.app.Application
import android.os.SystemClock
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertFalse
import org.junit.Test
import org.junit.runner.RunWith
import ru.tsu.mobileprojectmap.ui.screens.map.MapViewModel
import ru.tsu.mobileprojectmap.ui.screens.map.model.MapEditMode

@RunWith(AndroidJUnit4::class)
class MapAlgorithmsInstrumentedTest {

    @Test
    fun aStarBuildsRouteOnCampusMap() {
        val viewModel = createViewModel()
        waitUntil { !viewModel.uiState.isMapLoading }

        viewModel.setMode(MapEditMode.SET_START)
        viewModel.onCellClick(72, 78)
        viewModel.setMode(MapEditMode.SET_FINISH)
        viewModel.onCellClick(114, 102)
        viewModel.findPath()

        waitUntil { !viewModel.uiState.isRunning && viewModel.uiState.pathFound != null }
        assertFalse(viewModel.uiState.path.isEmpty())
    }

    @Test
    fun geneticAlgorithmBuildsDisplayedRoute() {
        val viewModel = createViewModel()
        waitUntil { !viewModel.uiState.isMapLoading }

        viewModel.runGeneticMealRoute()

        waitUntil { !viewModel.uiState.isRunning && viewModel.uiState.geneticSummary != null }
        assertFalse(viewModel.uiState.path.isEmpty())
    }

    @Test
    fun antAlgorithmBuildsDisplayedRoute() {
        val viewModel = createViewModel()
        waitUntil { !viewModel.uiState.isMapLoading }

        viewModel.runAntLandmarksRoute(
            selectedLandmarkIds = setOf(
                "main_building",
                "science_library",
                "university_grove",
                "main_gate"
            )
        )

        waitUntil { !viewModel.uiState.isRunning && viewModel.uiState.antSummary != null }
        assertFalse(viewModel.uiState.path.isEmpty())
    }

    private fun createViewModel(): MapViewModel {
        val application = InstrumentationRegistry
            .getInstrumentation()
            .targetContext
            .applicationContext as Application
        return MapViewModel(application)
    }

    private fun waitUntil(
        timeoutMs: Long = 20_000,
        condition: () -> Boolean
    ) {
        val startedAt = SystemClock.elapsedRealtime()
        while (SystemClock.elapsedRealtime() - startedAt < timeoutMs) {
            if (condition()) return
            SystemClock.sleep(100)
        }
        throw AssertionError("Timeout waiting for condition")
    }
}
