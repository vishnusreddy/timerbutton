package com.goeslocal.timerbutton

import android.os.SystemClock
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * Observable state and controls for a Compose [TimerButton].
 */
@Stable
class TimerButtonState internal constructor(
    durationMillis: Long,
    private val clock: TimerClock = TimerClock { SystemClock.elapsedRealtime() },
) {
    private val engine = TimerButtonEngine(durationMillis, clock)

    /**
     * Current progress from `0f` to `1f`.
     */
    var progress: Float by mutableStateOf(0f)
        private set

    /**
     * Remaining timer duration in milliseconds.
     */
    var remainingMillis: Long by mutableLongStateOf(engine.remainingMillis)
        private set

    /**
     * Elapsed timer duration in milliseconds.
     */
    var elapsedMillis: Long by mutableLongStateOf(0L)
        private set

    /**
     * Current timer status.
     */
    var timerState: TimerButtonStatus by mutableStateOf(TimerButtonStatus.Idle)
        private set

    val isRunning: Boolean get() = timerState == TimerButtonStatus.Running
    val isPaused: Boolean get() = timerState == TimerButtonStatus.Paused
    val isCompleted: Boolean get() = timerState == TimerButtonStatus.Completed
    val durationMillis: Long get() = engine.durationMillis

    internal var onTimerStart: () -> Unit = {}
    internal var onTick: (Long, Float) -> Unit = { _, _ -> }
    internal var onTimerComplete: () -> Unit = {}
    internal var onTimerCancel: () -> Unit = {}
    internal var onTimerPause: () -> Unit = {}
    internal var onTimerResume: () -> Unit = {}
    internal var onTimerReset: () -> Unit = {}
    internal var onTimerRestart: () -> Unit = {}
    internal var onStateChange: (TimerButtonStatus) -> Unit = {}

    fun setDuration(durationMillis: Long) {
        engine.setDuration(durationMillis)
        sync()
    }

    internal fun snapshot(): TimerButtonEngineSnapshot = engine.snapshot().also {
        sync()
    }

    internal fun restore(snapshot: TimerButtonEngineSnapshot) {
        engine.restore(snapshot)
        sync()
    }

    /**
     * Starts the timer from zero.
     */
    fun start() {
        val previous = timerState
        if (engine.start()) {
            sync()
            if (previous == TimerButtonStatus.Idle || previous == TimerButtonStatus.Cancelled) {
                onTimerStart()
            }
            dispatchStateChange()
        }
    }

    /**
     * Pauses a running timer.
     */
    fun pause() {
        if (engine.pause()) {
            sync()
            onTimerPause()
            dispatchStateChange()
        }
    }

    /**
     * Resumes a paused timer.
     */
    fun resume() {
        if (engine.resume()) {
            sync()
            onTimerResume()
            dispatchStateChange()
        }
    }

    /**
     * Cancels a running or paused timer at its current progress.
     */
    fun cancel() {
        if (engine.cancel()) {
            sync()
            onTimerCancel()
            dispatchStateChange()
        }
    }

    /**
     * Resets the timer to idle and zero progress.
     */
    fun reset() {
        if (engine.reset()) {
            sync()
            onTimerReset()
            dispatchStateChange()
        }
    }

    /**
     * Restarts the timer from zero.
     */
    fun restart() {
        if (engine.restart()) {
            sync()
            onTimerRestart()
            onTimerStart()
            dispatchStateChange()
        }
    }

    internal fun tick() {
        val previous = timerState
        if (!engine.tick()) return
        sync()
        onTick(remainingMillis, progress)
        if (previous != timerState) {
            dispatchStateChange()
        }
        if (engine.consumeCompletion()) {
            onTimerComplete()
        }
    }

    private fun sync() {
        progress = engine.progress
        remainingMillis = engine.remainingMillis
        elapsedMillis = engine.elapsedMillis
        timerState = engine.status
    }

    private fun dispatchStateChange() {
        onStateChange(timerState)
    }
}
