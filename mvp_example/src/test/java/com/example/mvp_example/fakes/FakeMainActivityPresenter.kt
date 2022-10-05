package com.example.mvp_example.fakes

import com.example.mvp_example.IMainActivityPresenter
import com.example.mvp_example.IMainActivityView
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch

@ExperimentalCoroutinesApi
class FakeMainActivityPresenter(private val view: IMainActivityView, coroutineDispatcher: CoroutineDispatcher) :
    IMainActivityPresenter {
    private val scope = CoroutineScope(coroutineDispatcher)

    override val currentText
        get() = view.getCurrentText()

    override fun changeText() {
        with(view) {
            disableUI()
            scope.launch {
                setText(newText)
                enableUI()
            }
        }
    }

    companion object {
        const val newText = "newText"
    }
}
