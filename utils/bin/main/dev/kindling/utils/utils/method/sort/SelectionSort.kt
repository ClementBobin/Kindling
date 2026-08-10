package dev.kindling.library.utils.method.sort

/**
 * Selection Sort.
 *
 * Repeatedly selects the smallest element from the unsorted
 * portion and moves it to the beginning.
 *
 * Time complexity:
 * - Best: O(n²)
 * - Average: O(n²)
 * - Worst: O(n²)
 *
 * Space complexity:
 * - O(1)
 */
object SelectionSort {

    /**
     * Sorts the array using natural ordering.
     *
     * @param array array to sort
     */
    fun <T : Comparable<T>> sort(array: Array<T>) {
        sort(array, naturalOrder())
    }

    /**
     * Sorts the array using the provided comparator.
     *
     * @param array array to sort
     * @param comparator comparator defining ordering
     */
    fun <T> sort(
        array: Array<T>,
        comparator: Comparator<T>
    ) {
        for (i in array.indices) {
            var min = i

            for (j in i + 1 until array.size) {
                if (SortUtils.less(array[j], array[min], comparator)) {
                    min = j
                }
            }

            if (min != i) {
                SortUtils.swap(array, i, min)
            }
        }
    }
}