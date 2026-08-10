package dev.kindling.utils.method.sort

/**
 * Counting Sort.
 *
 * Sorts elements by counting occurrences of each value.
 * Efficient when the range of values (k) is not significantly
 * larger than the number of elements (n).
 */
object CountingSort {

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
     * Counting sort is not directly applicable to generic types
     * since it relies on integer indices — this overload falls back
     * to a rank-based counting approach.
     *
     * @param array array to sort
     * @param comparator comparator to determine order
     */
    fun <T> sort(array: Array<T>, comparator: Comparator<T>) {
        if (array.isEmpty()) return

        // Assign each unique element a rank, then count by rank
        val sorted = array.sortedWith(comparator)
        val rankMap = LinkedHashMap<T, Int>()
        var rank = 0
        for (element in sorted) {
            if (element !in rankMap) rankMap[element] = rank++
        }

        val count = IntArray(rank)
        for (element in array) count[rankMap[element]!!]++
        for (i in 1 until rank) count[i] += count[i - 1]

        val output = array.copyOf()
        for (i in array.size - 1 downTo 0) {
            val r = rankMap[array[i]]!!
            output[count[r] - 1] = array[i]
            count[r]--
        }

        output.copyInto(array)
    }
}