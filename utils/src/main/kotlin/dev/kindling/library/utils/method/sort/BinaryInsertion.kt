package dev.kindling.library.utils.method.sort

/**
 * Binary Insertion Sort.
 *
 * Uses binary search to find the insertion position,
 * reducing comparisons compared to normal insertion sort.
 */
object BinaryInsertion {

    /**
     * Sorts the array in ascending natural order.
     *
     * @param array array to sort
     */
    fun <T : Comparable<T>> sort(array: Array<T>) {
        val n = array.size

        for (i in 1 until n) {
            val value = array[i]

            var low = 0
            var high = i

            // Find insertion index using binary search
            while (low < high) {
                val mid = low + (high - low) / 2

                if (value < array[mid]) {
                    high = mid
                } else {
                    low = mid + 1
                }
            }

            // Shift elements right
            for (j in i downTo low + 1) {
                array[j] = array[j - 1]
            }

            array[low] = value
        }
    }

    fun <T> sort(array: Array<T>, comparator: Comparator<T>) {
        val n = array.size
        for (i in 1 until n) {
            val value = array[i]
            var low = 0
            var high = i

            while (low < high) {
                val mid = low + (high - low) / 2
                // replace `value < array[mid]` with:
                if (SortUtils.less(value, array[mid], comparator)) high = mid
                else low = mid + 1
            }

            for (j in i downTo low + 1) array[j] = array[j - 1]
            array[low] = value
        }
    }
}