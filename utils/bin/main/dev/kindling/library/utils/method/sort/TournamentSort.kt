package dev.kindling.library.utils.method.sort

/**
 * Tournament Sort.
 *
 * Uses a tournament tree to repeatedly select the smallest element.
 *
 * Time complexity:
 * - Best: O(n log n)
 * - Average: O(n log n)
 * - Worst: O(n log n)
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
        private val array: Array<T>,
        private val comparator: Comparator<T>
    ) {

        private val tree: IntArray = IntArray(array.size * 2)
        private var root: Int

        init {
            root = build(0, array.lastIndex, 1)
        }

        fun pop(): T {
            val index = winner(root)
            root = rebuild(root)

            return array[index]
        }


        private fun build(
            left: Int,
            right: Int,
            node: Int
        ): Int {

            if (left == right) {
                tree[node] = left
                return node
            }

            val mid = left + (right - left) / 2

            val leftNode = build(
                left,
                mid,
                node * 2
            )

            val rightNode = build(
                mid + 1,
                right,
                node * 2 + 1
            )

            tree[node] = compare(
                tree[leftNode],
                tree[rightNode]
            )

            return node
        }


        private fun rebuild(node: Int): Int {
            val index = tree[node]

            update(
                node,
                index
            )

            return node
        }


        private fun update(
            node: Int,
            removed: Int
        ) {
            if (node >= tree.size) return

            if (tree[node] == removed) {
                tree[node] = Int.MAX_VALUE
            }

            val left = node * 2
            val right = node * 2 + 1

            if (left < tree.size) {
                update(left, removed)
                update(right, removed)

                tree[node] = compare(
                    tree[left],
                    tree[right]
                )
            }
        }


        private fun winner(node: Int): Int {
            return tree[node]
        }


        private fun compare(
            a: Int,
            b: Int
        ): Int {

            if (a == Int.MAX_VALUE) return b
            if (b == Int.MAX_VALUE) return a

            return if (
                comparator.compare(
                    array[a],
                    array[b]
                ) <= 0
            ) {
                a
            } else {
                b
            }
        }
    }
}