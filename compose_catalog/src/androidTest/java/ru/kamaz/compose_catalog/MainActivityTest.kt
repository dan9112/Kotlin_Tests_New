package ru.kamaz.compose_catalog

import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import ru.kamaz.compose_catalog.MainActivity.Companion.DrawerAppComponent
import ru.kamaz.compose_catalog.views.screens.KamAZ6282Screen

@RunWith(AndroidJUnit4::class)
class MainActivityTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun checkChangeDrawerContentAfterChangeDrawerState() = with(receiver = composeTestRule) {
        // Given
        val viewModel = MainViewModel()
        setContent {
            DrawerAppComponent(viewModel = viewModel)
        }
        val item = KamAZ6282Screen

        // When
        onNode(matcher = hasContentDescription(value = "Menu")).performClick()
        waitForIdle()
        onNode(matcher = hasTestTag(testTag = item.toString())).performClick()

        // Then
        assertThat(viewModel.currentScreen.value).isEqualTo(item)
    }
}
