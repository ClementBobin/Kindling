package dev.kindling.utilstils.method.sort

/**
 * Cocktail Shaker Sort.
 *
 * A bidirectional variant of Bubble Sort that traverses the array
 * in both directions alternately, moving large elements right and
 * small elements left in the same pass.
 */
object CocktailSort {

    /**
     * Sorts an IntArray in ascending order.
     *
     * @param array array to sort
     */
    fun sort(array: IntArray) {
        var start = 0
        var end = array.size - 1

        while (start < end) {
            var swapped = false

            for (i in start until end) {
                if (array[i] > array[i + 1]) {
                    val temp = array[i]
                    array[i] = array[i + 1]
                    array[i + 1] = temp
                    swapped = true
                }
            }
            end--

            for (i in end downTo start + 1) {
                if (array[i] < array[i - 1]) {
                    val temp = array[i]
                    array[i] = array[i - 1]
                    array[i - 1] = temp
                    swapped = true
                }
            }
            start++

            if (!swapped) break
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
        var start = 0
        var end = array.size - 1

        while (start < end) {
            var swapped = false

            for (i in start until end) {
                if (SortUtils.greater(array[i], array[i + 1], comparator)) {
                    SortUtils.swap(array, i, i + 1)
                    swapped = true
                }
            }
            end--

            for (i in end downTo start + 1) {
                if (SortUtils.less(array[i], array[i - 1], comparator)) {
                    SortUtils.swap(array, i, i - 1)
                    swapped = true
                }
            }
            start++

            if (!swapped) break
        }
    }
}