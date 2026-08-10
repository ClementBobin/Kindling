package dev.kindling.library.utils.method.sort

/**
 * Tournament Sort.
 *
 * Uses a tournament tree to repeatedly select the smallest element.
 *
 * Time complexity:
 * - Best: O(n²)
 * - Average: O(n²)
 * - Worst: O(n²)
 *
 * Space complexity:
 * - O(n)
 */
object TournamentSort {

    /**
     * Sorts using natural ordering.
     */
    fun <T : Comparable<T>> sort(array: Array<T>) {
        sort(array, naturalOrder())
    }

    /**
     * Sorts using the provided comparator.
     */
    fun <T> sort(
        array: Array<T>,
        comparator: Comparator<T>
    ) {
        if (array.size <= 1) return

        val tree = TournamentTree(array, comparator)

        for (i in array.indices) {
            array[i] = tree.pop()
        }
    }


    private class TournamentTree<T>(
        array: Array<T>,
        private val comparator: Comparator<T>
    ) {
        private val snapshot = array.copyOf()
        private val tree: IntArray = IntArray(array.size * 4)
        private val n = array.size

        init {
            build(0, n - 1, 1)
        }

        fun pop(): T {
            val index = tree[1]
            val result = snapshot[index]
            update(1, 0, n - 1, index)
            return result
        }

        private fun build(left: Int, right: Int, node: Int) {
            if (left == right) {
                tree[node] = left
                return
            }

            val mid = left + (right - left) / 2
            build(left, mid, node * 2)
            build(mid + 1, right, node * 2 + 1)

            tree[node] = compare(tree[node * 2], tree[node * 2 + 1])
        }

        private fun update(node: Int, left: Int, right: Int, removed: Int) {
            if (left == right) {
                tree[node] = Int.MAX_VALUE
                return
            }

            val mid = left + (right - left) / 2
            if (removed <= mid) {
                update(node * 2, left, mid, removed)
            } else {
                update(node * 2 + 1, mid + 1, right, removed)
            }

            tree[node] = compare(tree[node * 2], tree[node * 2 + 1])
        }

        private fun compare(a: Int, b: Int): Int {
            if (a == Int.MAX_VALUE) return b
            if (b == Int.MAX_VALUE) return a

            return if (comparator.compare(snapshot[a], snapshot[b]) <= 0) a else b
        }
    }
}
