package dev.kindling.utils.method.sort

/**
 * Bucket Sort.
 *
 * Distributes elements into buckets, sorts each bucket,
 * then concatenates them into the final sorted array.
 */
object BucketSort {

    /** Maximum range supported for IntArray bucket sort to avoid excessive allocation. */
    const val MAX_RANGE = 1_000_000

    /**
     * Sorts an IntArray using counting-style bucket sort.
     *
     * @param array array to sort
     */
    fun sort(array: IntArray) {
        if (array.size <= 1) return

        val min = array.min()
        val max = array.max()
        val range = max.toLong() - min.toLong() + 1

        require(range <= MAX_RANGE) { "Range $range exceeds MAX_RANGE $MAX_RANGE" }

        val buckets = IntArray(range.toInt())

        for (value in array) buckets[value - min]++

        var outPos = 0
        for (i in buckets.indices)
            repeat(buckets[i]) { array[outPos++] = i + min }
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

        val bucketCount = array.size
        val buckets = Array<MutableList<T>>(bucketCount) { mutableListOf() }

        // Distribute into buckets by rank
        val min = array.minWith(comparator)!!
        val max = array.maxWith(comparator)!!

        for (element in array) {
            val index = getBucketIndex(element, min, max, bucketCount, comparator)
            buckets[index].add(element)
        }

        // Sort each bucket and concatenate
        var outPos = 0
        for (bucket in buckets) {
            bucket.sortWith(comparator)
            for (element in bucket) array[outPos++] = element
        }
    }

    private fun <T> getBucketIndex(
        element: T,
        min: T,
        max: T,
        bucketCount: Int,
        comparator: Comparator<T>
    ): Int {
        // If all elements are equal, put everything in bucket 0
        if (comparator.compare(min, max) == 0) return 0

        val range = comparator.compare(max, min).toDouble()
        val offset = comparator.compare(element, min).toDouble()

        return ((offset / range) * (bucketCount - 1)).toInt().coerceIn(0, bucketCount - 1)
    }
}