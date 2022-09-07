package ru.kamaz.matrix.models.core

import ru.kamaz.matrix.models.templates.MatrixNestedLists

fun <E> createMatrix(height: Int, width: Int, e: E): Matrix<E> = MatrixNestedLists(height, width, e)
