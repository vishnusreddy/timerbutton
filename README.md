# TimerButton

TimerButton is a Kotlin-first Android library for Material-style buttons with timed progress. It gives Android teams one reusable component for “wait before retry”, “download in progress”, “hold for action”, “continue after countdown”, OTP resend, cooldown, and timed confirmation flows.

The library supports both UI stacks:

- Jetpack Compose: `TimerButton(...)` and `rememberTimerButtonState(...)`
- XML/View apps: `TimerButtonView`

The demo app in `:app` showcases Compose and XML usage. The reusable implementation lives in `:timerbutton`.

## Demo Media

Animated component crops captured from the sample app running on a physical Android device.

<table>
  <tr>
    <td><img src="docs/media/gifs/primary-progress.gif" alt="Primary TimerButton progress animation" width="360"></td>
    <td><img src="docs/media/gifs/otp-progress.gif" alt="OTP TimerButton cooldown animation" width="320"></td>
  </tr>
  <tr>
    <td>Primary CTA</td>
    <td>OTP Cooldown</td>
  </tr>
  <tr>
    <td><img src="docs/media/gifs/vertical-progress.gif" alt="Top-to-bottom TimerButton progress animation" width="320"></td>
    <td><img src="docs/media/components/xml-button.png" alt="XML TimerButton component" width="320"></td>
  </tr>
  <tr>
    <td>Vertical Progress</td>
    <td>XML View</td>
  </tr>
</table>

## Why Use This

Timer flows are easy to get subtly wrong. A button that owns a countdown needs to avoid UI-thread drift, double completion callbacks, leaks from long-running timers, stale Compose lambdas, and awkward state cleanup.

TimerButton is built around a small monotonic-clock timer engine. UI progress is derived from elapsed real time, not from blindly subtracting a fixed amount every frame. If the main thread is delayed, the next tick recalculates from actual elapsed time and clamps progress to `0f..1f`.

## Features

- Compose and XML/View APIs
- Start, pause, resume, cancel, reset, and restart
- Accurate elapsed-real-time progress
- `Idle`, `Running`, `Paused`, `Completed`, and `Cancelled` states
- One-shot completion callback
- `onTick`, lifecycle, and state change callbacks
- Overlay, background, and underline progress modes
- Left-to-right, right-to-left, top-to-bottom, and bottom-to-top progress directions
- Natural wrap-content sizing and explicit width/height sizing
- Full-width buttons when requested by the caller
- Custom colors, alpha, shapes, borders, elevation, padding, text style, and icons
- XML attributes for common configuration
- Unit-tested timer engine with fake-clock tests
- No `GlobalScope`, no static context references, and no retained Activity/Fragment references

## Module Setup

For local development in this repo:

```kotlin
dependencies {
    implementation(project(":timerbutton"))
}
```

When published later, the dependency will look like:

```kotlin
dependencies {
    implementation("com.goeslocal:timerbutton:<version>")
}
```

## Compose Quick Start

The simplest usage starts the timer when the user taps the button:

```kotlin
TimerButton(
    text = "Start Timer",
    durationMillis = 10_000L,
    onTimerComplete = {
        println("Timer completed")
    }
)
```

Use normal Compose sizing modifiers:

```kotlin
TimerButton(
    text = "Compact",
    durationMillis = 5_000L,
    modifier = Modifier
        .width(140.dp)
        .height(44.dp)
)

TimerButton(
    text = "Full width CTA",
    durationMillis = 5_000L,
    modifier = Modifier
        .fillMaxWidth()
        .height(56.dp)
)
```

If you do not pass a width modifier, the button wraps its content like a normal Material button.

## Compose State Usage

Use `rememberTimerButtonState` when another control needs to start, pause, resume, cancel, reset, or restart the timer.

```kotlin
@Composable
fun ResendOtp() {
    val timerState = rememberTimerButtonState(durationMillis = 30_000L)

    Column {
        TimerButton(
            state = timerState,
            text = "Resend OTP",
            config = TimerButtonConfig(
                durationMillis = 30_000L,
                clickStartsTimer = false
            ),
            textFormatter = { state, label ->
                if (state.isRunning) {
                    "Resend in ${(state.remainingMillis + 999) / 1000}s"
                } else {
                    label
                }
            },
            onTimerComplete = {
                println("User can request another OTP")
            }
        )

        Button(onClick = { timerState.start() }) {
            Text("Start")
        }

        Button(onClick = { timerState.cancel() }) {
            Text("Cancel")
        }
    }
}
```

## When Does The Timer Start?

There are three common patterns.

### 1. Start On Click

This is the default. `clickStartsTimer = true`, so tapping the button starts the timer.

```kotlin
TimerButton(
    text = "Download",
    durationMillis = 8_000L
)
```

### 2. Auto Start

Use this for countdown screens or cooldowns that begin as soon as the composable enters the UI.

```kotlin
TimerButton(
    text = "Please wait",
    durationMillis = 10_000L,
    config = TimerButtonConfig(
        durationMillis = 10_000L,
        autoStart = true
    )
)
```

### 3. Manual Start

Use this when business logic decides when the timer starts, such as after a network request succeeds.

```kotlin
val state = rememberTimerButtonState(30_000L)

TimerButton(
    state = state,
    text = "Resend OTP",
    config = TimerButtonConfig(
        durationMillis = 30_000L,
        clickStartsTimer = false
    )
)

LaunchedEffect(otpWasSentSuccessfully) {
    if (otpWasSentSuccessfully) {
        state.start()
    }
}
```

## Compose Lifecycle Behavior

Compose timers are lifecycle-safe by construction:

- Timer work runs inside `LaunchedEffect`.
- The timer loop is cancelled when the composable leaves composition.
- Timer state is saved with `rememberSaveable`, so Activity recreation from orientation changes does not restart an active timer.
- `rememberUpdatedState` is used internally so callbacks do not become stale after recomposition.
- No unmanaged global coroutine is launched.

You do not need to manually clear the Compose timer when the screen is destroyed. Removing the composable cancels the running coroutine. On configuration changes, such as rotation, the state restores from a saved monotonic timestamp and continues from the correct elapsed time.

This does not replace business-level persistence. If the app process is killed or the countdown is security-sensitive, store the authoritative timestamp in your ViewModel, repository, or backend policy.

## Should I Put TimerButtonState In A ViewModel?

Usually, no. `TimerButtonState` is UI state and is designed to be remembered inside Compose.

Use local Compose state when:

- The timer only affects this button.
- The timer only needs to survive normal recomposition and configuration changes.
- The timer is purely presentational, such as progress fill and remaining text.

Use a ViewModel for business-level timing when:

- The countdown must survive navigation away from the composable.
- Multiple screens need the same remaining time.
- The timer is tied to server state, OTP cooldown policy, billing, auth, or security behavior.
- You need process-resilient behavior using saved timestamps.

Recommended production pattern for important cooldowns:

1. Store the authoritative start/end timestamp in your ViewModel or repository.
2. Expose remaining time as UI state.
3. Use `TimerButtonState` for the button rendering and user controls.
4. On screen re-entry, restart or restore the UI timer from the authoritative remaining time.

Do not put an Android `Context`, Activity, Fragment, or View reference in a ViewModel.

## Compose Customization

```kotlin
TimerButton(
    text = "Confirm",
    durationMillis = 5_000L,
    modifier = Modifier
        .width(220.dp)
        .height(56.dp),
    shape = RoundedCornerShape(28.dp),
    colors = TimerButtonColors(
        containerColor = Color(0xFF0B6E4F),
        contentColor = Color.White,
        progressColor = Color(0xFFFFC857),
        disabledContainerColor = Color.LightGray,
        disabledContentColor = Color.DarkGray
    ),
    progressAlpha = 0.40f,
    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 10.dp),
    leadingIcon = {
        Icon(Icons.Default.Download, contentDescription = null)
    }
)
```

## Progress Direction And Mode

```kotlin
TimerButton(
    text = "Upload",
    durationMillis = 8_000L,
    config = TimerButtonConfig(
        durationMillis = 8_000L,
        progressDirection = TimerProgressDirection.LeftToRight,
        progressMode = TimerProgressMode.Overlay
    )
)
```

Directions:

- `LeftToRight`
- `RightToLeft`
- `TopToBottom`
- `BottomToTop`

Modes:

- `Overlay`: progress is drawn over the button surface.
- `Background`: progress is drawn behind the content.
- `Underline`: progress is drawn as a thin line.

## Compose Callback Behavior

```kotlin
TimerButton(
    text = "Retry",
    durationMillis = 10_000L,
    onClick = {
        println("Button clicked")
    },
    onTimerStart = {
        println("Started")
    },
    onTick = { remainingMillis, progress ->
        println("remaining=$remainingMillis progress=$progress")
    },
    onTimerComplete = {
        println("Completed once")
    },
    onTimerCancel = {
        println("Cancelled")
    },
    onTimerPause = {
        println("Paused")
    },
    onTimerResume = {
        println("Resumed")
    },
    onTimerReset = {
        println("Reset")
    },
    onTimerRestart = {
        println("Restarted")
    },
    onStateChange = { status ->
        println("State changed to $status")
    }
)
```

Callback rules:

- `onTimerStart` fires when the timer starts from idle or cancelled.
- `onTick` fires while running.
- `onTimerComplete` fires once per completed run.
- `onTimerCancel` fires only on explicit cancel.
- `onTimerPause` fires only when a running timer pauses.
- `onTimerResume` fires only when a paused timer resumes.
- `onTimerReset` fires when reset returns the button to idle.
- `onTimerRestart` fires when a timer is restarted from zero.
- `onStateChange` fires when the public timer state changes.

## XML Usage

```xml
<com.goeslocal.timerbutton.TimerButtonView
    android:id="@+id/resendButton"
    android:layout_width="220dp"
    android:layout_height="56dp"
    android:text="Resend OTP"
    app:timerDuration="30000"
    app:timerProgressColor="@color/purple_500"
    app:timerProgressAlpha="0.35"
    app:timerProgressDirection="leftToRight"
    app:timerProgressMode="overlay"
    app:timerTextIdle="Resend OTP"
    app:timerTextRunning="Resend in %ss"
    app:timerTextCompleted="Resend now"
    app:timerCornerRadius="18dp"
    app:timerAutoStart="false"
    app:timerClickStartsTimer="true"
    app:timerAllowClickWhileRunning="false" />
```

## XML Kotlin Usage

```kotlin
binding.resendButton.setDuration(30_000L)

binding.resendButton.setTimerListener(
    object : TimerButtonListener {
        override fun onTimerStart() {
            // Disable related controls if needed.
        }

        override fun onTick(remainingMillis: Long, progress: Float) {
            // Update external labels if needed.
        }

        override fun onTimerComplete() {
            Toast.makeText(this@MainActivity, "Completed", Toast.LENGTH_SHORT).show()
        }
    }
)

binding.resendButton.start()
```

Available controls:

```kotlin
binding.resendButton.start()
binding.resendButton.pause()
binding.resendButton.resume()
binding.resendButton.cancel()
binding.resendButton.reset()
binding.resendButton.restart()
```

## XML Lifecycle And Cleanup

`TimerButtonView` removes its frame callbacks in `onDetachedFromWindow`. That covers normal Activity, Fragment, RecyclerView, and navigation teardown.

The view does not store Activity or Fragment references. Your listener can still accidentally capture one, like any Android listener. For long-lived or manually retained views, call:

```kotlin
binding.resendButton.release()
```

Use `release()` when:

- You keep a reference to the view outside the normal view hierarchy.
- You set a listener that captures a Fragment binding.
- You manually detach/reuse views.

Typical Fragment cleanup:

```kotlin
override fun onDestroyView() {
    binding.resendButton.release()
    _binding = null
    super.onDestroyView()
}
```

If the view is only used in a normal layout and not retained anywhere, `onDetachedFromWindow` is normally enough.

## Accessibility

TimerButton behaves like a button and exposes progress semantics in Compose. The XML view updates its content description with progress percentage.

Recommendations for app teams:

- Use text such as “Resend in 12s”, not color alone.
- Keep contrast readable when using overlay progress.
- Use `contentDescription` for icon-only usage.
- Avoid tiny touch targets; prefer at least 48dp height/width for tappable production UI.
- Respect disabled state for unavailable actions.

## Timer Accuracy

The engine stores a monotonic start time and calculates:

```text
elapsed = now - startedAt
remaining = duration - elapsed
progress = elapsed / duration
```

Progress is clamped between `0f` and `1f`. This avoids major drift if the UI thread skips frames.

## Testing

Run library unit tests and build the demo:

```bash
./gradlew :timerbutton:testDebugUnitTest :app:assembleDebug
```

The timer engine is unit-tested with a fake clock for:

- start
- completion
- one-shot completion callback consumption
- cancel
- pause/resume
- reset
- restart
- progress clamping
- restore after Activity recreation/configuration change
- multiple independent timer instances

## Demo App

The `:app` module demonstrates:

- Natural-size Compose buttons
- Fixed width/height buttons
- Full-width CTA buttons
- Compact, tall, pill, rounded, square-ish, and outlined styles
- Custom colors and padding
- Icon content
- OTP/resend formatting
- Pause/resume controls
- Cancel/reset/restart controls
- Progress directions
- Progress modes
- Multiple independent timers
- XML `TimerButtonView`

## Production Guidance

For simple UI countdowns, use `TimerButton` directly.

For important app rules, such as OTP retry windows, do not rely only on a visual timer. Store the real cooldown timestamp in your domain layer or ViewModel, validate it against server policy, and use TimerButton to present the countdown.

For Compose screens, prefer `rememberTimerButtonState` for UI-level timers. It survives recomposition and configuration changes. Use your ViewModel or domain layer for timers that must survive navigation away from the screen, process death, or server-enforced cooldown rules.

For XML screens, avoid retaining listener objects longer than the view lifecycle. Call `release()` in `onDestroyView` if your Fragment binding pattern benefits from explicit cleanup.

## Contributing

Keep public APIs small and predictable. Add fake-clock tests for timer behavior changes. Avoid new dependencies unless they clearly improve reliability or developer experience.

## License

Apache License 2.0. See [LICENSE](LICENSE).
