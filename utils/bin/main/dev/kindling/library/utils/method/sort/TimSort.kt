package dev.kindling.library.utils.method.sort

/**
 * Tim Sort.
 *
 * Hybrid sorting algorithm derived from Merge Sort and Insertion Sort.
 * It identifies already sorted runs and merges them efficiently.
 *
 * Time complexity:
 * - Best: O(n)
 * - Average: O(n log n)
 * - Worst: O(n log n)
 *
 * Space complexity:
 * - O(n)
 */
object TimSort {

    private const val MIN_RUN = 32

    /**
     * Sorts using natural ordering.
     */
    fun <T : Comparable<T>> sort(array: Array<T>) = sort(array, naturalOrder())

    /**
     * Sorts using the provided comparator.
     */
    fun <T> sort(array: Array<T>, comparator: Comparator<T>) {
        val n = array.size
        if (n < 2) return

        // Phase 1 — insertion sort each run of size MIN_RUN
        var start = 0
        while (start < n) {
            val end = minOf(start + MIN_RUN - 1, n - 1)
            InsertionSort.sortRange(array, start, end, comparator)
            start += MIN_RUN
        }

        // Phase 2 — merge runs bottom-up
        var size = MIN_RUN
        while (size < n) {
            var left = 0
            while (left < n) {
                val mid   = minOf(left + size - 1, n - 1)
                val right = minOf(left + size * 2 - 1, n - 1)
                if (mid < right) MergeSort.merge(array, left, mid, right, comparator)
                left += size * 2
            }
            size *= 2
        }
    }
}