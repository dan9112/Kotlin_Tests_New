package ru.kamaz.kotlin_tests.presentation.ui.recycler_view_adapter

import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import ru.kamaz.kotlin_tests.domain.model.WikiArticleInfo
import ru.kamaz.kotlin_tests.presentation.ui.recycler_view__view_holder.WikiArticlesInfoViewHolder

class WikiArticlesInfoAdapter :
    PagingDataAdapter<WikiArticleInfo, WikiArticlesInfoViewHolder>(REPO_COMPARATOR) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): WikiArticlesInfoViewHolder {
        return WikiArticlesInfoViewHolder.create(parent)
    }

    override fun onBindViewHolder(holder: WikiArticlesInfoViewHolder, position: Int) {
        val repoItem = getItem(position)
        if (repoItem != null) {
            holder.bind(repoItem)
        }
    }

    companion object {
        private val REPO_COMPARATOR = object : DiffUtil.ItemCallback<WikiArticleInfo>() {
            override fun areItemsTheSame(oldItem: WikiArticleInfo, newItem: WikiArticleInfo): Boolean =
                oldItem.title == newItem.title

            override fun areContentsTheSame(oldItem: WikiArticleInfo, newItem: WikiArticleInfo): Boolean =
                oldItem == newItem
        }
    }
}