package dev.kindling.library.utils.method.sort

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
     * Falls back to a rank-based radix approach: assigns each unique
     * element a rank under the comparator, then sorts by rank digits.
     *
     * @param array array to sort
     * @param comparator comparator to determine order
     */
    fun <T> sort(array: Array<T>, comparator: Comparator<T>) {
        if (array.isEmpty()) return

        // Assign integer ranks to each element under the comparator
        val sorted = array.sortedWith(comparator)
        val rankMap = LinkedHashMap<T, Int>()
        var rank = 0
        for (element in sorted) {
            if (element !in rankMap) rankMap[element] = rank++
        }

        val ranks = IntArray(array.size) { rankMap[array[it]]!! }
        val max = ranks.max()

        var exp = 1
        while (max / exp > 0) {
            countingSortByDigit(array, ranks, exp)
            exp *= 10
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

    private fun <T> countingSortByDigit(array: Array<T>, ranks: IntArray, exp: Int) {
        val n = array.size
        val outputArr = arrayOfNulls<Any>(n)
        val outputRanks = IntArray(n)
        val count = IntArray(10)

        for (r in ranks) count[r / exp % 10]++
        for (i in 1 until 10) count[i] += count[i - 1]
        for (i in n - 1 downTo 0) {
            val digit = ranks[i] / exp % 10
            outputArr[count[digit] - 1] = array[i]
            outputRanks[count[digit] - 1] = ranks[i]
            count[digit]--
        }

        @Suppress("UNCHECKED_CAST")
        (outputArr as Array<T>).copyInto(array)
        outputRanks.copyInto(ranks)
    }
}