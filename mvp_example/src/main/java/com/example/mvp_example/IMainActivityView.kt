package com.example.mvp_example

interface IMainActivityView {
    fun setText(text: String)
    fun disableUI()
    fun enableUI()
    fun getCurrentText(): CharSequence
}
