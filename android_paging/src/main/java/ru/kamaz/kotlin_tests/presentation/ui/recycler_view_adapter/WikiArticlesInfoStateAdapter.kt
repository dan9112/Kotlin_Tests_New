package ru.kamaz.kotlin_tests.presentation.ui.recycler_view_adapter

import android.view.ViewGroup
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter

class WikiArticlesInfoStateAdapter(private val retry: () -> Unit) :
    LoadStateAdapter<WikiArticlesInfoLoadStateViewHolder>() {
    override fun onBindViewHolder(
        holder: WikiArticlesInfoLoadStateViewHolder,
        loadState: LoadState
    ) = holder.bind(loadState)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        loadState: LoadState
    ) = WikiArticlesInfoLoadStateViewHolder.create(parent, retry)
}