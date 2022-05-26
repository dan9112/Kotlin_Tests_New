package ru.kamaz.service_bind_and_start.domain.usecases.ui.blank

import ru.kamaz.service_bind_and_start.domain.repository.MyRepository

/**
 * UseCase для удаления ранее сохранённой булевой переменной
 */
object DeleteBooleanUseCase {
    /**
     * Метод удаления булевой переменной
     */
    fun MyRepository.deleteBoolean() = deleteResult()
}
