package ru.kamaz.kotlin_tests.presentation.view_model

import androidx.lifecycle.ViewModelProvider
import androidx.savedstate.SavedStateRegistryOwner
import kotlinx.coroutines.ExperimentalCoroutinesApi
import ru.kamaz.kotlin_tests.data.repository.WikiArticlesRepository
import ru.kamaz.kotlin_tests.data.service.WikiArticleService
import ru.kamaz.kotlin_tests.domain.repository.RepositoryInterface

/**
 * Class that handles object creation.
 * Like this, objects can be passed as parameters in the constructors and then replaced for
 * testing, where needed.
 */
@ExperimentalCoroutinesApi
object Injection {

    /**
     * Creates an instance of [WikiArticlesRepository] based on the [WikiArticleService] and a
     * WikiLocalCache
     */
    private fun provideRepository(repository: Repository): RepositoryInterface = when (repository) {
        Repository.WikiRepository -> WikiArticlesRepository(WikiArticleService.create())
    }

    /**
     * Provides the [ViewModelProvider.Factory] that is then used to get a reference to
     * [ViewModel][SearchArticlesViewModel] objects.
     */
    fun provideViewModelFactory(
        owner: SavedStateRegistryOwner,
        repository: Repository = Repository.WikiRepository
    ) = ViewModelFactory(owner, provideRepository(repository))

    enum class Repository {
        WikiRepository
    }
}
