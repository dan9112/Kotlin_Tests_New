package com.example.mvp_example

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@ExperimentalCoroutinesApi
internal class DataManagerTest {
    private val dataManager = DataManager

    @Test
    @DisplayName("When ask string, then get one from the private predefined list in DataManager")
    fun `get string`() = runTest {
        val text = dataManager.getString()
        assertThat(text).isNotNull()
        val dataList = with(DataManager::class.java.getDeclaredField("strings")) {
            isAccessible = true
            get(dataManager) as List<*>
        }

        assertThat(dataList).contains(text)
    }
}
