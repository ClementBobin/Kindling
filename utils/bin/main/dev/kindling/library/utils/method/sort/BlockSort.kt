package dev.kindling.library.utils.method.sort

/**
 * Block Sort (Wiki Sort).
 *
 * A merge-based sorting algorithm that achieves O(n log n) time with O(1) auxiliary space.
 * Works by dividing the array into blocks, sorting them, then merging using internal buffers.
 *
 * Time complexity:  O(n log n) worst case
 * Space complexity: O(1) auxiliary space (in-place)
 */
object BlockSort {

    fun <T : Comparable<T>> sort(array: Array<T>) = sort(array, naturalOrder())

    fun <T> sort(array: Array<T>, comparator: Comparator<T>) {
        val n = array.size
        if (n < 2) return
        if (n < 16) { InsertionSort.sortRange(array, 0, n - 1, comparator); return }

        var width = 1
        while (width < n) {
            var i = 0
            while (i < n) {
                val left  = i
                val mid   = minOf(i + width - 1, n - 1)
                val right = minOf(i + 2 * width - 1, n - 1)
                if (mid < right) mergeInPlace(array, left, mid, right, comparator)
                i += 2 * width
            }
            width *= 2
        }
    }

    // ── In-place merges (propres à Block, pas extractibles) ──────────────────

    private fun <T> mergeInPlace(
        array: Array<T>, left: Int, mid: Int, right: Int, comparator: Comparator<T>
    ) {
        var start2 = mid + 1
        if (comparator.compare(array[mid], array[start2]) <= 0) return
        var start1 = left
        while (start1 <= mid && start2 <= right) {
            if (comparator.compare(array[start1], array[start2]) <= 0) { start1++ } else {
                val value = array[start2]; var index = start2
                while (index != start1) { array[index] = array[index - 1]; index-- }
                array[start1] = value; start1++; start2++
            }
        }
    }
}