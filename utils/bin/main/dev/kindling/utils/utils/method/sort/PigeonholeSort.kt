package dev.kindling.utilstils.method.sort

/**
 * Pigeonhole Sort.
 *
 * Works by creating "pigeonholes" (buckets) for each value in the range [min, max],
 * placing each element into its corresponding hole, then collecting them back in order.
 *
 * Only applicable to integer-like types with a bounded, reasonably small range.
 *
 * Time complexity:  O(n + range)
 * Space complexity: O(range)
 */
object PigeonholeSort {

    /** Maximum range supported for pigeonhole sort to avoid excessive allocation. */
    const val MAX_RANGE = 1_000_000

    fun sort(array: IntArray) {
        if (array.size < 2) return

        val min = array.min()
        val max = array.max()
        val rangeLong = max.toLong() - min.toLong() + 1
        require(rangeLong <= MAX_RANGE) { "Range $rangeLong exceeds MAX_RANGE $MAX_RANGE" }
        val range = rangeLong.toInt()

        val holes = IntArray(range)
        for (v in array) holes[v - min]++

        var i = 0
        for (h in 0 until range) {
            repeat(holes[h]) { array[i++] = h + min }
        }
    }

    /**
     * Generic overload for any type mappable to an Int key.
     * The [key] lambda extracts the integer rank used for bucketing.
     *
     * Example:
     * ```
     * PigeonholeSort.sort(people) { it.age }
     * ```
     */
    fun <T> sort(array: Array<T>, key: (T) -> Int): Array<T> {
        if (array.size < 2) return array

        val keys = IntArray(array.size) { key(array[it]) }
        val min  = keys.min()
        val max  = keys.max()
        val rangeLong = max.toLong() - min.toLong() + 1
        require(rangeLong <= MAX_RANGE) { "Range $rangeLong exceeds MAX_RANGE $MAX_RANGE" }
        val range = rangeLong.toInt()

        val holes = Array<MutableList<T>>(range) { mutableListOf() }
        for (i in array.indices) holes[keys[i] - min].add(array[i])

        var i = 0
        for (hole in holes) {
            for (v in hole) array[i++] = v
        }
        return array
    }

    fun <T> sort(list: MutableList<T>, key: (T) -> Int): MutableList<T> {
        if (list.size < 2) return list

        val keys  = IntArray(list.size) { key(list[it]) }
        val min   = keys.min()
        val max   = keys.max()
        val rangeLong = max.toLong() - min.toLong() + 1
        require(rangeLong <= MAX_RANGE) { "Range $rangeLong exceeds MAX_RANGE $MAX_RANGE" }
        val range = rangeLong.toInt()

        val holes = Array<MutableList<T>>(range) { mutableListOf() }
        for (i in list.indices) holes[keys[i] - min].add(list[i])

        var i = 0
        for (hole in holes) {
            for (v in hole) list[i++] = v
        }
        return list
    }
}