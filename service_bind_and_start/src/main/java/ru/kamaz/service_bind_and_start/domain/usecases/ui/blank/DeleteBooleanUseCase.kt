package ru.kamaz.service_bind_and_start.domain.usecases.ui.blank

import ru.kamaz.service_bind_and_start.domain.repository.MyRepository

object DeleteBooleanUseCase {
    fun MyRepository.deleteBoolean() = deleteResult()
}
