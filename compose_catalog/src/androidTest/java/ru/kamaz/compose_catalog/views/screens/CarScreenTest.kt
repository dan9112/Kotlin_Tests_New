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
class CarScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun existsAllItems(): Unit = with(receiver = composeTestRule) {
        with(receiver = KamAZ6282Screen) {
            // Given
            setContent {
                GetView {}
            }

            // ???
            onNode(matcher = hasContentDescription(value = "Menu")).assertExists(errorMessageOnFail = "menu has not found")
            onNode(matcher = hasTestTag(testTag = "contentText")).assertExists(errorMessageOnFail = "text has not found")
            onNode(matcher = hasContentDescription(value = "image")).assertExists(errorMessageOnFail = "image has not found")
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
