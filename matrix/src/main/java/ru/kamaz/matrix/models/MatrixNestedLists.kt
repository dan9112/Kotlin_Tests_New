package ru.kamaz.matrix.models

open class MatrixNestedLists<E>(
    final override val height: Int,
    final override val width: Int,
    defaultValue: E
) : Matrix<E> {
    private val list = mutableListOf<MutableList<E>>()

    init {
        for (i in 0 until height) list += MutableList(size = width) { defaultValue }
    }

    @Throws(IndexOutOfBoundsException::class)
    override fun get(row: Int, column: Int) =
        if (row >= height || column >= width) throw IndexOutOfBoundsException()
        else list[row][column]

    @Throws(IndexOutOfBoundsException::class)
    override fun set(row: Int, column: Int, value: E) =
        if (row >= height || column >= width) throw IndexOutOfBoundsException()
        else list[row][column] = value

    override fun equals(other: Any?) =
        other is Matrix<*> && height == other.height && width == other.width && isSame(other)

    private fun isSame(matrix: Matrix<*>) = run {
        for (i in 0 until height) for (j in 0 until width) if (list[i][j] != matrix[i, j]) return@run false
        true
    }

    override fun hashCode() = run {
        var result = height
        result = 31 * result + width
        result
    }

    override fun toString() = list.toString()
}
