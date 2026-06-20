# Advanced Usage

## OTP Cooldown

Use TimerButton for the visible countdown, but keep the authoritative cooldown in your domain layer.

```kotlin
val state = rememberTimerButtonState(30_000L)

TimerButton(
    state = state,
    text = "Resend OTP",
    config = TimerButtonConfig(30_000L, clickStartsTimer = false),
    textFormatter = { timerState, label ->
        if (timerState.isRunning || timerState.isPaused) {
            "Resend in ${(timerState.remainingMillis + 999) / 1000}s"
        } else {
            label
        }
    },
)

LaunchedEffect(otpSentSuccessfully) {
    if (otpSentSuccessfully) state.start()
}
```

## Auto-Start Wait Screen

```kotlin
TimerButton(
    text = "Continue",
    durationMillis = 5_000L,
    config = TimerButtonConfig(
        durationMillis = 5_000L,
        autoStart = true,
    ),
    textFormatter = { state, label ->
        if (state.isRunning) "Continue in ${(state.remainingMillis + 999) / 1000}s" else label
    },
)
```

## Pause/Resume UI

```kotlin
val state = rememberTimerButtonState(20_000L)

TimerButton(
    state = state,
    text = "Upload",
    config = TimerButtonConfig(20_000L, clickStartsTimer = false),
)

Button(onClick = { state.pause() }) { Text("Pause") }
Button(onClick = { state.resume() }) { Text("Resume") }
```

## Multiple Independent Timers

```kotlin
Row {
    TimerButton("First", 3_000L)
    TimerButton("Second", 5_000L)
    TimerButton("Third", 7_000L)
}
```

Each instance owns its own timer state.

## Accessibility Checklist

- Use descriptive text such as `Resend in 12s`.
- Keep touch targets at least 48dp.
- Keep text and progress contrast readable.
- Use `contentDescription` for icon-only wrappers.
- Do not communicate progress with color alone.
