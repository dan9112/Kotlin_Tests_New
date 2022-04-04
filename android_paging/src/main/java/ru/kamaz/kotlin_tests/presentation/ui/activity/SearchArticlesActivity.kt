package ru.kamaz.kotlin_tests.presentation.ui.activity

import android.os.Bundle
import android.view.KeyEvent.ACTION_DOWN
import android.view.KeyEvent.KEYCODE_ENTER
import android.view.inputmethod.EditorInfo.IME_ACTION_GO
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ru.kamaz.kotlin_tests.databinding.ActivitySearchWikiArticlesBinding
import ru.kamaz.kotlin_tests.domain.model.WikiArticleInfo
import ru.kamaz.kotlin_tests.presentation.ui.recycler_view_adapter.WikiArticlesInfoAdapter
import ru.kamaz.kotlin_tests.presentation.ui.recycler_view_adapter.WikiArticlesInfoStateAdapter
import ru.kamaz.kotlin_tests.presentation.view_model.Injection
import ru.kamaz.kotlin_tests.presentation.view_model.SearchArticlesViewModel
import ru.kamaz.kotlin_tests.presentation.view_model.UiAction
import ru.kamaz.kotlin_tests.presentation.view_model.UiState

@ExperimentalCoroutinesApi
class SearchArticlesActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivitySearchWikiArticlesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val viewModel = ViewModelProvider(
            this,
            Injection.provideViewModelFactory(owner = this)
        )[SearchArticlesViewModel::class.java]

        val decoration = DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        binding.run {
            list.addItemDecoration(decoration)
            viewModel.run {
                bindState(uiState = state, pagingData = pagingDataFlow, uiActions = accept)
            }
        }
    }

    private fun ActivitySearchWikiArticlesBinding.bindState(
        uiState: StateFlow<UiState>,
        pagingData: Flow<PagingData<WikiArticleInfo>>,
        uiActions: (UiAction) -> Unit
    ) {
        val searchAdapter = WikiArticlesInfoAdapter()
        list.adapter = searchAdapter.withLoadStateHeaderAndFooter(
            header = WikiArticlesInfoStateAdapter { searchAdapter.retry() },
            footer = WikiArticlesInfoStateAdapter { searchAdapter.retry() }
        )

        bindSearch(uiState = uiState, onQueryChanged = uiActions)
        bindList(
            infoAdapter = searchAdapter,
            uiState = uiState,
            pagingData = pagingData,
            onScrollChanged = uiActions
        )
    }

    private fun ActivitySearchWikiArticlesBinding.bindSearch(
        uiState: StateFlow<UiState>,
        onQueryChanged: (UiAction.Search) -> Unit
    ) {
        searchArticle.run {
            setOnEditorActionListener { _, actionId, _ ->
                if (actionId == IME_ACTION_GO) {
                    updateRepoListFromInput(onQueryChanged)
                    true
                } else false
            }
            setOnKeyListener { _, keyCode, event ->
                if (event.action == ACTION_DOWN && keyCode == KEYCODE_ENTER) {
                    updateRepoListFromInput(onQueryChanged)
                    true
                } else false
            }
        }
        lifecycleScope.launch {
            uiState
                .map { it.query }
                .distinctUntilChanged()
                .collect(searchArticle::setText)
        }
    }

    private fun ActivitySearchWikiArticlesBinding.updateRepoListFromInput(onQueryChanged: (UiAction.Search) -> Unit) {
        searchArticle.text.trim().let {
            if (it.isNotEmpty()) {
                list.scrollToPosition(0)
                onQueryChanged(UiAction.Search(query = it.toString()))
            }
        }
    }

    private fun ActivitySearchWikiArticlesBinding.bindList(
        infoAdapter: WikiArticlesInfoAdapter,
        uiState: StateFlow<UiState>,
        pagingData: Flow<PagingData<WikiArticleInfo>>,
        onScrollChanged: (UiAction.Scroll) -> Unit
    ) {
        list.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy != 0) onScrollChanged(UiAction.Scroll(currentQuery = uiState.value.query))
            }
        })
        val notLoading = infoAdapter.loadStateFlow
            // Only emit when REFRESH LoadState for the paging source changes.
            .distinctUntilChangedBy { it.source.refresh }
            // Only react to cases where REFRESH completes i.e., NotLoading.
            .map { it.source.refresh is LoadState.NotLoading }

        val hasNotScrolledForCurrentSearch = uiState
            .map { it.hasNotScrolledForCurrentSearch }
            .distinctUntilChanged()

        val shouldScrollToTop = combine(
            notLoading,
            hasNotScrolledForCurrentSearch,
            Boolean::and
        ).distinctUntilChanged()

        lifecycleScope.run {
            launch {
                pagingData.collectLatest(infoAdapter::submitData)
            }

            launch {
                shouldScrollToTop.collect { shouldScroll ->
                    if (shouldScroll) list.scrollToPosition(0)
                }
            }
        }
    }
}
