package ru.kamaz.compose_catalog.views

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
    fun hasAllItems() = with(receiver = composeTestRule) {
        // Given
        setContent {
            DrawerContent(setScreen = {}, getScreen = { StartScreen }, closeDrawer = {})
        }

        // When
        onNode(matcher = hasText(text = "КамАЗ"))
            .assertExists(errorMessageOnFail = "Header element not found in component tree")
        CarScreen.values.forEach {
            onNode(matcher = hasText(text = it.toString()))
                .assertExists(errorMessageOnFail = "$it not found in component tree")
        }
    }

    @Test
    fun checkNavigationDrawerCloseAndSetScreenAfterHeaderClick() =
        checkNavigationDrawerCloseAndSetScreenAfterItemClick(item = StartScreen)

    @Test
    fun checkNavigationDrawerCloseAndSetScreenAfterKamAZ4310Click() =
        checkNavigationDrawerCloseAndSetScreenAfterItemClick(item = KamAZ4310Screen)

    @Test
    fun checkNavigationDrawerCloseAndSetScreenAfterKamAZ5511Click() =
        checkNavigationDrawerCloseAndSetScreenAfterItemClick(item = KamAZ5511Screen)

    @Test
    fun checkNavigationDrawerCloseAndSetScreenAfterKamAZ6282Click() =
        checkNavigationDrawerCloseAndSetScreenAfterItemClick(item = KamAZ6282Screen)

    @Test
    fun checkNavigationDrawerCloseAndSetScreenAfterKamAZ6350Click() =
        checkNavigationDrawerCloseAndSetScreenAfterItemClick(item = KamAZ6350Screen)

    private fun checkNavigationDrawerCloseAndSetScreenAfterItemClick(item: DrawerAppScreen) =
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
