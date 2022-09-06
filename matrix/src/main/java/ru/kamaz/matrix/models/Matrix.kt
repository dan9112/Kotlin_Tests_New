package ru.kamaz.matrix.models

interface Matrix<E> {
    val height: Int
    val width: Int

    @Throws(IndexOutOfBoundsException::class)
    operator fun get(row: Int, column: Int): E
    @Throws(IndexOutOfBoundsException::class)
    operator fun get(cell: Cell): E = get(cell.row, cell.column)

    @Throws(IndexOutOfBoundsException::class)
    operator fun set(row: Int, column: Int, value: E)
    @Throws(IndexOutOfBoundsException::class)
    operator fun set(cell: Cell, value: E) = set(cell.row, cell.column, value)

    fun <E> transpose(matrix: Matrix<E>): Matrix<E> {
        return if (matrix.width < 1 || matrix.height < 1) matrix
        else createMatrix(height = matrix.width, width = matrix.height, e = matrix[0, 0]).apply {
            for (i in 0 until matrix.width) {
                for (j in 0 until matrix.height) this[i, j] = matrix[j, i]
            }
        }
    }
}

fun <E> createMatrix(height: Int, width: Int, e: E): Matrix<E> = MatrixNestedLists(height, width, e)
