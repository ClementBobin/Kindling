package dev.kindling.utils.method.sort

/**
 * Radix Sort.
 *
 * A non-comparative sorting algorithm that sorts integers by
 * processing individual digits from least significant to most
 * significant. Uses counting sort as a stable subroutine per digit.
 * Achieves O(nk) time where k is the number of digits in the max value.
 *
 * Note: this implementation handles non-negative integers only.
 * For generic types, elements are sorted by their natural or
 * comparator-defined rank, falling back to comparison-based counting.
 */
object RadixSort {

    /**
     * Sorts an IntArray in ascending order.
     *
     * @param array array to sort
     */
    fun sort(array: IntArray) {
        if (array.isEmpty()) return
        require(array.all { it >= 0 }) { "Radix sort only supports non-negative integers." }

        val max = array.max()

        var exp = 1
        while (max / exp > 0) {
            countingSortByDigit(array, exp)
            exp *= 10
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
        if (array.isEmpty()) return
        val sorted = array.sortedWith(comparator)
        for (i in array.indices) {
            array[i] = sorted[i]
        }
    }

    private fun countingSortByDigit(array: IntArray, exp: Int) {
        val n = array.size
        val output = IntArray(n)
        val count = IntArray(10)

        for (value in array) count[value / exp % 10]++
        for (i in 1 until 10) count[i] += count[i - 1]
        for (i in n - 1 downTo 0) {
            val digit = array[i] / exp % 10
            output[count[digit] - 1] = array[i]
            count[digit]--
        }

        output.copyInto(array)
    }
}