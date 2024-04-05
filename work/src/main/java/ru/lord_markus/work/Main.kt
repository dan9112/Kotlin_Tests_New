package ru.lord_markus.work

import java.lang.System.currentTimeMillis
import java.util.SortedMap

private const val DEFAULT_MAX_VALUE = 3000
private const val DEFAULT_OFFSET = 2
private const val ONE = 1

fun main() {
    var map = listOf(
        1200, 1200,
        554, 554,
        660,
        490,
        277, 277, 277,
        627, 627,
        296
    )
        .groupingBy { it }
        .eachCount()
        .toSortedMap()

    val results = arrayListOf<IntArray>()

    val start = currentTimeMillis()
    while (map.isNotEmpty()) {
        removeKey(sortedMap = map).also { (key, value) ->
            results.add(element = value)
            println(
                message = "${value.sum()} мм.:\t${
                    value.joinToString(
                        separator = " + ",
                        transform = { "$it мм." }
                    )
                }"
            )
            map = key
        }
    }
    println(message = "Время вычислений: ${currentTimeMillis() - start} мс.")
}

private fun removeKey(
    sortedMap: SortedMap<Int, Int>,
    resulArrayList: ArrayList<Int> = arrayListOf(),
    maxValue: Int = DEFAULT_MAX_VALUE,
    offset: Int = DEFAULT_OFFSET
): Pair<SortedMap<Int, Int>, IntArray> {
    val filteredSortedMap = sortedMap.filterKeys {
        resulArrayList.sum() + resulArrayList.size * offset + it <= maxValue
    }
    return if (filteredSortedMap.isEmpty()) sortedMap to resulArrayList
        .sortedDescending()
        .toIntArray()
    else filteredSortedMap
        .map { (key, value) ->
            val localResultArrayList = arrayListOf<Int>().apply {
                addAll(elements = resulArrayList)
                add(element = key)
            }
            val localSortedMap = sortedMapOf<Int, Int>().apply {
                putAll(from = sortedMap)
            }
            if (value > ONE) localSortedMap[key] = value.dec()
            else localSortedMap.remove(key)
            removeKey(
                sortedMap = localSortedMap,
                resulArrayList = localResultArrayList
            )
        }
        .maxBy { it.second.sum() }
}
