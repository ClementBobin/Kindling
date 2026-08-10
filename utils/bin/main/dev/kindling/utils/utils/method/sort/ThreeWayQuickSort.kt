package dev.kindling.library.utils.method.sort

/**
 * Three-Way Quick Sort.
 *
 * Optimized Quick Sort that handles arrays with many duplicate
 * values efficiently by partitioning into three sections:
 *
 * - Less than pivot
 * - Equal to pivot
 * - Greater than pivot
 *
 * Time complexity:
 * - Best: O(n log n)
 * - Average: O(n log n)
 * - Worst: O(n²)
 *
 * Space complexity:
 * - O(log n) recursion stack
 */
object ThreeWayQuickSort {

    /**
     * Sorts using natural ordering.
     *
     * @param array array to sort
     */
    fun <T : Comparable<T>> sort(array: Array<T>) {
        sort(array, naturalOrder())
    }

    /**
     * Sorts using a comparator.
     *
     * @param array array to sort
     * @param comparator comparator defining ordering
     */
    fun <T> sort(
        array: Array<T>,
        comparator: Comparator<T>
    ) {
        quickSort(
            array,
            0,
            array.size - 1,
            comparator
        )
    }

    private fun <T> quickSort(
        array: Array<T>,
        left: Int,
        right: Int,
        comparator: Comparator<T>
    ) {
        if (left >= right) return

        var start = left
        var end = right
        var current = left

        val pivot = array[left]

        while (current <= end) {

            when {
                SortUtils.less(
                    array[current],
                    pivot,
                    comparator
                ) -> {
                    SortUtils.swap(array, start, current)
                    start++
                    current++
                }

                SortUtils.greater(
                    array[current],
                    pivot,
                    comparator
                ) -> {
                    SortUtils.swap(array, current, end)
                    end--
                }

                else -> {
                    current++
                }
            }
        }

        quickSort(
            array,
            left,
            start - 1,
            comparator
        )

        quickSort(
            array,
            end + 1,
            right,
            comparator
        )
    }
}