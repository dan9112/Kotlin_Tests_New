package ru.kamaz.service_bind_and_start.domain.usecases.ui.blank

import ru.kamaz.service_bind_and_start.domain.repository.MyRepository

object GetBooleanUseCase {
    fun MyRepository.getBoolean() = getResult()
}
