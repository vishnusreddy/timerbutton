package com.goeslocal.timerbutton

/**
 * Monotonic clock abstraction used by [TimerButtonEngine].
 */
internal fun interface TimerClock {
    fun nowMillis(): Long
}

/**
 * Testable timer state machine based on elapsed real time instead of decrementing ticks.
 */
internal class TimerButtonEngine(
    durationMillis: Long,
    private val clock: TimerClock,
) {
    var durationMillis: Long = durationMillis.coerceAtLeast(1L)
        private set

    var status: TimerButtonStatus = TimerButtonStatus.Idle
        private set

    var elapsedMillis: Long = 0L
        private set

    val remainingMillis: Long
        get() = (durationMillis - elapsedMillis).coerceIn(0L, durationMillis)

    val progress: Float
        get() = (elapsedMillis.toFloat() / durationMillis.toFloat()).coerceIn(0f, 1f)

    private var startedAtMillis = 0L
    private var pausedAtElapsedMillis = 0L
    private var completionDelivered = false

    internal fun snapshot(): TimerButtonEngineSnapshot {
        tick()
        return TimerButtonEngineSnapshot(
            durationMillis = durationMillis,
            status = status,
            elapsedMillis = elapsedMillis,
            startedAtMillis = startedAtMillis,
            pausedAtElapsedMillis = pausedAtElapsedMillis,
            completionDelivered = completionDelivered,
        )
    }

    internal fun restore(snapshot: TimerButtonEngineSnapshot) {
        durationMillis = snapshot.durationMillis.coerceAtLeast(1L)
        startedAtMillis = snapshot.startedAtMillis
        pausedAtElapsedMillis = snapshot.pausedAtElapsedMillis.coerceIn(0L, durationMillis)
        completionDelivered = snapshot.completionDelivered
        status = snapshot.status
        elapsedMillis = snapshot.elapsedMillis.coerceIn(0L, durationMillis)

        if (status == TimerButtonStatus.Running) {
            tick()
        }
        if (elapsedMillis >= durationMillis) {
            elapsedMillis = durationMillis
            status = TimerButtonStatus.Completed
        }
    }

    fun setDuration(durationMillis: Long) {
        this.durationMillis = durationMillis.coerceAtLeast(1L)
        if (status == TimerButtonStatus.Idle || status == TimerButtonStatus.Cancelled) {
            elapsedMillis = 0L
            completionDelivered = false
        } else {
            tick()
        }
    }

    fun start(): Boolean {
        if (status == TimerButtonStatus.Running) return false
        elapsedMillis = 0L
        pausedAtElapsedMillis = 0L
        startedAtMillis = clock.nowMillis()
        completionDelivered = false
        status = TimerButtonStatus.Running
        return true
    }

    fun pause(): Boolean {
        if (status != TimerButtonStatus.Running) return false
        tick()
        pausedAtElapsedMillis = elapsedMillis
        status = TimerButtonStatus.Paused
        return true
    }

    fun resume(): Boolean {
        if (status != TimerButtonStatus.Paused) return false
        startedAtMillis = clock.nowMillis() - pausedAtElapsedMillis
        status = TimerButtonStatus.Running
        return true
    }

    fun cancel(): Boolean {
        if (status != TimerButtonStatus.Running && status != TimerButtonStatus.Paused) return false
        tick()
        status = TimerButtonStatus.Cancelled
        return true
    }

    fun reset(): Boolean {
        val changed = status != TimerButtonStatus.Idle || elapsedMillis != 0L
        elapsedMillis = 0L
        pausedAtElapsedMillis = 0L
        completionDelivered = false
        status = TimerButtonStatus.Idle
        return changed
    }

    fun restart(): Boolean {
        elapsedMillis = 0L
        pausedAtElapsedMillis = 0L
        startedAtMillis = clock.nowMillis()
        completionDelivered = false
        status = TimerButtonStatus.Running
        return true
    }

    fun tick(): Boolean {
        if (status != TimerButtonStatus.Running) return false
        elapsedMillis = (clock.nowMillis() - startedAtMillis).coerceIn(0L, durationMillis)
        if (elapsedMillis >= durationMillis) {
            status = TimerButtonStatus.Completed
        }
        return true
    }

    fun consumeCompletion(): Boolean {
        if (status != TimerButtonStatus.Completed || completionDelivered) return false
        completionDelivered = true
        return true
    }
}

internal data class TimerButtonEngineSnapshot(
    val durationMillis: Long,
    val status: TimerButtonStatus,
    val elapsedMillis: Long,
    val startedAtMillis: Long,
    val pausedAtElapsedMillis: Long,
    val completionDelivered: Boolean,
)
