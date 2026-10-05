package dev.kindling.utils.method.sort

/**
 * Bubble Sort.
 *
 * Repeatedly swaps adjacent elements if they are in the wrong order.
 */
object BubbleSort {

    /**
     * Sorts the array in ascending natural order.
     *
     * @param array array to sort
     */
    fun <T : Comparable<T>> sort(array: Array<T>) {
        val n = array.size

        for (i in 0 until n - 1) {
            var swapped = false

            for (j in 0 until n - i - 1) {
                if (array[j] > array[j + 1]) {
                    SortUtils.swap(array, j, j + 1)
                    swapped = true
                }
            }

            // Already sorted
            if (!swapped) {
                break
            }
        }
    }

    fun <T> sort(array: Array<T>, comparator: Comparator<T>) {
        val n = array.size
        for (i in 0 until n - 1) {
            var swapped = false
            for (j in 0 until n - i - 1) {
                if (SortUtils.greater(array[j], array[j + 1], comparator)) {
                    SortUtils.swap(array, j, j + 1)
                    swapped = true
                }
            }
            if (!swapped) break
        }
    }

    /** Single pass up to [limit]. Returns true if any swap occurred. */
    internal fun <T> singlePass(array: Array<T>, limit: Int, comparator: Comparator<T>): Boolean {
        var swapped = false
        for (j in 0 until limit) {
            if (SortUtils.greater(array[j], array[j + 1], comparator)) {
                SortUtils.swap(array, j, j + 1)
                swapped = true
            }
        }
        return swapped
    }
}