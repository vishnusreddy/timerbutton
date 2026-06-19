package com.goeslocal.timerbutton

/**
 * Shared behavior configuration for timer buttons.
 *
 * @property durationMillis total timer duration.
 * @property autoStart starts the timer when attached/composed.
 * @property clickStartsTimer starts or restarts the timer when clicked.
 * @property allowClickWhileRunning whether user clicks are delivered during an active timer.
 */
data class TimerButtonConfig(
    val durationMillis: Long = 10_000L,
    val autoStart: Boolean = false,
    val clickStartsTimer: Boolean = true,
    val allowClickWhileRunning: Boolean = false,
    val progressDirection: TimerProgressDirection = TimerProgressDirection.LeftToRight,
    val progressMode: TimerProgressMode = TimerProgressMode.Overlay,
)

