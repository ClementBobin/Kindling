package dev.kindling.library.utils.method.sort

/**
 * Facade for the Kindling sort library.
 *
 * Provides a unified entry point for all sorting algorithms, delegating
 * to [SortType] for dispatch. Prefer this over calling algorithm objects
 * directly when the sort strategy should be a runtime parameter.
 *
 * ```kotlin
 * Sorter.sort(array, SortType.INTRO)
 * Sorter.sort(array, SortType.MERGE, compareByDescending { it.name })
 * Sorter.sort(intArray, SortType.RADIX)
 * ```
 */
object Sorter {

    /**
     * Sorts [array] in ascending order using the given [type].
     *
     * @param array array to sort
     * @param type  sorting algorithm to use
     */
    fun sort(array: IntArray, type: SortType) {
        type.sort(array)
    }

    /**
     * Sorts [array] in ascending natural order using the given [type].
     *
     * @param array array to sort
     * @param type  sorting algorithm to use
     */
    fun <T : Comparable<T>> sort(array: Array<T>, type: SortType) {
        type.sort(array)
    }

    /**
     * Sorts [array] using the given [comparator] and [type].
     *
     * @param array      array to sort
     * @param type       sorting algorithm to use
     * @param comparator comparator to determine order
     */
    fun <T> sort(array: Array<T>, type: SortType, comparator: Comparator<T>) {
        type.sort(array, comparator)
    }
}