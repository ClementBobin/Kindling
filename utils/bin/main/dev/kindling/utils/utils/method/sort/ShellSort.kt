package dev.kindling.library.utils.method.sort

/**
 * Shell Sort.
 *
 * Generalization of insertion sort that allows exchanges
 * of elements far apart using a gap sequence.
 *
 * Time complexity:
 * - Depends on gap sequence
 * - Average: around O(n log² n)
 * - Worst: O(n²)
 *
 * Space complexity:
 * - O(1)
 */
object ShellSort {

    /**
     * Sorts the array using natural ordering.
     *
     * @param array array to sort
     */
    fun <T : Comparable<T>> sort(array: Array<T>) {
        sort(array, naturalOrder())
    }

    /**
     * Sorts the array using the provided comparator.
     *
     * @param array array to sort
     * @param comparator comparator defining ordering
     */
    fun <T> sort(array: Array<T>, comparator: Comparator<T>) {
        val n = array.size

        // Knuth gap sequence: 1, 4, 13, 40…
        var gap = 1
        while (gap < n / 3) gap = gap * 3 + 1

        while (gap >= 1) {
            InsertionSort.insertWithGap(array, gap, comparator)
            gap /= 3
        }
    }
}