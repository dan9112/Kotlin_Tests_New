package ru.kamaz.service_bind_and_start.domain.repository

interface MyRepository {
    /**
     * Функция сохранения результата
     * @param result булевое значение результата
     */
    fun saveResult(result: Boolean)

    /**
     * Функция получения ранее сохранённого результата
     * @return ранее сохранённый результат или null, если в памяти нет сохранённого результата
     */
    fun getResult(): Boolean?

    /**
     * Функция удаления ранее сохранённого результата
     * @return true, если результат был удалён, и false, если нет
     */
    fun deleteResult(): Boolean
}
