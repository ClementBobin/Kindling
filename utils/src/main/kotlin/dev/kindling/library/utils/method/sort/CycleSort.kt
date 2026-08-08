package dev.kindling.library.utils.method.sort

/**
 * Cycle Sort.
 *
 * An in-place, unstable sorting algorithm that minimizes the number
 * of memory writes. Optimal when write cost is expensive.
 * Performs O(n²) comparisons and O(n) writes in all cases.
 */
object CycleSort {

    /**
     * Sorts an IntArray in ascending order.
     *
     * @param array array to sort
     */
    fun sort(array: IntArray) {
        val n = array.size

        for (start in 0 until n - 1) {
            var item = array[start]
            var pos = start

            for (i in start + 1 until n)
                if (array[i] < item) pos++

            if (pos == start) continue

            while (item == array[pos]) pos++

            val temp = item
            item = array[pos]
            array[pos] = temp

            while (pos != start) {
                pos = start

                for (i in start + 1 until n)
                    if (array[i] < item) pos++

                while (item == array[pos]) pos++

                val temp = item
                item = array[pos]
                array[pos] = temp
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

        for (start in 0 until n - 1) {
            var item = array[start]
            var pos = start

            for (i in start + 1 until n)
                if (SortUtils.less(array[i], item, comparator)) pos++

            if (pos == start) continue

            while (comparator.compare(item, array[pos]) == 0) pos++

            val temp = item
            item = array[pos]
            array[pos] = temp

            while (pos != start) {
                pos = start

                for (i in start + 1 until n)
                    if (SortUtils.less(array[i], item, comparator)) pos++

                while (comparator.compare(item, array[pos]) == 0) pos++

                val temp = item
                item = array[pos]
                array[pos] = temp
            }
        }
    }
}