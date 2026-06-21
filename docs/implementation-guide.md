# TimerButton Usage Guide

This guide is the practical reference for using TimerButton in production apps. It keeps the README fast while giving enough detail for Compose, XML, callbacks, lifecycle, and testing.

## Install

```kotlin
dependencies {
    implementation("com.goeslocal:timerbutton-compose:0.2.0") // Compose
    implementation("com.goeslocal:timerbutton-view:0.2.0")    // XML/View

    // Or use the compatibility bundle when you want both APIs:
    implementation("com.goeslocal:timerbutton:0.2.0")
}
```

For local development inside this repository:

```kotlin
dependencies {
    implementation(project(":timerbutton-compose"))
    implementation(project(":timerbutton-view"))
}
```

## Mental Model

TimerButton has two public surfaces:

- Compose: `TimerButton(...)` and `rememberTimerButtonState(...)`
- XML/View: `TimerButtonView`

They are published separately so Compose-only apps do not pull XML-only code, and XML-only apps do not pull Compose dependencies. The original `timerbutton` artifact remains as a compatibility bundle that depends on both split artifacts.

Progress is calculated from a monotonic clock:

```text
elapsed = now - startedAt
remaining = duration - elapsed
progress = elapsed / duration
```

This keeps countdowns accurate even when frames are skipped.

## Compose

Start on click is the default:

```kotlin
TimerButton(
    text = "Retry",
    durationMillis = 10_000L,
    textFormatter = { state, label ->
        if (state.isRunning) {
            "Retry in ${(state.remainingMillis + 999) / 1000}s"
        } else {
            label
        }
    },
    onTimerComplete = {
        println("Retry is available")
    },
)
```

Use a state object when another event controls the timer:

```kotlin
val timerState = rememberTimerButtonState(durationMillis = 30_000L)

TimerButton(
    state = timerState,
    text = "Resend OTP",
    config = TimerButtonConfig(
        durationMillis = 30_000L,
        clickStartsTimer = false,
    ),
    textFormatter = { state, label ->
        if (state.isRunning || state.isPaused) {
            "Resend in ${(state.remainingMillis + 999) / 1000}s"
        } else {
            label
        }
    },
)

LaunchedEffect(otpSentSuccessfully) {
    if (otpSentSuccessfully) timerState.start()
}
```

### Compose Controls

`TimerButtonState` exposes:

| Member | Meaning |
| --- | --- |
| `progress` | Float from `0f` to `1f`. |
| `remainingMillis` | Remaining duration in milliseconds. |
| `elapsedMillis` | Elapsed duration in milliseconds. |
| `timerState` | `Idle`, `Running`, `Paused`, `Completed`, or `Cancelled`. |
| `isRunning`, `isPaused`, `isCompleted` | Convenience booleans. |
| `setDuration(...)` | Change the current duration. |
| `start()` | Start from zero. |
| `pause()` | Pause while running. |
| `resume()` | Resume while paused. |
| `cancel()` | Stop while keeping current progress. |
| `reset()` | Return to idle and zero progress. |
| `restart()` | Start again from zero. |

Operations that do not apply are no-ops. For example, `pause()` only changes state while running.

### Compose Styling

```kotlin
TimerButton(
    text = "Download report",
    durationMillis = 8_000L,
    modifier = Modifier
        .width(240.dp)
        .height(56.dp),
    config = TimerButtonConfig(
        durationMillis = 8_000L,
        progressDirection = TimerProgressDirection.LeftToRight,
        progressMode = TimerProgressMode.Overlay,
    ),
    colors = TimerButtonColors(
        containerColor = Color(0xFF172033),
        contentColor = Color.White,
        progressColor = Color(0xFF7DA2FF),
        disabledContainerColor = Color(0xFFE5E7EB),
        disabledContentColor = Color(0xFF6B7280),
    ),
    shape = RoundedCornerShape(18.dp),
    border = BorderStroke(1.dp, Color(0xFF7DA2FF)),
    progressAlpha = 0.42f,
)
```

## XML/View

Add `TimerButtonView` to any XML layout:

```xml
<com.goeslocal.timerbutton.TimerButtonView
    android:id="@+id/resendButton"
    android:layout_width="220dp"
    android:layout_height="56dp"
    android:text="Resend OTP"
    app:timerDuration="30000"
    app:timerTextIdle="Resend OTP"
    app:timerTextRunning="Resend in %ss"
    app:timerTextCompleted="Resend now"
    app:timerProgressDirection="leftToRight"
    app:timerProgressMode="overlay" />
```

Control it from Kotlin:

```kotlin
binding.resendButton.setTimerListener(
    object : TimerButtonListener {
        override fun onTimerComplete() {
            println("Resend is available")
        }
    },
)

binding.resendButton.start()
```

### XML Attributes

| Attribute | Default | Purpose |
| --- | --- | --- |
| `timerDuration` | `10000` | Total duration in milliseconds. |
| `timerProgressColor` | teal | Progress fill or underline color. |
| `timerProgressAlpha` | `0.32` | Alpha applied to progress color. |
| `timerProgressDirection` | `leftToRight` | Fill direction. |
| `timerProgressMode` | `overlay` | Progress drawing mode. |
| `timerAutoStart` | `false` | Start when attached. |
| `timerClickStartsTimer` | `true` | Start/resume from clicks. |
| `timerAllowClickWhileRunning` | `false` | Deliver clicks while running. |
| `timerTextIdle` | `android:text` | Idle/cancelled text. |
| `timerTextRunning` | `android:text` | Running/paused text. |
| `timerTextCompleted` | `android:text` | Completed text. |
| `timerCornerRadius` | `12dp` | Background and progress corner radius. |
| `timerStrokeColor` | transparent | Optional border color. |
| `timerStrokeWidth` | `0dp` | Optional border width. |
| `timerButtonBackgroundColor` | purple | Enabled surface. |
| `timerButtonDisabledColor` | gray | Disabled surface. |
| `timerTextColor` | current text color | Label color. |

`timerTextRunning` may contain `%s` or `%d`; TimerButton replaces it with remaining seconds rounded up.

## Shared Options

Progress directions:

- `LeftToRight`
- `RightToLeft`
- `TopToBottom`
- `BottomToTop`

Progress modes:

- `Overlay`: tinted layer over the button surface.
- `Background`: progress-colored surface behind content.
- `Underline`: 4dp progress line.

Callbacks:

| Callback | When it fires |
| --- | --- |
| `onClick` | A button click is delivered. |
| `onTimerStart` | A timer starts from idle/cancelled, or after restart. |
| `onTick` | An animation tick occurs while running. |
| `onTimerComplete` | A run completes, once per run. |
| `onTimerCancel` | A running or paused timer is cancelled. |
| `onTimerPause` | A running timer pauses. |
| `onTimerResume` | A paused timer resumes. |
| `onTimerReset` | State returns to idle. |
| `onTimerRestart` | State starts again from zero. |
| `onStateChange` | Public timer state changes. |

## Lifecycle

Compose timers run inside `LaunchedEffect` and stop when the composable leaves composition. State is saved with `rememberSaveable`, so normal Activity recreation restores timer progress.

`TimerButtonView` removes animation callbacks when detached. Call `release()` in `onDestroyView()` when listeners capture a Fragment binding or when you retain view references manually.

## Production Rules

TimerButton is a UI component. For important rules such as OTP cooldowns, billing windows, auth lockouts, or server-enforced retry limits, store the authoritative timestamp in your ViewModel, repository, or backend. Use TimerButton to render the visible countdown and handle local interaction.

## Testing

Run:

```bash
./gradlew check
```

Focused library tests:

```bash
./gradlew :timerbutton-core:testDebugUnitTest
```

The timer engine has fake-clock coverage for start, completion, one-shot completion delivery, cancel, pause/resume, reset, restart, clamping, restore after Activity recreation, and independent timer instances.
