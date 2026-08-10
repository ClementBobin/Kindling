package dev.kindling.utils.method.sort

internal object SortUtils {

    fun <T> swap(array: Array<T>, i: Int, j: Int) {
        val temp = array[i]
        array[i] = array[j]
        array[j] = temp
    }

    fun <T> less(
        a: T,
        b: T,
        comparator: Comparator<T>
    ): Boolean {
        return comparator.compare(a, b) < 0
    }

    fun <T> greater(
        a: T,
        b: T,
        comparator: Comparator<T>
    ): Boolean {
        return comparator.compare(a, b) > 0
    }
}