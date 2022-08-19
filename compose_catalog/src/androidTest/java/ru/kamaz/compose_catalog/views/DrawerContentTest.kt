package ru.kamaz.compose_catalog.views

import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import ru.kamaz.compose_catalog.views.screens.*

@RunWith(AndroidJUnit4::class)
class DrawerContentTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun existsAllItems() = with(receiver = composeTestRule) {
        // Given
        setContent {
            DrawerContent(setScreen = {}, getScreen = { StartScreen }, closeDrawer = {})
        }

        // ???
        onNode(matcher = hasTestTag(testTag = "header"))
            .assertExists(errorMessageOnFail = "Header element has not found in component tree")
        CarScreen.values.forEach {
            onNode(matcher = hasTestTag(testTag = it.toString()))
                .assertExists(errorMessageOnFail = "$it has not found in component tree")
        }
    }

    @Test
    fun checkCloseDrawerAndSetScreenHaveInvokedAfterHeaderClick() =
        checkCloseDrawerAndSetScreenHaveInvokedAfterItemClick(item = StartScreen)

    @Test
    fun checkCloseDrawerAndSetScreenHaveInvokedAfterKamAZ4310Click() =
        checkCloseDrawerAndSetScreenHaveInvokedAfterItemClick(item = KamAZ4310Screen)

    @Test
    fun checkCloseDrawerAndSetScreenHaveInvokedAfterKamAZ5511Click() =
        checkCloseDrawerAndSetScreenHaveInvokedAfterItemClick(item = KamAZ5511Screen)

    @Test
    fun checkCloseDrawerAndSetScreenHaveInvokedAfterKamAZ6282Click() =
        checkCloseDrawerAndSetScreenHaveInvokedAfterItemClick(item = KamAZ6282Screen)

    @Test
    fun checkCloseDrawerAndSetScreenHaveInvokedAfterKamAZ6350Click() =
        checkCloseDrawerAndSetScreenHaveInvokedAfterItemClick(item = KamAZ6350Screen)

    private fun checkCloseDrawerAndSetScreenHaveInvokedAfterItemClick(item: DrawerAppScreen) =
        with(receiver = composeTestRule) {
            // Given
            var hasInvoked = false
            var screen: DrawerAppScreen? = null
            setContent {
                DrawerContent(
                    setScreen = { screen = it },
                    getScreen = { StartScreen },
                    closeDrawer = { hasInvoked = true })
            }

            // When
            onNode(matcher = hasText(text = item.toString())).performClick()

            // Then
            assertThat(hasInvoked).isTrue()
            assertThat(screen).isEqualTo(item)
        }
}
