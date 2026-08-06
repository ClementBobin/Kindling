package dev.kindling.library.utils.method.sort

/**
 * Merge Sort.
 *
 * A divide-and-conquer algorithm that recursively splits the array
 * in half, sorts each half, then merges them back together.
 * Guarantees O(n log n) time in all cases with O(n) extra space.
 */
object MergeSort {

    /**
     * Sorts an IntArray in ascending order.
     *
     * @param array array to sort
     */
    fun sort(array: IntArray) {
        mergeSort(array, 0, array.size - 1)
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
        mergeSort(array, 0, array.size - 1, comparator)
    }

    private fun mergeSort(array: IntArray, low: Int, high: Int) {
        if (low >= high) return
        val mid = low + (high - low) / 2
        mergeSort(array, low, mid)
        mergeSort(array, mid + 1, high)
        merge(array, low, mid, high)
    }

    private fun <T> mergeSort(array: Array<T>, low: Int, high: Int, comparator: Comparator<T>) {
        if (low >= high) return
        val mid = low + (high - low) / 2
        mergeSort(array, low, mid, comparator)
        mergeSort(array, mid + 1, high, comparator)
        merge(array, low, mid, high, comparator)
    }

    private fun merge(array: IntArray, low: Int, mid: Int, high: Int) {
        val left = array.copyOfRange(low, mid + 1)
        val right = array.copyOfRange(mid + 1, high + 1)

        var i = 0; var j = 0; var k = low

        while (i < left.size && j < right.size)
            array[k++] = if (left[i] <= right[j]) left[i++] else right[j++]

        while (i < left.size) array[k++] = left[i++]
        while (j < right.size) array[k++] = right[j++]
    }

    /**
     * Merges two adjacent sorted subarrays [low.mid] and [mid+1.high].
     * Used internally by TimSort.
     */
    internal fun <T> merge(array: Array<T>, low: Int, mid: Int, high: Int, comparator: Comparator<T>) {
        val left  = array.copyOfRange(low, mid + 1)
        val right = array.copyOfRange(mid + 1, high + 1)
        var i = 0; var j = 0; var k = low
        while (i < left.size && j < right.size)
            array[k++] = if (!SortUtils.greater(left[i], right[j], comparator)) left[i++] else right[j++]
        while (i < left.size) array[k++] = left[i++]
        while (j < right.size) array[k++] = right[j++]
    }
}