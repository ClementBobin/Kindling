package dev.kindling.utils.state

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * A reactive key-value map.
 *
 * Port of the `useMap` React hook.
 *
 * ```kotlin
 * val map = KMap<String, Int>()
 * map.set("apples", 3)
 * map.set("bananas", 5)
 *
 * println(map.get("apples"))  // 3
 * println(map.has("bananas")) // true
 *
 * map.remove("apples")
 * map.clear()
 *
 * // Observe reactively:
 * map.state.collect { snapshot -> render(snapshot) }
 * ```
 *
 * @param initialEntries Key-value pairs to pre-populate the map.
 */
class KMap<K, V>(initialEntries: List<Pair<K, V>> = emptyList()) {

    private val _state = MutableStateFlow(initialEntries.toMap())

    /** Immutable snapshot of the current map contents as a [StateFlow]. */
    val state: StateFlow<Map<K, V>> = _state.asStateFlow()

    /** The current map snapshot. */
    val snapshot: Map<K, V> get() = _state.value

    /** Number of entries currently in the map. */
    val size: Int get() = _state.value.size

    /** `true` when the map contains no entries. */
    val isEmpty: Boolean get() = _state.value.isEmpty()

    /** Associates [key] with [value], replacing any existing entry. */
    fun set(key: K, value: V) {
        _state.value = _state.value.toMutableMap().also { it[key] = value }.toMap()
    }

    /** Returns the value for [key], or `null` if absent. */
    fun get(key: K): V? = _state.value[key]

    /** Returns `true` when the map contains [key]. */
    fun has(key: K): Boolean = _state.value.containsKey(key)

    /** Removes the entry for [key]. No-op if absent. */
    fun remove(key: K) {
        if (!_state.value.containsKey(key)) return
        _state.value = _state.value.toMutableMap().also { it.remove(key) }.toMap()
    }

    /** Removes all entries. */
    fun clear() { _state.value = emptyMap() }

    override fun toString() = "KMap(size=$size)"
}