package dev.kindling.library.utils.method.sort

/**
 * Heap Sort.
 *
 * Builds a max-heap from the array, then repeatedly extracts the
 * maximum element to produce a sorted array in-place.
 *
 * Time complexity : O(n log n) in all cases
 * Space complexity: O(1)
 */
object HeapSort {

    /**
     * Sorts an [IntArray] in ascending order.
     *
     * @param array array to sort
     */
    fun sort(array: IntArray) {
        sortRange(array, 0, array.size)
    }

    /**
     * Sorts a range of an [IntArray] in-place.
     */
    fun sortRange(array: IntArray, low: Int, highExclusive: Int) {
        val n = highExclusive - low
        if (n <= 1) return
        for (i in n / 2 - 1 downTo 0) heapifyDownRange(array, i, n, low)
        for (i in n - 1 downTo 1) {
            val temp = array[low]; array[low] = array[low + i]; array[low + i] = temp
            heapifyDownRange(array, 0, i, low)
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
     * @param array      array to sort
     * @param comparator comparator to determine order
     */
    fun <T> sort(array: Array<T>, comparator: Comparator<T>) {
        sortRange(array, 0, array.size, comparator)
    }

    /**
     * Sorts a range of a typed array in-place.
     */
    fun <T> sortRange(array: Array<T>, low: Int, highExclusive: Int, comparator: Comparator<T>) {
        val n = highExclusive - low
        if (n <= 1) return
        for (i in n / 2 - 1 downTo 0) heapifyDownRange(array, i, n, low, comparator)
        for (i in n - 1 downTo 1) {
            SortUtils.swap(array, low, low + i)
            heapifyDownRange(array, 0, i, low, comparator)
        }
    }

    private fun heapifyDownRange(array: IntArray, i: Int, n: Int, offset: Int) {
        val temp = array[offset + i]
        var current = i
        var j = 2 * current + 1
        while (j < n) {
            if (j < n - 1 && array[offset + j] < array[offset + j + 1]) j++
            if (temp >= array[offset + j]) break
            array[offset + current] = array[offset + j]
            current = j
            j = 2 * current + 1
        }
        array[offset + current] = temp
    }

    private fun <T> heapifyDownRange(array: Array<T>, i: Int, n: Int, offset: Int, comparator: Comparator<T>) {
        val temp = array[offset + i]
        var current = i
        var j = 2 * current + 1
        while (j < n) {
            if (j < n - 1 && SortUtils.less(array[offset + j], array[offset + j + 1], comparator)) j++
            if (!SortUtils.less(temp, array[offset + j], comparator)) break
            array[offset + current] = array[offset + j]
            current = j
            j = 2 * current + 1
        }
        array[offset + current] = temp
    }
}