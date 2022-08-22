package ru.kamaz.compose_catalog.views.screens

import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DrawerAppScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun carScreenExistsAllItems(): Unit = with(receiver = composeTestRule) {
        with(receiver = KamAZ6282Screen) {
            // Given
            setContent {
                GetView {}
            }

            // ???
            onNode(matcher = hasContentDescription(value = "Menu")).assertExists(errorMessageOnFail = "Menu has not found")
            onNode(matcher = hasTestTag(testTag = "Content text")).assertExists(errorMessageOnFail = "Text has not found")
            onNode(matcher = hasContentDescription(value = "Image")).assertExists(errorMessageOnFail = "Image has not found")
        }
    }

    @Test
    fun startScreenExistsAllItems(): Unit = with(receiver = composeTestRule) {
        with(receiver = StartScreen) {
            // Given
            setContent {
                GetView {}
            }

            // ???
            onNode(matcher = hasContentDescription(value = "Menu")).assertExists(errorMessageOnFail = "Menu has not found")
            onNode(matcher = hasTestTag(testTag = "Main text")).assertExists(errorMessageOnFail = "Main text has not found")
            onNode(matcher = hasTestTag(testTag = "Numbered list")).assertExists(errorMessageOnFail = "Numbered list text has not found")
        }
    }

    @Test
    fun checkOpenDrawerHasInvokedAfterMenuClick() = with(receiver = composeTestRule) {
        // Given
        var hasInvoked = false
        setContent {
            KamAZ6282Screen.GetView(openDrawer = {
                hasInvoked = true
            })
        }

        // When
        onNode(matcher = hasContentDescription(value = "Menu")).performClick()

        // Then
        assertThat(hasInvoked).isTrue()
    }
}
