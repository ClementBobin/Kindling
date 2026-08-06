package dev.kindling.library.utils.method.sort

/**
 * Insertion Sort.
 *
 * Builds the sorted array one element at a time by shifting
 * each element leftward into its correct position.
 * Efficient for small or nearly-sorted arrays.
 *
 * Time complexity : O(n²) worst/average, O(n) best (nearly sorted)
 * Space complexity: O(1)
 */
object InsertionSort {

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
        insertWithGap(array, 1, comparator)
    }

    /**
     * Insertion sort pass with a given [gap].
     *
     * A gap of 1 is standard insertion sort.
     * A gap greater than 1 is used internally by [ShellSort].
     *
     * @param array      array to sort
     * @param gap        gap between compared elements
     * @param comparator comparator to determine order
     */
    internal fun <T> insertWithGap(array: Array<T>, gap: Int, comparator: Comparator<T>) {
        val n = array.size
        for (i in gap until n) {
            val value = array[i]
            var j = i
            while (j >= gap && SortUtils.greater(array[j - gap], value, comparator)) {
                array[j] = array[j - gap]
                j -= gap
            }
            array[j] = value
        }
    }

    /**
     * Insertion sort pass with a given [gap] on an [IntArray].
     *
     * A gap of 1 is standard insertion sort.
     * A gap greater than 1 is used internally by [ShellSort].
     *
     * @param array array to sort
     * @param gap   gap between compared elements
     */
    internal fun insertWithGap(array: IntArray, gap: Int) {
        val n = array.size
        for (i in gap until n) {
            val value = array[i]
            var j = i
            while (j >= gap && array[j - gap] > value) {
                array[j] = array[j - gap]
                j -= gap
            }
            array[j] = value
        }
    }

    /**
     * Sorts the subarray `array[left..right]` in-place using the given comparator.
     *
     * Used internally by [IntroSort], [TimSort], and [BlockSort] to handle
     * small partitions without allocating a new array.
     *
     * @param array      array containing the subrange to sort
     * @param left       inclusive start index of the subrange
     * @param right      inclusive end index of the subrange
     * @param comparator comparator to determine order
     */
    internal fun <T> sortRange(array: Array<T>, left: Int, right: Int, comparator: Comparator<T>) {
        for (i in left + 1..right) {
            val value = array[i]
            var j = i
            while (j >= left + 1 && SortUtils.greater(array[j - 1], value, comparator)) {
                array[j] = array[j - 1]
                j--
            }
            array[j] = value
        }
    }

    /**
     * Sorts the subarray `array[left..right]` in-place.
     *
     * Used internally by [IntroSort] to handle small [IntArray] partitions
     * without allocating a new array.
     *
     * @param array array containing the subrange to sort
     * @param left  inclusive start index of the subrange
     * @param right inclusive end index of the subrange
     */
    internal fun sortRange(array: IntArray, left: Int, right: Int) {
        for (i in left + 1..right) {
            val value = array[i]
            var j = i
            while (j > left && array[j - 1] > value) {
                array[j] = array[j - 1]
                j--
            }
            array[j] = value
        }
    }
}