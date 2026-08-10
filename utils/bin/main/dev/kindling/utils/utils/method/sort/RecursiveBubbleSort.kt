package dev.kindling.library.utils.method.sort

/**
 * Recursive Bubble Sort.
 *
 * Bubble Sort implemented using recursion.
 *
 * Time complexity:
 * - Best: O(n)
 * - Average: O(n²)
 * - Worst: O(n²)
 *
 * Space complexity:
 * - O(n) due to recursion stack
 */
object RecursiveBubbleSort {

    /**
     * Sorts the array using natural ordering.
     *
     * @param array array to sort
     */
    fun <T : Comparable<T>> sort(array: Array<T>) {
        sort(array, naturalOrder())
    }

    /**
     * Sorts the array using a comparator.
     *
     * @param array array to sort
     * @param comparator comparator defining ordering
     */
    fun <T> sort(
        array: Array<T>,
        comparator: Comparator<T>
    ) {
        bubbleSort(array, array.size, comparator)
    }

    private fun <T> bubbleSort(array: Array<T>, n: Int, comparator: Comparator<T>) {
        if (n <= 1) return
        if (!BubbleSort.singlePass(array, n - 1, comparator)) return // early-exit
        bubbleSort(array, n - 1, comparator)
    }
}