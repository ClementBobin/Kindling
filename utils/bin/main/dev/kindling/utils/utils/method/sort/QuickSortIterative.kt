package dev.kindling.utils.method.sort

/**
 * Quick Sort (Iterative).
 *
 * An iterative variant of Quick Sort that uses an explicit stack
 * instead of recursion, avoiding stack overflow on large inputs.
 * Achieves O(n log n) average-case time with O(log n) stack space.
 */
object QuickSortIterative {

    /**
     * Sorts an IntArray in ascending order.
     *
     * @param array array to sort
     */
    fun sort(array: IntArray) {
        if (array.size <= 1) return

        val stack = IntArray(128)

        stack[0] = 0
        stack[1] = array.size - 1
        var top = 1

        while (top >= 0) {
            val h = stack[top--]
            val l = stack[top--]

            if (l < h) {
                val p = QuickSort.partition(array, l, h)

                // Push larger partition first so smaller is popped first
                if (p - l < h - p) {
                    if (p + 1 < h) {
                        stack[++top] = p + 1
                        stack[++top] = h
                    }
                    if (l < p - 1) {
                        stack[++top] = l
                        stack[++top] = p - 1
                    }
                } else {
                    if (l < p - 1) {
                        stack[++top] = l
                        stack[++top] = p - 1
                    }
                    if (p + 1 < h) {
                        stack[++top] = p + 1
                        stack[++top] = h
                    }
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
        if (array.size <= 1) return

        val stack = IntArray(128)

        stack[0] = 0
        stack[1] = array.size - 1
        var top = 1

        while (top >= 0) {
            val h = stack[top--]
            val l = stack[top--]

            if (l < h) {
                val p = QuickSort.partition(array, l, h, comparator)

                if (p - l < h - p) {
                    if (p + 1 < h) {
                        stack[++top] = p + 1
                        stack[++top] = h
                    }
                    if (l < p - 1) {
                        stack[++top] = l
                        stack[++top] = p - 1
                    }
                } else {
                    if (l < p - 1) {
                        stack[++top] = l
                        stack[++top] = p - 1
                    }
                    if (p + 1 < h) {
                        stack[++top] = p + 1
                        stack[++top] = h
                    }
                }
            }
        }
    }
}
