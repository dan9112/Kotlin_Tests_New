package ru.kamaz.foreground_service

import android.content.Context.MODE_PRIVATE
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import ru.kamaz.foreground_service.App.Companion.topLevelSharedPreferencesFile
import ru.kamaz.foreground_service.databinding.Fragment0Binding

class Fragment0 : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ) = DataBindingUtil.inflate<Fragment0Binding>(
        inflater, R.layout.fragment_0, container, false
    ).run {
        with(receiver = findNavController()) {
            with(receiver = requireContext().getSharedPreferences(topLevelSharedPreferencesFile, MODE_PRIVATE)) {
                if (getBoolean("needSplashScreen", true)) {
                    edit().putBoolean("needSplashScreen", false).apply()
                    navigate(Fragment0Directions.actionFragment0ToSplashScreenFragment())
                }
            }
            button01.setOnClickListener {
                navigate(Fragment0Directions.actionFragment0ToFragment1())
            }
            button02.setOnClickListener {
                navigate(Fragment0Directions.actionFragment0ToFragment2())
            }
        }
        root
    }
}
