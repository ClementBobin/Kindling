package dev.kindling.utilstils.method.sort

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
        val n = array.size
        for (i in n / 2 - 1 downTo 0) heapifyDown(array, i, n)
        for (i in n - 1 downTo 1) {
            val temp = array[0]; array[0] = array[i]; array[i] = temp
            heapifyDown(array, 0, i)
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
        val n = array.size
        for (i in n / 2 - 1 downTo 0) heapifyDown(array, i, n, comparator)
        for (i in n - 1 downTo 1) {
            SortUtils.swap(array, 0, i)
            heapifyDown(array, 0, i, comparator)
        }
    }

    /**
     * Sifts the element at index [i] downward to restore the max-heap property
     * over `array[0..n)`.
     *
     * @param array array representing the heap
     * @param i     index of the element to sift down
     * @param n     heap size (exclusive upper bound)
     */
    private fun heapifyDown(array: IntArray, i: Int, n: Int) {
        val temp = array[i]
        var j = 2 * i + 1
        while (j < n) {
            if (j < n - 1 && array[j] < array[j + 1]) j++
            if (temp >= array[j]) break
            array[(j - 1) / 2] = array[j]
            j = 2 * j + 1
        }
        array[(j - 1) / 2] = temp
    }

    /**
     * Sifts the element at index [i] downward to restore the max-heap property
     * over `array[0..n)` using the given comparator.
     *
     * @param array      array representing the heap
     * @param i          index of the element to sift down
     * @param n          heap size (exclusive upper bound)
     * @param comparator comparator to determine order
     */
    private fun <T> heapifyDown(array: Array<T>, i: Int, n: Int, comparator: Comparator<T>) {
        val temp = array[i]
        var j = 2 * i + 1
        while (j < n) {
            if (j < n - 1 && SortUtils.less(array[j], array[j + 1], comparator)) j++
            if (!SortUtils.less(temp, array[j], comparator)) break
            array[(j - 1) / 2] = array[j]
            j = 2 * j + 1
        }
        array[(j - 1) / 2] = temp
    }
}