package com.goeslocal.timerbutton

/**
 * Listener for View-based timer button events.
 */
interface TimerButtonListener {
    fun onTimerStart() = Unit
    fun onTick(remainingMillis: Long, progress: Float) = Unit
    fun onTimerComplete() = Unit
    fun onTimerCancel() = Unit
    fun onTimerPause() = Unit
    fun onTimerResume() = Unit
    fun onTimerReset() = Unit
    fun onTimerRestart() = Unit
    fun onStateChange(status: TimerButtonStatus) = Unit
}

