package ru.tsu.mobileprojectmap

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppNavigationUiTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun homeScreenShowsPrimaryActions() {
        composeTestRule.onNodeWithTag("home_screen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("home_open_map").assertIsDisplayed()
        composeTestRule.onNodeWithTag("home_open_tree").assertIsDisplayed()
    }

    @Test
    fun mapScreenShowsBottomAlgorithmPanel() {
        composeTestRule.onNodeWithTag("home_open_map").performClick()

        composeTestRule.onNodeWithTag("map_screen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("map_status_card").assertIsDisplayed()
        composeTestRule.onNodeWithTag("map_panel").assertIsDisplayed()
        composeTestRule.onNodeWithTag("map_run_astar").assertIsDisplayed()
    }

    @Test
    fun decisionTreeScreenOpensWithBuildAction() {
        composeTestRule.onNodeWithTag("home_open_tree").performClick()

        composeTestRule.onNodeWithTag("decision_tree_screen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("decision_tree_build").assertIsDisplayed()
    }
}
