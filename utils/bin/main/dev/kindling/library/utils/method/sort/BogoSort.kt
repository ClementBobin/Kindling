package dev.kindling.library.utils.method.sort

import kotlin.random.Random

/**
 * Bogo Sort.
 *
 * Randomly shuffles the array until it becomes sorted.
 *
 * WARNING:
 * Extremely inefficient. Intended only for demonstration/testing.
 */
object BogoSort {

    /**
     * Sorts the array using Bogo Sort.
     *
     * @param array array to sort
     */
    fun <T : Comparable<T>> sort(array: Array<T>) {
        while (!isSorted(array)) {
            shuffle(array)
        }
    }

    fun <T> sort(array: Array<T>, comparator: Comparator<T>) {
        while (!isSorted(array, comparator)) shuffle(array)
    }

    private fun <T : Comparable<T>> isSorted(array: Array<T>): Boolean {
        for (i in 0 until array.size - 1) {
            if (array[i] > array[i + 1]) {
                return false
            }
        }
        return true
    }

    private fun <T> isSorted(array: Array<T>, comparator: Comparator<T>): Boolean {
        for (i in 0 until array.size - 1) {
            if (SortUtils.greater(array[i], array[i + 1], comparator)) return false
        }
        return true
    }

    private fun <T> shuffle(array: Array<T>) {
        for (i in array.indices) {
            val randomIndex = Random.nextInt(array.size)

            val temp = array[i]
            array[i] = array[randomIndex]
            array[randomIndex] = temp
        }
    }
}