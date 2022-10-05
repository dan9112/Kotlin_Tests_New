package com.example.mvp_example

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivityPresenter(private val view: IMainActivityView) : IMainActivityPresenter {
    private val model = DataManager
    private val scope = CoroutineScope(Dispatchers.IO)

    override val currentText
        get() = view.getCurrentText()

    override fun changeText() {
        with(view) {
            disableUI()
            scope.launch {
                val text = model.getString()
                withContext(Dispatchers.Main) {
                    setText(text)
                    enableUI()
                }
            }
        }
    }
}
