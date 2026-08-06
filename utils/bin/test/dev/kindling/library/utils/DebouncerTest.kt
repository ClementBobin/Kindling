package dev.kindling.utils

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import kotlin.time.Duration.Companion.milliseconds

class DebouncerTest {

    @Test
    fun `trailing debouncer emits last value after quiet period`() = runTest {
        val debouncer = Debouncer<String>(this, delay = 300.milliseconds)
        val results = mutableListOf<String>()
        debouncer.onDebounced { results.add(it) }

        debouncer.emit("a")
        debouncer.emit("b")
        debouncer.emit("c")

        advanceTimeBy(400)   // past the 300 ms window
        assertEquals(listOf("c"), results)
    }

    @Test
    fun `debouncer emits nothing before quiet period ends`() = runTest {
        val debouncer = Debouncer<String>(this, delay = 300.milliseconds)
        val results = mutableListOf<String>()
        debouncer.onDebounced { results.add(it) }

        debouncer.emit("x")
        advanceTimeBy(100)   // quiet period not elapsed

        assertTrue(results.isEmpty())
    }

    @Test
    fun `multiple quiet windows produce multiple emissions`() = runTest {
        val debouncer = Debouncer<Int>(this, delay = 200.milliseconds)
        val results = mutableListOf<Int>()
        debouncer.onDebounced { results.add(it) }

        debouncer.emit(1)
        advanceTimeBy(300)   // first window
        debouncer.emit(2)
        advanceTimeBy(300)   // second window

        assertEquals(listOf(1, 2), results)
    }
}

class ThrottlerTest {

    @Test
    fun `throttler emits first value immediately`() = runTest {
        val throttler = Throttler<Int>(this, period = 500.milliseconds)
        val results = mutableListOf<Int>()
        throttler.onThrottled { results.add(it) }

        throttler.emit(1)
        advanceTimeBy(10)

        assertEquals(listOf(1), results)
    }

    @Test
    fun `throttler suppresses values within period`() = runTest {
        val throttler = Throttler<Int>(this, period = 500.milliseconds)
        val results = mutableListOf<Int>()
        throttler.onThrottled { results.add(it) }

        throttler.emit(1)
        advanceTimeBy(100)
        throttler.emit(2)   // should be suppressed
        advanceTimeBy(100)
        throttler.emit(3)   // should be suppressed

        advanceTimeBy(50)
        assertEquals(listOf(1), results)
    }

    @Test
    fun `throttler allows emission after period`() = runTest {
        val throttler = Throttler<Int>(this, period = 500.milliseconds)
        val results = mutableListOf<Int>()
        throttler.onThrottled { results.add(it) }

        throttler.emit(1)
        advanceTimeBy(600)   // past period
        throttler.emit(2)
        advanceTimeBy(50)

        assertEquals(listOf(1, 2), results)
    }
}
