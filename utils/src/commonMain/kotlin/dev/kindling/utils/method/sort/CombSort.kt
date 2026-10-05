package dev.kindling.utils.method.sort

/**
 * Comb Sort.
 *
 * An improvement over Bubble Sort that eliminates turtles (small values
 * near the end) by using a shrinking gap between compared elements.
 * Gap shrinks by a factor of 1.3 each pass until it reaches 1,
 * at which point it behaves like Bubble Sort.
 */
object CombSort {

    private const val SHRINK_FACTOR = 1.3

    /**
     * Sorts an IntArray in ascending order.
     *
     * @param array array to sort
     */
    fun sort(array: IntArray) {
        val n = array.size
        var gap = n
        var swapped = true

        while (gap != 1 || swapped) {
            gap = nextGap(gap)
            swapped = false

            for (i in 0 until n - gap) {
                if (array[i] > array[i + gap]) {
                    val temp = array[i]
                    array[i] = array[i + gap]
                    array[i + gap] = temp
                    swapped = true
                }
            }
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
        val n = array.size
        var gap = n
        var swapped = true

        while (gap != 1 || swapped) {
            gap = nextGap(gap)
            swapped = false

            for (i in 0 until n - gap) {
                if (SortUtils.greater(array[i], array[i + gap], comparator)) {
                    SortUtils.swap(array, i, i + gap)
                    swapped = true
                }
            }
        }
    }

    private fun nextGap(gap: Int): Int {
        val next = (gap / SHRINK_FACTOR).toInt()
        return if (next < 1) 1 else next
    }
}