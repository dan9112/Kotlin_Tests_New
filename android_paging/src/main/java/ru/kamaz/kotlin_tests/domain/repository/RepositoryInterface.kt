package ru.kamaz.kotlin_tests.domain.repository

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import ru.kamaz.kotlin_tests.domain.model.WikiArticleInfo

interface RepositoryInterface {
    fun getSearchResultStream(
        query: String,
        searchLimit: Int = NETWORK_PAGE_SIZE
    ): Flow<PagingData<WikiArticleInfo>>

    companion object {
        const val NETWORK_PAGE_SIZE = 100
    }
}