package dev.kindling.utils.method.sort

/**
 * Quick Sort.
 *
 * A divide-and-conquer algorithm that selects a pivot element and
 * partitions the array around it, recursively sorting each partition.
 *
 * Pivot selection uses median-of-three to reduce the probability of
 * hitting the O(n²) worst case on already-sorted or reversed input.
 *
 * Time complexity : O(n log n) average, O(n²) worst case
 * Space complexity: O(log n) stack space
 *
 * The [partition] methods are `internal` so that [IntroSort] can
 * reuse them directly rather than duplicating the logic.
 */
object QuickSort {

    /**
     * Sorts an [IntArray] in ascending order.
     *
     * @param array array to sort
     */
    fun sort(array: IntArray) {
        quickSort(array, 0, array.size - 1)
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
        quickSort(array, 0, array.size - 1, comparator)
    }

    private fun quickSort(array: IntArray, low: Int, high: Int) {
        if (low >= high) return
        val p = partition(array, low, high)
        quickSort(array, low, p - 1)
        quickSort(array, p + 1, high)
    }

    private fun <T> quickSort(array: Array<T>, low: Int, high: Int, comparator: Comparator<T>) {
        if (low >= high) return
        val p = partition(array, low, high, comparator)
        quickSort(array, low, p - 1, comparator)
        quickSort(array, p + 1, high, comparator)
    }

    /**
     * Partitions `array[low.high]` around a median-of-three pivot and
     * returns the final index of the pivot element.
     *
     * Exposed as `internal` so [IntroSort] can reuse this partitioning
     * step without duplicating it.
     *
     * @param array array to partition
     * @param low   inclusive lower bound of the partition
     * @param high  inclusive upper bound of the partition
     * @return      index of the pivot after partitioning
     */
    internal fun partition(array: IntArray, low: Int, high: Int): Int {
        val mid = low + (high - low) / 2
        if (array[mid] < array[low])  { val t = array[mid];  array[mid]  = array[low];  array[low]  = t }
        if (array[high] < array[low]) { val t = array[high]; array[high] = array[low];  array[low]  = t }
        if (array[mid] < array[high]) { val t = array[mid];  array[mid]  = array[high]; array[high] = t }

        val pivot = array[high]
        var j = low

        for (i in low until high) {
            if (array[i] < pivot) {
                val temp = array[i]; array[i] = array[j]; array[j] = temp
                j++
            }
        }

        val temp = array[high]; array[high] = array[j]; array[j] = temp
        return j
    }

    /**
     * Partitions `array[low.high]` around a median-of-three pivot using
     * the given comparator, and returns the final index of the pivot element.
     *
     * Exposed as `internal` so [IntroSort] can reuse this partitioning
     * step without duplicating it.
     *
     * @param array      array to partition
     * @param low        inclusive lower bound of the partition
     * @param high       inclusive upper bound of the partition
     * @param comparator comparator to determine order
     * @return           index of the pivot after partitioning
     */
    internal fun <T> partition(array: Array<T>, low: Int, high: Int, comparator: Comparator<T>): Int {
        val mid = low + (high - low) / 2
        if (SortUtils.less(array[mid], array[low], comparator))   SortUtils.swap(array, mid, low)
        if (SortUtils.less(array[high], array[low], comparator))  SortUtils.swap(array, high, low)
        if (SortUtils.less(array[mid], array[high], comparator))  SortUtils.swap(array, mid, high)

        val pivot = array[high]
        var j = low

        for (i in low until high) {
            if (SortUtils.less(array[i], pivot, comparator)) {
                SortUtils.swap(array, i, j)
                j++
            }
        }

        SortUtils.swap(array, high, j)
        return j
    }
}