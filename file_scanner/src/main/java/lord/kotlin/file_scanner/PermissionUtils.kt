package lord.kotlin.file_scanner

import android.app.Activity
import android.content.Context.MODE_PRIVATE
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import timber.log.Timber

/** Объект проверки разрешения */
object PermissionUtils {
    /** Внутри этого файла shared_preference мы просто будем хранить информацию о том,
     * запрашивал ли пользователь текущее разрешение ранее или нет
     * @see PREFS_CURRENT_KEY */
    private const val PREFS_FILE_NAME = "preference_permission"

    /** Ключ для хранения значения текущего флага
     * @see PREFS_FILE_NAME */
    private lateinit var PREFS_CURRENT_KEY: String

    /** Метод получения флага первого запроса разрешения в SharedPreferences */
    private fun getApplicationLaunchedFirstTime(activity: Activity): Boolean {
        return activity.getSharedPreferences(PREFS_FILE_NAME, MODE_PRIVATE)
            .getBoolean(PREFS_CURRENT_KEY, true)
    }

    /** Метод сохранения флага первого запуска в SharedPreferences */
    private fun setApplicationLaunchedFirstTime(activity: Activity) {
        activity.getSharedPreferences(PREFS_FILE_NAME, MODE_PRIVATE).edit().apply {
            putBoolean(PREFS_CURRENT_KEY, false)
        }.apply()
    }

    /** Функция проверки разрешения
     * @param activity              активность, которая запрашивает разрешение
     * @param permission            разрешение, которое необходимо в данной активности
     * @param permissionAskListener [интерфейс][PermissionAskListener], в котором в виде функций отображены все возможные сценарии после проверки? (не знаю как лучше сформулировать)
     * @param keyForStore           ключ, по которому будет храниться флаг первого запроса [разрешения][permission] */
    fun checkPermission(
        activity: Activity,
        permission: String,
        permissionAskListener: PermissionAskListener,
        keyForStore: String
    ) {
        PREFS_CURRENT_KEY = keyForStore
        Timber.d("checkPermission")
        //проверьте, предоставлено ли уже разрешение, т.е. приложение было запущено ранее, и пользователь тогда предоставил разрешение.
        if (ContextCompat.checkSelfPermission(
                activity,
                permission
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            /*
                    У нас нет разрешения, возникают два случая:
                    1. Приложение запущено впервые,
                    2. Приложение было запущено ранее, и пользователь отказал в разрешении при последнем запуске.
                        2А. Пользователь ранее отказал в разрешении БЕЗ флажка "Больше не спрашивать"
                        2Б. Пользователь ранее отказал в разрешении, установив флажок "Больше не спрашивать"
                */
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {

                /*
                        shouldShowRequestPermissionRationale вернул true
                        это означает Case: 2A
                        см. блок-схему, единственный случай, когда shouldShowRequestPermissionRationale возвращает "true", - это когда приложение было запущено ранее, и пользователь "отказал" в разрешении при последнем запуске БЕЗ проверки "никогда не показывать снова"
                    */
                Timber.d("onPermissionDenied")
                permissionAskListener.onPermissionPreviouslyDenied()
            } else {
                /*
                        это означает, либо случай 1, либо случай 2Б
                        См. Блок-схему, shouldShowRequestPermissionRationale возвращает false, только когда приложение запускается в первый раз (Случай: 1) или приложение было запущено ранее, а затем пользователь HAD поставил флажок «Никогда не показывать снова» (Случай: 2B)
                    */
                if (getApplicationLaunchedFirstTime(activity)) {

                    //Случай 1
                    Timber.d("ApplicationLaunchedFirstTime")
                    setApplicationLaunchedFirstTime(activity) //  ** НЕ ЗАБУДЬТЕ ЭТО **
                    permissionAskListener.onPermissionRequest()
                } else {
                    //Случай 2Б
                    Timber.d("onPermissionDisabled")
                    permissionAskListener.onPermissionDisabled()
                }
            }
        } else {
            Timber.d("Permission already granted")
            permissionAskListener.onPermissionGranted()
        }
    }

    /** Интерфейс, содержащий 4 метода
     * @see onPermissionGranted
     * @see onPermissionRequest
     * @see onPermissionPreviouslyDenied
     * @see onPermissionDisabled */
    interface PermissionAskListener {
        /** Пользователь уже предоставил это разрешение
         *
         * Приложение должно было быть запущено ранее, и пользователь уже должен был предоставить это разрешение */
        fun onPermissionGranted()

        /**Приложение запускается ПЕРВЫЙ РАЗ.
         *
         * Нам не нужно показывать дополнительные пояснения, мы просто запрашиваем разрешение */
        fun onPermissionRequest()

        /** Приложение было запущено ранее, и пользователь просто «отказал» в разрешении. Пользователь НЕ нажимал "НЕ ПОКАЗАТЬ СНОВА".
         *
         * В этом случае нам нужно показывать дополнительные пояснения, объясняя, как «предоставление этого разрешения» будет полезно для пользователя */
        fun onPermissionPreviouslyDenied()

        /** Приложение было запущено ранее, и пользователь "отказал" в разрешении. И ТАКЖЕ нажал "НЕ ПОКАЗАТЬ СНОВА".
         *
         * Нам нужно показать Toask / alerttdiaTimber / ..., чтобы указать, что пользователь отказал в разрешении, установив флажок «Не показывать снова» ...
         * Итак, вы можете перенаправить пользователя на страницу настроек> приложение> разрешения, где пользователь может предоставить разрешение .. */
        fun onPermissionDisabled()
    }
}
