package dev.kindling.utils

import kotlinx.coroutines.test.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

// ─────────────────────────────────────────────────────────────────────────────
//  Counter
// ─────────────────────────────────────────────────────────────────────────────

class CounterTest {

    @Test
    fun `starts at initial value`() {
        val c = Counter(5)
        assertEquals(5, c.count)
    }

    @Test
    fun `increment increases by step`() {
        val c = Counter(0, step = 3)
        c.increment()
        assertEquals(3, c.count)
    }

    @Test
    fun `decrement decreases by step`() {
        val c = Counter(10, step = 2)
        c.decrement()
        assertEquals(8, c.count)
    }

    @Test
    fun `clamps at max`() {
        val c = Counter(9, max = 10)
        c.increment()
        c.increment()
        assertEquals(10, c.count)
    }

    @Test
    fun `clamps at min`() {
        val c = Counter(1, min = 0)
        c.decrement()
        c.decrement()
        assertEquals(0, c.count)
    }

    @Test
    fun `reset returns to initial value`() {
        val c = Counter(3)
        c.increment()
        c.increment()
        c.reset()
        assertEquals(3, c.count)
    }

    @Test
    fun `set clamps to range`() {
        val c = Counter(0, min = 0, max = 5)
        c.set(100)
        assertEquals(5, c.count)
        c.set(-10)
        assertEquals(0, c.count)
    }

    @Test
    fun `initial value is clamped`() {
        val c = Counter(50, min = 0, max = 10)
        assertEquals(10, c.count)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Toggle
// ─────────────────────────────────────────────────────────────────────────────

class ToggleTest {

    @Test
    fun `starts at default value`() {
        assertFalse(Toggle().value)
        assertTrue(Toggle(true).value)
    }

    @Test
    fun `toggle flips value`() {
        val t = Toggle(false)
        t.toggle()
        assertTrue(t.value)
        t.toggle()
        assertFalse(t.value)
    }

    @Test
    fun `set forces value`() {
        val t = Toggle(false)
        t.set(true)
        assertTrue(t.value)
        t.set(false)
        assertFalse(t.value)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  StateWithHistory
// ─────────────────────────────────────────────────────────────────────────────

class StateWithHistoryTest {

    @Test
    fun `starts with initial value`() {
        val h = StateWithHistory("hello")
        assertEquals("hello", h.value)
    }

    @Test
    fun `back and forward navigate history`() {
        val h = StateWithHistory("a")
        h.set("b")
        h.set("c")

        h.back()
        assertEquals("b", h.value)
        h.back()
        assertEquals("a", h.value)
        h.forward()
        assertEquals("b", h.value)
    }

    @Test
    fun `canUndo and canRedo are accurate`() {
        val h = StateWithHistory(0)
        assertFalse(h.canUndo)
        assertFalse(h.canRedo)

        h.set(1)
        assertTrue(h.canUndo)
        assertFalse(h.canRedo)

        h.undo()
        assertFalse(h.canUndo)
        assertTrue(h.canRedo)
    }

    @Test
    fun `setting value after undo discards forward history`() {
        val h = StateWithHistory(1)
        h.set(2)
        h.set(3)
        h.undo()          // back to 2
        h.set(99)         // branch: forward history (3) should be gone
        assertFalse(h.canRedo)
        assertEquals(99, h.value)
    }

    @Test
    fun `capacity is enforced`() {
        val h = StateWithHistory(0, capacity = 3)
        h.set(1); h.set(2); h.set(3); h.set(4)
        // History should contain at most 3 entries
        assertTrue(h.historySnapshot.size <= 3)
    }

    @Test
    fun `go jumps to correct index`() {
        val h = StateWithHistory("x")
        h.set("y")
        h.set("z")
        h.go(0)
        assertEquals("x", h.value)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  KQueue
// ─────────────────────────────────────────────────────────────────────────────

class KQueueTest {

    @Test
    fun `enqueue and dequeue in FIFO order`() {
        val q = KQueue<Int>()
        q.enqueue(1)
        q.enqueue(2)
        q.enqueue(3)
        assertEquals(1, q.dequeue())
        assertEquals(2, q.dequeue())
        assertEquals(3, q.dequeue())
    }

    @Test
    fun `dequeue on empty returns null`() {
        assertNull(KQueue<String>().dequeue())
    }

    @Test
    fun `peek does not remove`() {
        val q = KQueue<Int>()
        q.enqueue(42)
        assertEquals(42, q.peek())
        assertEquals(42, q.peek())
        assertEquals(1, q.size)
    }

    @Test
    fun `clear empties queue`() {
        val q = KQueue(listOf(1, 2, 3))
        q.clear()
        assertTrue(q.isEmpty)
        assertEquals(0, q.size)
    }

    @Test
    fun `initialItems are pre-populated`() {
        val q = KQueue(listOf("a", "b"))
        assertEquals(2, q.size)
        assertEquals("a", q.peek())
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  KMap
// ─────────────────────────────────────────────────────────────────────────────

class KMapTest {

    @Test
    fun `set and get`() {
        val m = KMap<String, Int>()
        m.set("x", 10)
        assertEquals(10, m.get("x"))
    }

    @Test
    fun `has returns correct value`() {
        val m = KMap<String, Int>()
        assertFalse(m.has("k"))
        m.set("k", 1)
        assertTrue(m.has("k"))
    }

    @Test
    fun `remove deletes entry`() {
        val m = KMap(listOf("a" to 1, "b" to 2))
        m.remove("a")
        assertFalse(m.has("a"))
        assertTrue(m.has("b"))
    }

    @Test
    fun `clear empties map`() {
        val m = KMap(listOf("a" to 1))
        m.clear()
        assertTrue(m.isEmpty)
    }

    @Test
    fun `overwrite existing key`() {
        val m = KMap<String, Int>()
        m.set("k", 1)
        m.set("k", 99)
        assertEquals(99, m.get("k"))
        assertEquals(1, m.size)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  KTimer / KInterval
// ─────────────────────────────────────────────────────────────────────────────

class KTimerTest {

    @Test
    fun `fires after delay`() = runTest {
        var fired = false
        val t = KTimer(this, 200.milliseconds) { fired = true }
        advanceTimeBy(300)
        assertTrue(fired)
    }

    @Test
    fun `clear prevents firing`() = runTest {
        var fired = false
        val t = KTimer(this, 200.milliseconds) { fired = true }
        t.clear()
        advanceTimeBy(300)
        assertFalse(fired)
    }

    @Test
    fun `reset restarts the timer`() = runTest {
        var count = 0
        val t = KTimer(this, 200.milliseconds) { count++ }
        advanceTimeBy(100)
        t.reset()
        advanceTimeBy(300)
        assertEquals(1, count)
    }
}

class KIntervalTest {

    @Test
    fun `fires repeatedly`() = runTest {
        var ticks = 0
        val i = KInterval(this, 100.milliseconds) { ticks++ }
        advanceTimeBy(350)
        assertEquals(3, ticks)
    }

    @Test
    fun `clear stops ticking`() = runTest {
        var ticks = 0
        val i = KInterval(this, 100.milliseconds) { ticks++ }
        advanceTimeBy(150)
        i.clear()
        advanceTimeBy(300)
        assertEquals(1, ticks)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  RetryRunner
// ─────────────────────────────────────────────────────────────────────────────

class RetryRunnerTest {

    @Test
    fun `succeeds on first attempt`() = runTest {
        val runner = RetryRunner<String>(this)
        val result = runner.run { "ok" }
        assertEquals("ok", result)
        assertEquals(1, runner.attempt.value)
        assertFalse(runner.isLoading.value)
    }

    @Test
    fun `retries on failure and eventually succeeds`() = runTest {
        var calls = 0
        val runner = RetryRunner<Int>(this, retries = 2, delay = 10.milliseconds)
        val result = runner.run {
            calls++
            if (calls < 3) error("not yet")
            42
        }
        assertEquals(42, result)
        assertEquals(3, calls)
    }

    @Test
    fun `throws after exhausting retries`() = runTest {
        val runner = RetryRunner<Int>(this, retries = 1, delay = 10.milliseconds)
        val ex = runCatching { runner.run { error("always fails") } }.exceptionOrNull()
        assertNotNull(ex)
    }

    @Test
    fun `reset clears state`() = runTest {
        val runner = RetryRunner<String>(this)
        runner.run { "x" }
        runner.reset()
        assertNull(runner.value.value)
        assertEquals(0, runner.attempt.value)
    }

    @Test
    fun `onSuccess callback is invoked`() = runTest {
        var received: String? = null
        val runner = RetryRunner<String>(this, onSuccess = { received = it })
        runner.run { "hello" }
        assertEquals("hello", received)
    }
}