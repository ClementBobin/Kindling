package dev.kindling.utils.method.sort

/**
 * Enumerates every sorting algorithm available in the Kindling sort library
 * and provides a unified dispatch surface via [sort].
 *
 * Each entry documents its algorithmic characteristics so callers can make
 * an informed choice without consulting individual implementation files.
 *
 * Typical usage:
 * ```kotlin
 * val array = intArrayOf(5, 3, 1, 4, 2)
 * Sorter.sort(array, SortType.INTRO)
 *
 * val strings = arrayOf("banana", "apple", "cherry")
 * Sorter.sort(strings, SortType.INTRO)
 * Sorter.sort(strings, SortType.MERGE, compareByDescending { it.name })
 * ```
 */
enum class SortType {

    // ── Comparison / exchange sorts ──────────────────────────────────────────

    /**
     * Bubble Sort — O(n²) average/worst, O(n) best.
     *
     * Repeatedly swaps adjacent elements that are out of order.
     * Simple but inefficient; included for educational purposes.
     */
    BUBBLE,

    /**
     * Recursive Bubble Sort — O(n²) average/worst, O(n) best.
     *
     * Bubble sort expressed recursively; identical complexity but
     * consumes O(n) stack space.
     */
    RECURSIVE_BUBBLE,

    /**
     * Cocktail Shaker Sort — O(n²) average/worst, O(n) best.
     *
     * Bidirectional variant of bubble sort that traverses the array
     * in both directions per pass, reducing the total number of passes
     * slightly on partially-sorted input.
     */
    COCKTAIL,

    /**
     * Comb Sort — O(n²) worst, O(n log n) average.
     *
     * Improves bubble sort by eliminating small values near the end
     * (turtles) using a shrinking gap sequence.
     */
    COMB,

    /**
     * Gnome Sort — O(n²) average/worst, O(n) best.
     *
     * Moves each element backward to its correct position like a
     * garden gnome repositioning flower pots. Simple but slow.
     */
    GNOME,

    // ── Selection sorts ──────────────────────────────────────────────────────

    /**
     * Selection Sort — O(n²) all cases.
     *
     * Finds the minimum element on each pass and places it at the
     * front. Makes exactly n−1 swaps regardless of input order.
     */
    SELECTION,

    /**
     * Cycle Sort — O(n²) all cases, O(n) writes (optimal).
     *
     * Minimises the number of writes by placing each element directly
     * in its final position. Useful when write cost dominates.
     */
    CYCLE,

    /**
     * Tournament Sort — O(n log n) all cases.
     *
     * Uses a tournament tree (winner tree) to find successive minima.
     * Similar in spirit to heap sort but with an explicit tree structure.
     */
    TOURNAMENT,

    // ── Insertion sorts ──────────────────────────────────────────────────────

    /**
     * Insertion Sort — O(n²) average/worst, O(n) best.
     *
     * Builds a sorted prefix one element at a time. Excellent for
     * small or nearly-sorted arrays; used internally by [INTRO] and [TIM].
     */
    INSERTION,

    /**
     * Binary Insertion Sort — O(n log n) comparisons, O(n²) shifts.
     *
     * Uses binary search to find the insertion point, reducing the
     * comparison count but not the shift count.
     */
    BINARY_INSERTION,

    /**
     * Shell Sort — O(n log² n) typical, gap-sequence dependent.
     *
     * Generalises insertion sort by sorting elements a gap apart,
     * progressively reducing the gap to 1. Faster than plain insertion
     * sort on larger arrays.
     */
    SHELL,

    // ── Merge sorts ──────────────────────────────────────────────────────────

    /**
     * Merge Sort — O(n log n) all cases, O(n) space.
     *
     * Divides the array in half, recursively sorts each half, and
     * merges. Stable and predictable; preferred when stability matters.
     */
    MERGE,

    /**
     * Tim Sort — O(n log n) worst, O(n) best, O(n) space.
     *
     * Hybrid of merge sort and insertion sort used in Python and Java's
     * standard libraries. Detects natural runs and merges them
     * efficiently; excellent on real-world data.
     */
    TIM,

    /**
     * Block Sort — O(n log n) worst, O(1) extra space.
     *
     * In-place merge sort variant that rearranges internal blocks to
     * avoid auxiliary allocation. Achieves stable O(n log n) with O(1)
     * space at the cost of implementation complexity.
     */
    BLOCK,

    // ── Partition / quick sorts ──────────────────────────────────────────────

    /**
     * Quick Sort — O(n log n) average, O(n²) worst, O(log n) space.
     *
     * Median-of-three pivot selection mitigates the worst case on
     * sorted or reversed input. Fast in practice due to cache locality.
     */
    QUICK,

    /**
     * Iterative Quick Sort — O(n log n) average, O(n²) worst, O(log n) space.
     *
     * Same algorithm as [QUICK] but manages the recursion stack
     * explicitly, avoiding call-stack overhead and stack-overflow risk
     * on very large arrays.
     */
    QUICK_ITERATIVE,

    /**
     * Three-Way Quick Sort — O(n log n) average, O(n) on many duplicates.
     *
     * Partitions into three regions (less, equal, greater), making it
     * optimal for arrays with many repeated elements.
     */
    THREE_WAY_QUICK,

    /**
     * Intro Sort — O(n log n) all cases, O(log n) space.
     *
     * Hybrid of [QUICK], [HEAP], and [INSERTION]. Starts with quick
     * sort, falls back to heap sort if recursion depth exceeds
     * `2 * log₂(n)`, and uses insertion sort for small partitions.
     * This is the algorithm used by `std::sort` in most C++ STLs.
     */
    INTRO,

    // ── Distribution sorts ───────────────────────────────────────────────────

    /**
     * Radix Sort — O(nk) where k is the number of digits.
     *
     * Non-comparative; processes integer keys digit by digit using a
     * stable counting sort as a subroutine. Efficient when k is small.
     */
    RADIX,

    /**
     * Counting Sort — O(n + r) where r is the value range.
     *
     * Non-comparative; counts occurrences of each value.
     * Only applicable to [IntArray] input with a bounded value range.
     *
     * **Dispatches to [CountingSort] — `Array<T>` overloads are not supported.**
     */
    COUNTING,

    /**
     * Bucket Sort — O(n + k) average, O(n²) worst.
     *
     * Distributes elements into buckets, sorts each bucket
     * individually, then concatenates. Works best on uniformly
     * distributed floating-point data.
     */
    BUCKET,

    /**
     * Pigeonhole Sort — O(n + r) where r is the value range.
     *
     * Similar to counting sort; places each element into a pigeonhole
     * indexed by its value. Only applicable to [IntArray] input.
     *
     * **Dispatches to [PigeonholeSort] — `Array<T>` overloads are not supported.**
     */
    PIGEONHOLE,

    // ── Novelty / educational sorts ──────────────────────────────────────────

    /**
     * Stalin Sort — O(n) time, O(1) space.
     *
     * "Sorts" by removing any element that is out of order.
     * The result is a sorted subsequence, not a permutation of the
     * original. Included for entertainment.
     */
    STALIN,

    /**
     * Bogo Sort — O((n+1)!) expected, unbounded worst case.
     *
     * Randomly shuffles the array until it happens to be sorted.
     * Included for educational / humour purposes only.
     * **Do not use on arrays larger than ~8 elements.**
     */
    BOGO;

    // ── Dispatch ─────────────────────────────────────────────────────────────

    fun sort(array: IntArray) {
        when (this) {
            COCKTAIL         -> CocktailSort.sort(array)
            COMB             -> CombSort.sort(array)
            GNOME            -> GnomeSort.sort(array)
            CYCLE            -> CycleSort.sort(array)
            MERGE            -> MergeSort.sort(array)
            QUICK            -> QuickSort.sort(array)
            QUICK_ITERATIVE  -> QuickSortIterative.sort(array)
            INTRO            -> IntroSort.sort(array)
            RADIX            -> RadixSort.sort(array)
            BUCKET           -> BucketSort.sort(array)
            PIGEONHOLE       -> PigeonholeSort.sort(array)
            else             -> throw UnsupportedOperationException(
                "$this sort does not support IntArray — use Array<T> overload"
            )
        }
    }

    fun <T : Comparable<T>> sort(array: Array<T>) {
        sort(array, naturalOrder())
    }

    fun <T> sort(array: Array<T>, comparator: Comparator<T>) {
        when (this) {
            BUBBLE           -> BubbleSort.sort(array, comparator)
            RECURSIVE_BUBBLE -> RecursiveBubbleSort.sort(array, comparator)
            COCKTAIL         -> CocktailSort.sort(array, comparator)
            COMB             -> CombSort.sort(array, comparator)
            GNOME            -> GnomeSort.sort(array, comparator)
            SELECTION        -> SelectionSort.sort(array, comparator)
            CYCLE            -> CycleSort.sort(array, comparator)
            TOURNAMENT       -> TournamentSort.sort(array, comparator)
            INSERTION        -> InsertionSort.sort(array, comparator)
            BINARY_INSERTION -> BinaryInsertionSort.sort(array, comparator)
            SHELL            -> ShellSort.sort(array, comparator)
            MERGE            -> MergeSort.sort(array, comparator)
            TIM              -> TimSort.sort(array, comparator)
            BLOCK            -> BlockSort.sort(array, comparator)
            QUICK            -> QuickSort.sort(array, comparator)
            QUICK_ITERATIVE  -> QuickSortIterative.sort(array, comparator)
            THREE_WAY_QUICK  -> ThreeWayQuickSort.sort(array, comparator)
            INTRO            -> IntroSort.sort(array, comparator)
            RADIX            -> RadixSort.sort(array, comparator)
            COUNTING         -> throw UnsupportedOperationException(
                "COUNTING sort does not support Array<T> — use IntArray overload"
            )
            BUCKET           -> BucketSort.sort(array, comparator)
            PIGEONHOLE       -> throw UnsupportedOperationException(
                "PIGEONHOLE sort does not support Array<T> — use IntArray overload"
            )
            STALIN           -> StalinSort.sort(array, comparator)
            BOGO             -> BogoSort.sort(array, comparator)
        }
    }
}