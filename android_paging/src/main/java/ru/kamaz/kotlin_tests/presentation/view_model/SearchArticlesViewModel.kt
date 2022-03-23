package ru.kamaz.kotlin_tests.presentation.view_model

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ru.kamaz.kotlin_tests.domain.repository.RepositoryInterface
import ru.kamaz.kotlin_tests.domain.repository.RepositoryInterface.Companion.NETWORK_PAGE_SIZE
import ru.kamaz.kotlin_tests.domain.model.WikiArticleInfo

@ExperimentalCoroutinesApi
class SearchArticlesViewModel(
    private val repositoryInterface: RepositoryInterface,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    /**
     * Stream of immutable states representative of the UI.
     */
    val state: StateFlow<UiState>

    val pagingDataFlow: Flow<PagingData<WikiArticleInfo>>

    /**
     * Processor of side effects from the UI which in turn feedback into [state]
     */
    val accept: (UiAction) -> Unit

    private fun searchArticle(queryString: String): Flow<PagingData<WikiArticleInfo>> =
        repositoryInterface.getSearchResultStream(queryString, NETWORK_PAGE_SIZE)

    override fun onCleared() {
        savedStateHandle[LAST_SEARCH_QUERY] = state.value.query
        savedStateHandle[LAST_QUERY_SCROLLED] = state.value.lastQueryScrolled
        super.onCleared()
    }

    init {
        val initialQuery = savedStateHandle.get(LAST_SEARCH_QUERY) ?: DEFAULT_QUERY
        val lastQueryScrolled = savedStateHandle.get(LAST_QUERY_SCROLLED) ?: DEFAULT_QUERY
        val actionStateFlow = MutableSharedFlow<UiAction>()
        val searches = actionStateFlow.filterIsInstance<UiAction.Search>().distinctUntilChanged()
            .onStart { emit(UiAction.Search(query = initialQuery)) }
        val queriesScrolled =
            actionStateFlow.filterIsInstance<UiAction.Scroll>().distinctUntilChanged().shareIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
                replay = 1
            ).onStart { emit(UiAction.Scroll(currentQuery = lastQueryScrolled)) }
        pagingDataFlow = searches.flatMapLatest { searchArticle(queryString = it.query) }
            .cachedIn(viewModelScope)

        state = combine(
            searches,
            queriesScrolled,
            ::Pair
        ).map { (search, scroll) ->
            UiState(
                query = search.query,
                lastQueryScrolled = scroll.currentQuery,
                hasNotScrolledForCurrentSearch = search.query != scroll.currentQuery
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
            initialValue = UiState()
        )

        accept = { action -> viewModelScope.launch { actionStateFlow.emit(action) } }
    }
}

sealed class UiAction {
    data class Search(val query: String) : UiAction()
    data class Scroll(val currentQuery: String) : UiAction()
}

data class UiState(
    val query: String = DEFAULT_QUERY,
    val lastQueryScrolled: String = DEFAULT_QUERY,
    val hasNotScrolledForCurrentSearch: Boolean = false
)

private const val DEFAULT_QUERY = "cabir"
private const val LAST_QUERY_SCROLLED = "last_query_scrolled"
private const val LAST_SEARCH_QUERY = "last_search_query"