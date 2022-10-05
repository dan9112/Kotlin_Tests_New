package com.example.mvp_example

interface IMainActivityPresenter {
    val currentText: CharSequence

    fun changeText()
}