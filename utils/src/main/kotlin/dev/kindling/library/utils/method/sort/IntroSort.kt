package dev.kindling.library.utils.method.sort

import kotlin.math.log2

/**
 * Intro Sort.
 *
 * A hybrid sorting algorithm that combines [QuickSort], [HeapSort], and
 * [InsertionSort] to achieve O(n log n) worst-case performance while
 * keeping Quick Sort's average-case speed in practice.
 *
 * Strategy:
 * - Delegates partitioning to [QuickSort.partition] (median-of-three pivot).
 * - Falls back to [HeapSort] when the recursion depth exceeds `2 * log₂(n)`,
 *   guaranteeing O(n log n) worst case regardless of input shape.
 * - Delegates to [InsertionSort.sortRange] for partitions of 16 elements or
 *   fewer, where insertion sort's low overhead beats recursive algorithms.
 *
 * Time complexity : O(n log n) in all cases
 * Space complexity: O(log n) stack space
 */
object IntroSort {

    /** Partitions smaller than or equal to this size are sorted with insertion sort. */
    private const val INSERTION_THRESHOLD = 16

    /**
     * Sorts an [IntArray] in ascending order.
     *
     * @param array array to sort
     */
    fun sort(array: IntArray) {
        introSort(array, 0, array.size - 1, depthLimit(array.size))
    }

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
     * @param array      array to sort
     * @param comparator comparator to determine order
     */
    fun <T> sort(array: Array<T>, comparator: Comparator<T>) {
        introSort(array, 0, array.size - 1, depthLimit(array.size), comparator)
    }

    /**
     * Recursive intro sort implementation for [IntArray].
     *
     * @param array array to sort
     * @param low   inclusive lower bound of the current partition
     * @param high  inclusive upper bound of the current partition
     * @param depth remaining recursion budget before switching to heap sort
     */
    private fun introSort(array: IntArray, low: Int, high: Int, depth: Int) {
        if (high - low < 1) return
        when {
            high - low < INSERTION_THRESHOLD -> InsertionSort.sortRange(array, low, high)
            depth <= 0 -> HeapSort.sortRange(array, low, high + 1)
            else -> {
                val p = QuickSort.partition(array, low, high)
                introSort(array, low, p - 1, depth - 1)
                introSort(array, p + 1, high, depth - 1)
            }
        }
    }

    /**
     * Recursive intro sort implementation for typed arrays.
     *
     * @param array      array to sort
     * @param low        inclusive lower bound of the current partition
     * @param high       inclusive upper bound of the current partition
     * @param depth      remaining recursion budget before switching to heap sort
     * @param comparator comparator to determine order
     */
    private fun <T> introSort(array: Array<T>, low: Int, high: Int, depth: Int, comparator: Comparator<T>) {
        if (high - low < 1) return
        when {
            high - low < INSERTION_THRESHOLD -> InsertionSort.sortRange(array, low, high, comparator)
            depth <= 0 -> HeapSort.sortRange(array, low, high + 1, comparator)
            else -> {
                val p = QuickSort.partition(array, low, high, comparator)
                introSort(array, low, p - 1, depth - 1, comparator)
                introSort(array, p + 1, high, depth - 1, comparator)
            }
        }
    }

    /**
     * Computes the maximum recursion depth before falling back to [HeapSort].
     *
     * The limit `2 * log₂(n)` mirrors the threshold used by libc++ and most
     * production intro sort implementations, bounding worst-case stack depth
     * while still giving quick sort room to perform well on typical input.
     *
     * @param n number of elements to sort
     * @return  maximum permitted recursion depth
     */
    private fun depthLimit(n: Int): Int {
        if (n <= 0) return 0
        return (2 * log2(n.toDouble())).toInt()
    }
}