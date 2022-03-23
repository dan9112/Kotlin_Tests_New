package ru.kamaz.kotlin_tests.presentation.view_model

import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.savedstate.SavedStateRegistryOwner
import kotlinx.coroutines.ExperimentalCoroutinesApi
import ru.kamaz.kotlin_tests.domain.repository.RepositoryInterface

@ExperimentalCoroutinesApi
class ViewModelFactory(
    owner: SavedStateRegistryOwner,
    private val repositoryInterface: RepositoryInterface
) : AbstractSavedStateViewModelFactory(owner, null) {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(
        key: String,
        modelClass: Class<T>,
        handle: SavedStateHandle
    ) = when {
        (modelClass.isAssignableFrom(SearchArticlesViewModel::class.java)) -> SearchArticlesViewModel(
            repositoryInterface, handle
        ) as T
        else -> throw IllegalArgumentException("Unknown ViewModel class")
    }
}
