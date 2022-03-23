package ru.kamaz.kotlin_tests.presentation.ui.recycler_view__view_holder

import android.view.LayoutInflater
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ru.kamaz.kotlin_tests.R
import ru.kamaz.kotlin_tests.databinding.SearchViewItemBinding
import ru.kamaz.kotlin_tests.domain.model.WikiArticleInfo

class WikiArticlesInfoViewHolder(private val binding: SearchViewItemBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(article: WikiArticleInfo?) = binding.run {
        if (article != null) showSearchData(article)
        else {
            val resources = root.resources
            articleTitle.text = resources.getString(R.string.loading)
            articleShortText.visibility = GONE
            articleData.visibility = GONE
        }
    }

    private fun showSearchData(article: WikiArticleInfo) = binding.run {
        article.run {
            articleTitle.text = title

            // if the description is missing, hide the TextView
            articleShortText.run {
                var descriptionVisibility = GONE
                if (snippet != null) {
                    text = snippet
                    descriptionVisibility = VISIBLE
                }
                visibility = descriptionVisibility
            }

            articleData.run {
                text = timestamp
                visibility = VISIBLE
            }
        }
    }

    companion object {
        fun create(parent: ViewGroup) = WikiArticlesInfoViewHolder(
            SearchViewItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }
}