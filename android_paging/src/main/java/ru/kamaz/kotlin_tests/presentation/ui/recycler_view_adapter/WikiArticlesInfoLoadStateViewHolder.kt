package ru.kamaz.kotlin_tests.presentation.ui.recycler_view_adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.paging.LoadState
import androidx.recyclerview.widget.RecyclerView
import ru.kamaz.kotlin_tests.databinding.WikiArticlesInfoLoadStateFooterViewItemBinding

class WikiArticlesInfoLoadStateViewHolder(
    private val binding: WikiArticlesInfoLoadStateFooterViewItemBinding,
    retry: () -> Unit
) : RecyclerView.ViewHolder(binding.root) {
    init {
        binding.retryButton.setOnClickListener { retry.invoke() }
    }

    fun bind(loadState: LoadState) = binding.run {
        progressBar.isVisible = loadState is LoadState.Loading
        retryButton.isVisible = loadState is LoadState.Error
        errorMsg.isVisible = loadState is LoadState.Error
    }

    companion object {
        fun create(parent: ViewGroup, retry: () -> Unit) =
            WikiArticlesInfoLoadStateFooterViewItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ).run {
                WikiArticlesInfoLoadStateViewHolder(this, retry)
            }
    }
}
