package com.goeslocal.timerbutton

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TimerButtonEngineTest {
    private class FakeClock(var time: Long = 0L) : TimerClock {
        override fun nowMillis(): Long = time
        fun advance(millis: Long) {
            time += millis
        }
    }

    @Test
    fun startInitializesRunningTimer() {
        val clock = FakeClock()
        val engine = TimerButtonEngine(1_000L, clock)

        assertTrue(engine.start())

        assertEquals(TimerButtonStatus.Running, engine.status)
        assertEquals(0L, engine.elapsedMillis)
        assertEquals(1_000L, engine.remainingMillis)
    }

    @Test
    fun progressReachesOneAndCompletionIsConsumedOnce() {
        val clock = FakeClock()
        val engine = TimerButtonEngine(1_000L, clock)

        engine.start()
        clock.advance(2_000L)
        engine.tick()

        assertEquals(1f, engine.progress)
        assertEquals(TimerButtonStatus.Completed, engine.status)
        assertTrue(engine.consumeCompletion())
        assertFalse(engine.consumeCompletion())
    }

    @Test
    fun cancelStopsRunningTimer() {
        val clock = FakeClock()
        val engine = TimerButtonEngine(1_000L, clock)

        engine.start()
        clock.advance(300L)
        assertTrue(engine.cancel())
        clock.advance(300L)
        engine.tick()

        assertEquals(TimerButtonStatus.Cancelled, engine.status)
        assertEquals(300L, engine.elapsedMillis)
    }

    @Test
    fun pauseFreezesProgressUntilResume() {
        val clock = FakeClock()
        val engine = TimerButtonEngine(1_000L, clock)

        engine.start()
        clock.advance(400L)
        assertTrue(engine.pause())
        clock.advance(400L)
        engine.tick()

        assertEquals(400L, engine.elapsedMillis)
        assertTrue(engine.resume())
        clock.advance(100L)
        engine.tick()
        assertEquals(500L, engine.elapsedMillis)
    }

    @Test
    fun resetReturnsToIdle() {
        val clock = FakeClock()
        val engine = TimerButtonEngine(1_000L, clock)

        engine.start()
        clock.advance(500L)
        engine.tick()
        assertTrue(engine.reset())

        assertEquals(TimerButtonStatus.Idle, engine.status)
        assertEquals(0L, engine.elapsedMillis)
        assertEquals(0f, engine.progress)
    }

    @Test
    fun restartStartsFromZero() {
        val clock = FakeClock()
        val engine = TimerButtonEngine(1_000L, clock)

        engine.start()
        clock.advance(800L)
        engine.tick()
        assertTrue(engine.restart())

        assertEquals(TimerButtonStatus.Running, engine.status)
        assertEquals(0L, engine.elapsedMillis)
    }

    @Test
    fun progressIsAlwaysClamped() {
        val clock = FakeClock()
        val engine = TimerButtonEngine(1_000L, clock)

        assertEquals(0f, engine.progress)
        engine.start()
        clock.advance(5_000L)
        engine.tick()
        assertEquals(1f, engine.progress)
    }

    @Test
    fun multipleInstancesDoNotInterfere() {
        val clock = FakeClock()
        val first = TimerButtonEngine(1_000L, clock)
        val second = TimerButtonEngine(2_000L, clock)

        first.start()
        second.start()
        clock.advance(1_000L)
        first.tick()
        second.tick()

        assertEquals(1f, first.progress)
        assertEquals(0.5f, second.progress)
    }
}

