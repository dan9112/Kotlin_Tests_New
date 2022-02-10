package ru.kamaz.nier_visualizer

import android.app.Application
import android.content.Intent
import android.media.MediaPlayer
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import ru.kamaz.nier_visualizer.MusicService.Companion.actionDestroy

class MainActivityViewModel(application: Application) : AndroidViewModel(application) {

    override fun onCleared() {
        super.onCleared()
        getApplication<Application>().startService(
            Intent(
                getApplication(),
                MusicService::class.java
            ).setAction(actionDestroy)
        )
    }
}
