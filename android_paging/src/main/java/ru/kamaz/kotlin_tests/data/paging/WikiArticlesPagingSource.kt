package ru.kamaz.kotlin_tests.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import retrofit2.HttpException
import ru.kamaz.kotlin_tests.data.service.WikiArticleService
import ru.kamaz.kotlin_tests.data.response.Search
import java.io.IOException

private const val WEB_RESOURSE_STARTING_PAGE_INDEX = 0

class WikiArticlesPagingSource(
    private val service: WikiArticleService,
    private val query: String,
    private val searchLimit: Int
) :
    PagingSource<Int, Search>() {
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Search> {
        val position = params.key ?: WEB_RESOURSE_STARTING_PAGE_INDEX
        val limit = searchLimit
        return try {
            val response = service.searchArticles(
                searchString = query,
                searchOffset = (limit * position),
                searchLimit = limit
            )
            LoadResult.Page(
                data = response.query.search,
                prevKey = if (position == WEB_RESOURSE_STARTING_PAGE_INDEX) null else position - 1,
                nextKey = if (response._continue == null) null else position + 1
            )
        } catch (exception: IOException) {
            return LoadResult.Error(exception)
        } catch (exception: HttpException) {
            LoadResult.Error(exception)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Search>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
        }
    }
}
