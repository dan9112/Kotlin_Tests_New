package com.example.mvp_example

import com.example.mvp_example.fakes.FakeMainActivityPresenter
import com.example.mvp_example.fakes.FakeMainActivityPresenter.Companion.newText
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.*
import org.mockito.Mockito.clearAllCaches
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

@ExperimentalCoroutinesApi
internal class MainActivityPresenterTest {
    private lateinit var presenter: IMainActivityPresenter

    @Nested
    @DisplayName("Synchronous Code Tests")
    inner class SimpleTests {

        @BeforeEach
        fun setUp() {
            clearAllCaches()
        }

        @Test
        @DisplayName("When ask presenter's current text, then view's fun of the same name will be asked")
        fun `get current text with stub return value`() {
            presenter = MainActivityPresenter(mock { on { getCurrentText() } doReturn defaultText })

            assertThat(presenter.currentText).isEqualTo(defaultText)
        }

    }

    @Nested
    @DisplayName("Asynchronous Code Tests with standard test dispatcher")
    inner class StandardDispatcherTests {
        private val testDispatcher = StandardTestDispatcher()
        private lateinit var currentText: CharSequence
        private var uiIsEnable = true

        @BeforeEach
        fun before() {
            currentText = defaultText
            uiIsEnable = true
            Dispatchers.setMain(testDispatcher)
        }

        @AfterEach
        fun after() {
            Dispatchers.resetMain()
        }

        @Test
        @DisplayName("When ask change text, then UI will be block until anew string is got and the text is updated")
        fun changeText() = runTest {
            presenter.changeText()

            assertThat(uiIsEnable).isFalse()
            assertThat(currentText).isEqualTo(defaultText)

            testDispatcher.scheduler.runCurrent()

            assertThat(uiIsEnable).isTrue()
            assertThat(currentText).isEqualTo(newText)
        }

        init {
            presenter = FakeMainActivityPresenter(
                object : IMainActivityView {
                    override fun setText(text: String) {
                        currentText = text
                    }

                    override fun disableUI() {
                        uiIsEnable = false
                    }

                    override fun enableUI() {
                        uiIsEnable = true
                    }

                    override fun getCurrentText() = currentText

                },
                testDispatcher
            )
        }
    }

    companion object {
        private const val defaultText = "defaultText"
    }
}
