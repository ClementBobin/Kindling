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
        var l = left
        var r = right
        while (l < r) {
            medianOfThreeToLeft(array, l, r, comparator)
            var start = l
            var end = r
            var current = l + 1
            val pivot = array[l]

            while (current <= end) {
                when {
                    SortUtils.less(array[current], pivot, comparator) -> {
                        SortUtils.swap(array, start, current)
                        start++
                        current++
                    }
                    SortUtils.greater(array[current], pivot, comparator) -> {
                        SortUtils.swap(array, current, end)
                        end--
                    }
                    else -> current++
                }
            }

            if (start - l < r - end) {
                quickSort(array, l, start - 1, comparator)
                l = end + 1
            } else {
                quickSort(array, end + 1, r, comparator)
                r = start - 1
            }
        }
    }

    private fun <T> medianOfThreeToLeft(
        array: Array<T>,
        left: Int,
        right: Int,
        comparator: Comparator<T>
    ) {
        val mid = left + (right - left) / 2
        if (SortUtils.less(array[mid], array[left], comparator)) SortUtils.swap(array, mid, left)
        if (SortUtils.less(array[right], array[left], comparator)) SortUtils.swap(array, right, left)
        if (SortUtils.less(array[right], array[mid], comparator)) SortUtils.swap(array, right, mid)
        SortUtils.swap(array, left, mid)
    }
}
