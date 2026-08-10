package dev.kindling.utils.method.sort

/**
 * Gnome Sort.
 *
 * Based on the technique used by the standard Dutch Garden Gnome.
 * Looks at the current and previous element; if they are in the right
 * order it steps forward, otherwise it swaps them and steps backward.
 * Boundary conditions: no previous element steps forward, no next
 * element means done.
 *
 * — "Gnome Sort - The Simplest Sort Algorithm". Dickgrune.com
 */
object GnomeSort {

    /**
     * Sorts an IntArray in ascending order.
     *
     * @param array array to sort
     */
    fun sort(array: IntArray) {
        var pos = 1

        while (pos < array.size) {
            if (array[pos - 1] <= array[pos]) {
                pos++
            } else {
                val temp = array[pos - 1]
                array[pos - 1] = array[pos]
                array[pos] = temp

                if (--pos == 0) pos = 1
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
        var pos = 1

        while (pos < array.size) {
            if (!SortUtils.greater(array[pos - 1], array[pos], comparator)) {
                pos++
            } else {
                SortUtils.swap(array, pos - 1, pos)

                if (--pos == 0) pos = 1
            }
        }
    }
}