package ru.kamaz.compose_catalog

import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import ru.kamaz.compose_catalog.views.screens.KamAZ6282Screen

@RunWith(JUnit4::class)
class MainViewModelTest {
    private lateinit var viewModel: MainViewModel

    @Before
    fun setUp() {
        viewModel = MainViewModel()
    }

    @Test
    fun checkGetCurrentScreen() {
        // Given
        val screen = KamAZ6282Screen

        // When
        viewModel.setScreen(screen)

        // Then
        assertThat(viewModel.currentScreen.value).isEqualTo(screen)
    }
}
