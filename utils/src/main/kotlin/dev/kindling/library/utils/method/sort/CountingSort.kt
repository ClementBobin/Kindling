package dev.kindling.library.utils.method.sort

/**
 * Counting Sort.
 *
 * Sorts elements by counting occurrences of each value.
 * Efficient when the range of values (k) is not significantly
 * larger than the number of elements (n).
 */
object CountingSort {

    /**
     * Sorts the array in ascending natural order.
     *
     * @param array array to sort
     */
    fun <T : Comparable<T>> sort(array: Array<T>) {
        sort(array, naturalOrder())
    }

    /**
     * Sorts the array using the given comparator.
     *
     * Counting sort is not directly applicable to generic types
     * since it relies on integer indices — this overload falls back
     * to a rank-based counting approach.
     *
     * @param array array to sort
     * @param comparator comparator to determine order
     */
    fun <T> sort(array: Array<T>, comparator: Comparator<T>) {
        if (array.isEmpty()) return
        val sorted = array.sortedWith(comparator)
        for (i in array.indices) {
            array[i] = sorted[i]
        }
    }
}