package ru.lord_markus.work

import java.lang.System.currentTimeMillis
import java.util.SortedMap

private const val DEFAULT_MAX_LENGTH = 3_000
private const val DEFAULT_OFFSET_WIDTH = 2
private const val ONE = 1

fun main() {
    val start = currentTimeMillis()

    intArrayOf(
        1_200, 1_200,
        554, 554,
        660,
        490,
        277, 277, 277,
        627, 627,
        296
    )
        .count()
        .forEach { result ->
            println(
                message = "${result.sum()} мм.:\t${
                    result.joinToString(separator = " + ", transform = { "$it мм." })
                }"
            )
        }
    println(message = "Время вычислений: ${currentTimeMillis() - start} мс.")
}

private fun IntArray.count(
    maxLength: Int = DEFAULT_MAX_LENGTH,
    offsetWidth: Int = DEFAULT_OFFSET_WIDTH
): Array<IntArray> {
    fun increaseDetail(
        sortedMap: SortedMap<Int, Int>,
        resulArrayList: ArrayList<Int> = arrayListOf(),
    ): Pair<SortedMap<Int, Int>, List<Int>> {
        val filteredSortedMap = sortedMap.filterKeys {
            resulArrayList.sum() + resulArrayList.size * offsetWidth + it <= maxLength
        }
        return if (filteredSortedMap.isEmpty()) sortedMap to resulArrayList.sortedDescending()
        else filteredSortedMap
            .map { (key, value) ->
                val localResultArrayList = arrayListOf<Int>().apply {
                    addAll(elements = resulArrayList + key)
                }
                val localSortedMap = sortedMapOf<Int, Int>().apply {
                    putAll(from = sortedMap)
                }
                if (value > ONE) localSortedMap[key] = value.dec()
                else localSortedMap.remove(key = key)
                increaseDetail(
                    sortedMap = localSortedMap,
                    resulArrayList = localResultArrayList
                )
            }
            .maxBy { it.second.sum() }
    }

    var map = groupBy { it }
        .mapValues { it.value.count() }
        .toSortedMap()

    val results = arrayListOf<List<Int>>()

    while (map.isNotEmpty()) {
        increaseDetail(sortedMap = map).also { (key, value) ->
            results.add(element = value)
            map = key
        }
    }
    return results
        .map { it.toIntArray() }
        .toTypedArray()
}
