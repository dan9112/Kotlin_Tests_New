package ru.kamaz.kotlin_tests.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.map
import kotlinx.coroutines.flow.map
import ru.kamaz.kotlin_tests.data.service.WikiArticleService
import ru.kamaz.kotlin_tests.data.paging.WikiArticlesPagingSource
import ru.kamaz.kotlin_tests.domain.repository.RepositoryInterface
import ru.kamaz.kotlin_tests.domain.model.WikiArticleInfo

class WikiArticlesRepository(private val service: WikiArticleService) : RepositoryInterface {
    override fun getSearchResultStream(query: String, searchLimit: Int) = Pager(
        config = PagingConfig(pageSize = searchLimit, enablePlaceholders = false),
        pagingSourceFactory = { WikiArticlesPagingSource(service, query, searchLimit) }
    ).flow.map { pagingData ->
        pagingData.map { search ->
            WikiArticleInfo(
                title = search.title,
                snippet = search.snippet,
                timestamp = search.timestamp
            )
        }
    }
}
