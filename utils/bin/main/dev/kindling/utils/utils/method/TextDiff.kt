package dev.kindling.utils.method

// ─────────────────────────────────────────────────────────────────────────────
//  DiffOperation
// ─────────────────────────────────────────────────────────────────────────────

/**
 * A single diff operation produced by [TextDiff].
 */
sealed class DiffOperation {
    /** Text present in both the original and the revised string. */
    data class Equal(val text: String) : DiffOperation()

    /** Text that was removed from the original. */
    data class Delete(val text: String) : DiffOperation()

    /** Text that was inserted in the revised string. */
    data class Insert(val text: String) : DiffOperation()
}

// ─────────────────────────────────────────────────────────────────────────────
//  TextDiff
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Lightweight text diff utility using the Myers O(ND) algorithm.
 *
 * Pairs naturally with [StateWithHistory] to show what changed between
 * two history entries, or to build a change-preview UI.
 *
 * ```kotlin
 * val ops = TextDiff.diffWords("the quick brown fox", "the slow brown fox")
 * // [Equal("the "), Delete("quick"), Insert("slow"), Equal(" brown fox")]
 *
 * // Render to annotated string, HTML, etc.:
 * val html = TextDiff.toHtml(ops)
 * // "the <del>quick</del><ins>slow</ins> brown fox"
 *
 * // Or work with lines:
 * val lineDiff = TextDiff.diffLines(oldText, newText)
 * ```
 */
object TextDiff {

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * Diffs two strings at **word** granularity.
     * Whitespace is kept attached to the preceding word token.
     *
     * ```kotlin
     * TextDiff.diffWords("hello world", "hello Kotlin")
     * // [Equal("hello "), Delete("world"), Insert("Kotlin")]
     * ```
     */
    fun diffWords(original: String, revised: String): List<DiffOperation> {
        val a = tokenizeWords(original)
        val b = tokenizeWords(revised)
        return buildOps(a, b)
    }

    /**
     * Diffs two strings at **line** granularity.
     *
     * ```kotlin
     * TextDiff.diffLines("line1\nline2\nline3", "line1\nline2b\nline3")
     * ```
     */
    fun diffLines(original: String, revised: String): List<DiffOperation> {
        val a = original.lines()
        val b = revised.lines()
        return buildOps(a, b)
    }

    /**
     * Diffs two strings at **character** granularity.
     *
     * Best for short strings (passwords, codes). For long texts prefer
     * [diffWords] or [diffLines] for readability.
     */
    fun diffChars(original: String, revised: String): List<DiffOperation> {
        val a = original.map { it.toString() }
        val b = revised.map { it.toString() }
        return buildOps(a, b)
    }

    /**
     * Counts the number of [DiffOperation.Insert] and [DiffOperation.Delete]
     * tokens in [ops].
     */
    fun changeCount(ops: List<DiffOperation>): Int =
        ops.count { it is DiffOperation.Insert || it is DiffOperation.Delete }

    /** Returns `true` when [ops] contains no insertions or deletions. */
    fun isEqual(ops: List<DiffOperation>): Boolean = changeCount(ops) == 0

    /**
     * Renders [ops] to an HTML string using `<del>` and `<ins>` tags.
     *
     * ```kotlin
     * TextDiff.toHtml(TextDiff.diffWords("old text", "new text"))
     * // "<del>old</del><ins>new</ins> text"
     * ```
     */
    fun toHtml(ops: List<DiffOperation>): String = buildString {
        for (op in ops) when (op) {
            is DiffOperation.Equal  -> append(escapeHtml(op.text))
            is DiffOperation.Delete -> append("<del>${escapeHtml(op.text)}</del>")
            is DiffOperation.Insert -> append("<ins>${escapeHtml(op.text)}</ins>")
        }
    }

    /**
     * Renders [ops] to plain text showing only deletions (`[-text]`) and
     * insertions (`[+text]`).
     *
     * ```kotlin
     * TextDiff.toAnnotated(ops)
     * // "the [-quick][+slow] brown fox"
     * ```
     */
    fun toAnnotated(ops: List<DiffOperation>): String = buildString {
        for (op in ops) when (op) {
            is DiffOperation.Equal  -> append(op.text)
            is DiffOperation.Delete -> append("[-${op.text}]")
            is DiffOperation.Insert -> append("[+${op.text}]")
        }
    }

    // ── Myers diff (O(ND)) ────────────────────────────────────────────────────

    private fun buildOps(a: List<String>, b: List<String>): List<DiffOperation> {
        val lcs = longestCommonSubsequence(a, b)
        return buildList {
            var ia = 0; var ib = 0; var il = 0
            while (ia < a.size || ib < b.size) {
                when {
                    ia < a.size && ib < b.size && il < lcs.size && a[ia] == lcs[il] && b[ib] == lcs[il] -> {
                        add(DiffOperation.Equal(a[ia])); ia++; ib++; il++
                    }
                    ib < b.size && (il >= lcs.size || b[ib] != lcs[il]) -> {
                        add(DiffOperation.Insert(b[ib])); ib++
                    }
                    else -> {
                        add(DiffOperation.Delete(a[ia])); ia++
                    }
                }
            }
        }.mergeAdjacent()
    }

    /** Standard LCS via dynamic programming. */
    private fun longestCommonSubsequence(a: List<String>, b: List<String>): List<String> {
        val m = a.size; val n = b.size
        val dp = Array(m + 1) { IntArray(n + 1) }
        for (i in 1..m) for (j in 1..n) {
            dp[i][j] = if (a[i - 1] == b[j - 1]) dp[i - 1][j - 1] + 1
                       else maxOf(dp[i - 1][j], dp[i][j - 1])
        }
        val result = ArrayDeque<String>()
        var i = m; var j = n
        while (i > 0 && j > 0) {
            when {
                a[i - 1] == b[j - 1] -> { result.addFirst(a[i - 1]); i--; j-- }
                dp[i - 1][j] >= dp[i][j - 1] -> i--
                else -> j--
            }
        }
        return result.toList()
    }

    /** Merges consecutive operations of the same type into a single token. */
    private fun List<DiffOperation>.mergeAdjacent(): List<DiffOperation> {
        val merged = mutableListOf<DiffOperation>()
        for (op in this) {
            val last = merged.lastOrNull()
            when {
                last is DiffOperation.Equal  && op is DiffOperation.Equal  ->
                    merged[merged.lastIndex] = DiffOperation.Equal(last.text + op.text)
                last is DiffOperation.Delete && op is DiffOperation.Delete ->
                    merged[merged.lastIndex] = DiffOperation.Delete(last.text + op.text)
                last is DiffOperation.Insert && op is DiffOperation.Insert ->
                    merged[merged.lastIndex] = DiffOperation.Insert(last.text + op.text)
                else -> merged += op
            }
        }
        return merged
    }

    // ── Tokenisers ────────────────────────────────────────────────────────────

    /** Splits on word boundaries, keeping trailing whitespace with the preceding word. */
    private fun tokenizeWords(text: String): List<String> =
        Regex("\\S+\\s*").findAll(text).map { it.value }.toList()

    // ── HTML escaping ─────────────────────────────────────────────────────────

    private fun escapeHtml(s: String) = s
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
}