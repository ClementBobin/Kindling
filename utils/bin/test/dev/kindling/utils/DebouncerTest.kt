package dev.kindling.library.utils

import dev.kindling.library.utils.method.Debouncer
import dev.kindling.library.utils.method.Throttler
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import kotlin.time.Duration.Companion.milliseconds

class DebouncerTest {

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `trailing debouncer emits last value after quiet period`() = runTest {
        val debouncer = Debouncer<String>(this, delay = 300.milliseconds)
        val results = mutableListOf<String>()
        debouncer.onDebounced { results.add(it) }

        debouncer.emit("a")
        debouncer.emit("b")
        debouncer.emit("c")

        advanceTimeBy(400.milliseconds)   // past the 300 ms window
        assertEquals(listOf("c"), results)
        debouncer.cancel()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `debouncer emits nothing before quiet period ends`() = runTest {
        val debouncer = Debouncer<String>(this, delay = 300.milliseconds)
        val results = mutableListOf<String>()
        debouncer.onDebounced { results.add(it) }

        debouncer.emit("x")
        advanceTimeBy(100.milliseconds)   // quiet period not elapsed

        assertTrue(results.isEmpty())
        debouncer.cancel()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `multiple quiet windows produce multiple emissions`() = runTest {
        val debouncer = Debouncer<Int>(this, delay = 200.milliseconds)
        val results = mutableListOf<Int>()
        debouncer.onDebounced { results.add(it) }

        debouncer.emit(1)
        advanceTimeBy(300.milliseconds)   // first window
        debouncer.emit(2)
        advanceTimeBy(300.milliseconds)   // second window

        assertEquals(listOf(1, 2), results)
        debouncer.cancel()
    }
}

class ThrottlerTest {

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `throttler emits first value immediately`() = runTest {
        val throttler = Throttler<Int>(this, period = 500.milliseconds)
        val results = mutableListOf<Int>()
        throttler.onThrottled { results.add(it) }

        throttler.emit(1)
        advanceTimeBy(10.milliseconds)

        assertEquals(listOf(1), results)
        throttler.cancel()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `throttler suppresses values within period`() = runTest {
        val throttler = Throttler<Int>(this, period = 500.milliseconds)
        val results = mutableListOf<Int>()
        throttler.onThrottled { results.add(it) }

        throttler.emit(1)
        advanceTimeBy(100.milliseconds)
        throttler.emit(2)   // should be suppressed
        advanceTimeBy(100.milliseconds)
        throttler.emit(3)   // should be suppressed

        advanceTimeBy(350.milliseconds)  // total 550ms since first emit
        throttler.emit(4)   // should be accepted
        advanceTimeBy(10.milliseconds)

        assertEquals(listOf(1, 4), results)
        throttler.cancel()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `throttler allows emission after period`() = runTest {
        val throttler = Throttler<Int>(this, period = 500.milliseconds)
        val results = mutableListOf<Int>()
        throttler.onThrottled { results.add(it) }

        throttler.emit(1)
        advanceTimeBy(600.milliseconds)   // past period
        throttler.emit(2)
        advanceTimeBy(50.milliseconds)

        assertEquals(listOf(1, 2), results)
        throttler.cancel()
    }
}
