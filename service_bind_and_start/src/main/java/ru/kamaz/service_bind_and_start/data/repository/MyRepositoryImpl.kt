package ru.kamaz.service_bind_and_start.data.repository

import android.content.Context
import ru.kamaz.service_bind_and_start.domain.repository.MyRepository

private const val file = "spvf"
private const val valueKey = "vk"

class MyRepositoryImpl(private val context: Context) : MyRepository {
    override fun saveResult(result: Boolean) {
        context.getSharedPreferences(file, Context.MODE_PRIVATE).edit().putBoolean(
            valueKey, result
        ).apply()
    }

    override fun getResult() = context.getSharedPreferences(file, Context.MODE_PRIVATE).run {
        if (!contains(valueKey)) null
        else getBoolean(valueKey, false)
    }

    override fun deleteResult() = context.getSharedPreferences(file, Context.MODE_PRIVATE).run {
        if (!contains(valueKey)) false
        else {
            edit().remove(valueKey).apply()
            true
        }
    }
}
