package ru.kamaz.compose_catalog.di

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import ru.kamaz.compose_catalog.MainViewModel

val appModule = module { viewModel { MainViewModel() } }
