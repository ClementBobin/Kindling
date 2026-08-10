package dev.kindling.utils.method.sort

import java.util.Deque
import java.util.ArrayDeque

/**
 * Quick Sort (Iterative).
 *
 * An iterative variant of Quick Sort that uses an explicit stack
 * instead of recursion, avoiding stack overflow on large inputs.
 * Achieves O(n log n) average-case time with O(log n) stack space.
 * Uses median-of-three pivot selection to mitigate worst-case O(n²).
 */
object QuickSortIterative {

    /**
     * Sorts an IntArray in ascending order.
     *
     * @param array array to sort
     */
    fun sort(array: IntArray) {
        if (array.size <= 1) return

        val stack: Deque<Int> = ArrayDeque()
        stack.push(0)
        stack.push(array.size - 1)

        while (stack.isNotEmpty()) {
            val high = stack.pop()
            val low = stack.pop()

            if (low >= high) continue

            val p = partition(array, low, high)

            if (p - 1 > low) { stack.push(low); stack.push(p - 1) }
            if (p + 1 < high) { stack.push(p + 1); stack.push(high) }
        }
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
     * @param array array to sort
     * @param comparator comparator to determine order
     */
    fun <T> sort(array: Array<T>, comparator: Comparator<T>) {
        if (array.size <= 1) return

        val stack: Deque<Int> = ArrayDeque()
        stack.push(0)
        stack.push(array.size - 1)

        while (stack.isNotEmpty()) {
            val high = stack.pop()
            val low = stack.pop()

            if (low >= high) continue

            val p = partition(array, low, high, comparator)

            if (p - 1 > low) { stack.push(low); stack.push(p - 1) }
            if (p + 1 < high) { stack.push(p + 1); stack.push(high) }
        }
    }

    private fun partition(array: IntArray, low: Int, high: Int): Int {
        val mid = low + (high - low) / 2
        // Median-of-three pivot selection
        if (array[mid] < array[low]) { val t = array[mid]; array[mid] = array[low]; array[low] = t }
        if (array[high] < array[low]) { val t = array[high]; array[high] = array[low]; array[low] = t }
        if (array[mid] < array[high]) { val t = array[mid]; array[mid] = array[high]; array[high] = t }

        val pivot = array[high]
        var i = low - 1

        for (j in low until high) {
            if (array[j] <= pivot) {
                i++
                val temp = array[i]; array[i] = array[j]; array[j] = temp
            }
        }

        val temp = array[i + 1]; array[i + 1] = array[high]; array[high] = temp
        return i + 1
    }

    private fun <T> partition(array: Array<T>, low: Int, high: Int, comparator: Comparator<T>): Int {
        val mid = low + (high - low) / 2
        // Median-of-three pivot selection
        if (SortUtils.less(array[mid], array[low], comparator)) SortUtils.swap(array, mid, low)
        if (SortUtils.less(array[high], array[low], comparator)) SortUtils.swap(array, high, low)
        if (SortUtils.less(array[mid], array[high], comparator)) SortUtils.swap(array, mid, high)

        val pivot = array[high]
        var i = low - 1

        for (j in low until high) {
            if (!SortUtils.greater(array[j], pivot, comparator)) {
                i++
                SortUtils.swap(array, i, j)
            }
        }

        SortUtils.swap(array, i + 1, high)
        return i + 1
    }
}