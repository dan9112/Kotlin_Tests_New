package ru.kamaz.matrix.models.templates

import ru.kamaz.matrix.models.core.Cell
import ru.kamaz.matrix.models.core.Matrix

open class MatrixMap<E>(
    final override val height: Int,
    final override val width: Int,
    defaultValue: E
) : Matrix<E> {
    private val map = mutableMapOf<Cell, E>()

    init {
        for (i in 0 until height) for (j in 0 until width) {
            map[Cell(i, j)] = defaultValue
        }
    }

    @Throws(IndexOutOfBoundsException::class)
    override fun get(row: Int, column: Int) =
        map[Cell(row, column)] ?: throw IndexOutOfBoundsException()

    @Throws(IndexOutOfBoundsException::class)
    override fun set(row: Int, column: Int, value: E) =
        if (row >= height || column >= width) throw IndexOutOfBoundsException() else map[Cell(
            row, column
        )] = value

    override fun equals(other: Any?) =
        other is Matrix<*> && height == other.height && width == other.width && isSame(other)

    private fun isSame(matrix: Matrix<*>) = run {
        for (i in 0 until height) for (j in 0 until width) if (map[Cell(
                i, j
            )] != matrix[i, j]
        ) return@run false
        true
    }

    override fun hashCode(): Int {
        var result = height
        result = 31 * result + width
        return result
    }

    override fun toString() = map.toString()
}
