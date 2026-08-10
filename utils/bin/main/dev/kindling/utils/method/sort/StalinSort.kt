package dev.kindling.utils.method.sort

/**
 * Stalin Sort.
 *
 * Removes any element that is out of order — rather than sorting them,
 * it simply "purges" them. The result is a sorted array, but potentially shorter.
 *
 * Time complexity: O(n)
 * Space complexity: O(n) for the filtered result
 *
 * WARNING: Destructive — elements that break ordering are permanently removed.
 */
object StalinSort {

    /** Sorts (and purges) a generic array using natural ordering. */
    fun <T : Comparable<T>> sort(array: Array<T>): List<T> {
        if (array.isEmpty()) return emptyList()
        val result = mutableListOf(array[0])
        for (i in 1 until array.size) {
            if (array[i] >= result.last()) result.add(array[i])
        }
        return result
    }

    /** Sorts (and purges) a generic array using a custom comparator. */
    fun <T> sort(array: Array<T>, comparator: Comparator<T>): List<T> {
        if (array.isEmpty()) return emptyList()
        val result = mutableListOf(array[0])
        for (i in 1 until array.size) {
            if (comparator.compare(array[i], result.last()) >= 0) result.add(array[i])
        }
        return result
    }
}