# TimerButton Implementation Guide

This guide explains how to implement TimerButton in Compose and XML apps. It is written for app developers consuming the library, not for people changing the timer engine.

## Mental Model

TimerButton has two layers:

- A small timer engine that stores duration, elapsed time, remaining time, progress, and status.
- A UI wrapper that draws a button and maps user actions to timer controls.

Progress is calculated from a monotonic clock:

```text
elapsed = now - startedAt
remaining = duration - elapsed
progress = elapsed / duration
```

That means skipped frames do not cause countdown drift. The next tick recalculates progress from real elapsed time and clamps it to `0f..1f`.

## Which API Should I Use?

Use the Compose API when your screen is Compose:

```kotlin
TimerButton(...)
rememberTimerButtonState(...)
```

Use the View API when your screen is XML, a Fragment layout, RecyclerView row, or a mixed View hierarchy:

```kotlin
com.goeslocal.timerbutton.TimerButtonView
```

Use a ViewModel or domain timestamp when the timer is an app rule. TimerButton is the UI rendering and interaction layer.

## Compose Setup

Add the module dependency:

```kotlin
dependencies {
    implementation(project(":timerbutton"))
}
```

Import the public API:

```kotlin
import com.goeslocal.timerbutton.TimerButton
import com.goeslocal.timerbutton.TimerButtonColors
import com.goeslocal.timerbutton.TimerButtonConfig
import com.goeslocal.timerbutton.TimerButtonStatus
import com.goeslocal.timerbutton.TimerProgressDirection
import com.goeslocal.timerbutton.TimerProgressMode
import com.goeslocal.timerbutton.rememberTimerButtonState
```

## Compose: Start On Click

This is the default. Tapping the button starts the timer. Tapping it again while running does not restart it unless you opt into click delivery with `allowClickWhileRunning`.

```kotlin
@Composable
fun StartOnClickExample() {
    TimerButton(
        text = "Download",
        durationMillis = 8_000L,
        onClick = {
            println("User tapped download")
        },
        onTimerComplete = {
            println("Download timer completed")
        },
    )
}
```

Default click behavior:

- `Idle`, `Cancelled`, or `Completed`: click starts from zero.
- `Paused`: click resumes.
- `Running`: click is ignored unless `allowClickWhileRunning = true`.

## Compose: Auto Start

Use this for cooldown screens or countdowns that begin as soon as the composable enters the UI.

```kotlin
@Composable
fun AutoStartExample() {
    TimerButton(
        text = "Please wait",
        durationMillis = 10_000L,
        config = TimerButtonConfig(
            durationMillis = 10_000L,
            autoStart = true,
        ),
        textFormatter = { state, label ->
            if (state.isRunning) {
                "${(state.remainingMillis + 999) / 1000}s left"
            } else {
                label
            }
        },
    )
}
```

`autoStart` starts only when the state is `Idle`. It does not forcibly restart a timer that is already running, paused, completed, or cancelled.

## Compose: Manual Start

Use manual start when business logic decides when a timer begins, such as after an OTP request succeeds.

```kotlin
@Composable
fun ManualStartExample(otpWasSent: Boolean) {
    val state = rememberTimerButtonState(durationMillis = 30_000L)

    TimerButton(
        state = state,
        text = "Resend OTP",
        config = TimerButtonConfig(
            durationMillis = 30_000L,
            clickStartsTimer = false,
        ),
        textFormatter = { timerState, label ->
            if (timerState.isRunning || timerState.isPaused) {
                "Resend in ${(timerState.remainingMillis + 999) / 1000}s"
            } else {
                label
            }
        },
    )

    LaunchedEffect(otpWasSent) {
        if (otpWasSent) {
            state.start()
        }
    }
}
```

When `clickStartsTimer = false`, tapping the button still calls `onClick` if the button is clickable, but TimerButton will not start, resume, or restart itself.

## Compose: Pause, Resume, Cancel, Reset, Restart

Use a remembered state object for external controls.

```kotlin
@Composable
fun ControlsExample() {
    val state = rememberTimerButtonState(durationMillis = 8_000L)

    Column {
        TimerButton(
            state = state,
            text = "Controlled timer",
            config = TimerButtonConfig(
                durationMillis = 8_000L,
                clickStartsTimer = false,
            ),
            textFormatter = { timerState, label ->
                when {
                    timerState.isRunning -> "Running ${(timerState.progress * 100).toInt()}%"
                    timerState.isPaused -> "Paused ${(timerState.progress * 100).toInt()}%"
                    timerState.isCompleted -> "Complete"
                    else -> label
                }
            },
        )

        Button(onClick = { state.start() }) { Text("Start") }
        Button(onClick = { state.pause() }) { Text("Pause") }
        Button(onClick = { state.resume() }) { Text("Resume") }
        Button(onClick = { state.cancel() }) { Text("Cancel") }
        Button(onClick = { state.reset() }) { Text("Reset") }
        Button(onClick = { state.restart() }) { Text("Restart") }
    }
}
```

State operations are no-ops when they do not apply. For example, `pause()` only works while running, and `resume()` only works while paused.

## Compose: Text Formatting

`textFormatter` receives the current `TimerButtonState` and the base label.

```kotlin
textFormatter = { state, label ->
    when (state.timerState) {
        TimerButtonStatus.Idle -> label
        TimerButtonStatus.Running -> "Wait ${(state.remainingMillis + 999) / 1000}s"
        TimerButtonStatus.Paused -> "Paused at ${(state.progress * 100).toInt()}%"
        TimerButtonStatus.Completed -> "Ready"
        TimerButtonStatus.Cancelled -> label
    }
}
```

Prefer labels that explain the action or cooldown. Do not rely on color alone to communicate progress.

## Compose: Progress Direction And Mode

```kotlin
TimerButton(
    text = "Upload",
    durationMillis = 8_000L,
    config = TimerButtonConfig(
        durationMillis = 8_000L,
        progressDirection = TimerProgressDirection.RightToLeft,
        progressMode = TimerProgressMode.Underline,
    ),
)
```

Directions:

| Direction | Behavior |
| --- | --- |
| `LeftToRight` | Fill from the left edge toward the right edge. |
| `RightToLeft` | Fill from the right edge toward the left edge. |
| `TopToBottom` | Fill from the top edge toward the bottom edge. |
| `BottomToTop` | Fill from the bottom edge toward the top edge. |

Modes:

| Mode | Behavior |
| --- | --- |
| `Overlay` | Draw progress as a tinted layer over the button surface. |
| `Background` | Draw progress as the progress-colored surface behind button content. |
| `Underline` | Draw progress as a 4dp line. |

## Compose: Styling Reference

```kotlin
TimerButton(
    text = "Confirm",
    durationMillis = 5_000L,
    modifier = Modifier
        .width(220.dp)
        .height(56.dp),
    enabled = true,
    shape = RoundedCornerShape(28.dp),
    colors = TimerButtonColors(
        containerColor = Color(0xFF0F766E),
        contentColor = Color.White,
        progressColor = Color(0xFF99F6E4),
        disabledContainerColor = Color(0xFFE5E7EB),
        disabledContentColor = Color(0xFF6B7280),
        borderColor = Color.Transparent,
    ),
    progressAlpha = 0.40f,
    border = BorderStroke(1.dp, Color(0xFF99F6E4)),
    elevation = 2.dp,
    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 10.dp),
    textStyle = MaterialTheme.typography.labelLarge,
    leadingIcon = {
        Text("D")
    },
)
```

Sizing follows normal Compose rules:

- No width modifier: wraps content with Material minimum button size.
- `Modifier.width(...)`: fixed width.
- `Modifier.fillMaxWidth()`: full-width button.
- `Modifier.height(...)`: explicit height.

## Compose: Callback Reference

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
    },
)
```

Rules:

| Callback | When it fires |
| --- | --- |
| `onClick` | When a click is delivered by the button. |
| `onTimerStart` | When a timer starts from idle/cancelled, or after `restart()`. |
| `onTick` | On animation ticks while running. |
| `onTimerComplete` | Once per completed run. |
| `onTimerCancel` | After an explicit cancel. |
| `onTimerPause` | After a running timer pauses. |
| `onTimerResume` | After a paused timer resumes. |
| `onTimerReset` | After reset returns the state to idle. |
| `onTimerRestart` | After restart starts again from zero. |
| `onStateChange` | Whenever the public timer status changes. |

## Compose: API Reference

`TimerButton` has two overloads:

```kotlin
TimerButton(
    text: String,
    durationMillis: Long,
    ...
)
```

Use this overload when the timer is self-contained.

```kotlin
TimerButton(
    state: TimerButtonState,
    text: String,
    ...
)
```

Use this overload when external code needs to control the timer.

Important parameters:

| Parameter | Purpose |
| --- | --- |
| `text` | Base label. |
| `durationMillis` | Total countdown duration. |
| `state` | External timer state and controls. |
| `enabled` | Enables or disables click behavior and disabled colors. |
| `config` | Start behavior, click behavior, progress direction, and progress mode. |
| `textFormatter` | Converts state plus base label into visible text. |
| `leadingIcon` | Optional composable before the label. |
| `shape` | Button and progress clipping shape. |
| `colors` | Container, content, progress, disabled, and optional border colors. |
| `progressAlpha` | Alpha applied to progress color. |
| `border` | Compose border stroke. |
| `elevation` | Shadow elevation. |
| `contentPadding` | Padding around icon and text. |
| `textStyle` | Text style for the label. |

`TimerButtonState` exposes:

| Member | Meaning |
| --- | --- |
| `progress` | Float from `0f` to `1f`. |
| `remainingMillis` | Remaining time. |
| `elapsedMillis` | Elapsed time. |
| `timerState` | `Idle`, `Running`, `Paused`, `Completed`, or `Cancelled`. |
| `isRunning` | Convenience boolean. |
| `isPaused` | Convenience boolean. |
| `isCompleted` | Convenience boolean. |
| `durationMillis` | Current duration. |
| `setDuration(...)` | Change duration and sync the state. |
| `start()` | Start from zero. |
| `pause()` | Pause while running. |
| `resume()` | Resume while paused. |
| `cancel()` | Cancel while running or paused. |
| `reset()` | Return to idle and zero progress. |
| `restart()` | Start again from zero. |

## Compose Lifecycle

Compose timers are lifecycle-safe by construction:

- Timer work runs inside `LaunchedEffect`.
- The timer loop is cancelled when the composable leaves composition.
- State is saved with `rememberSaveable`, so normal Activity recreation restores active timer state.
- Callback lambdas are updated with `rememberUpdatedState`.
- No unmanaged global coroutine is launched.

This does not replace app-level persistence. Store important timestamps outside the composable.

## XML Setup

Use the View in a layout with the `app` namespace:

```xml
<com.goeslocal.timerbutton.TimerButtonView
    android:id="@+id/timerButton"
    android:layout_width="match_parent"
    android:layout_height="56dp"
    android:text="Continue"
    app:timerDuration="10000"
    app:timerTextIdle="Continue"
    app:timerTextRunning="Wait %ss"
    app:timerTextCompleted="Continue now" />
```

Then reference it from Kotlin:

```kotlin
binding.timerButton.start()
```

## XML: Full Attribute Example

```xml
<com.goeslocal.timerbutton.TimerButtonView
    android:id="@+id/resendButton"
    android:layout_width="match_parent"
    android:layout_height="56dp"
    android:text="Resend OTP"
    android:textStyle="bold"
    android:enabled="true"
    app:timerDuration="30000"
    app:timerAutoStart="false"
    app:timerClickStartsTimer="true"
    app:timerAllowClickWhileRunning="false"
    app:timerTextIdle="Resend OTP"
    app:timerTextRunning="Resend in %ss"
    app:timerTextCompleted="Resend now"
    app:timerButtonBackgroundColor="#172033"
    app:timerButtonDisabledColor="#E5E7EB"
    app:timerTextColor="#FFFFFF"
    app:timerProgressColor="#7DA2FF"
    app:timerProgressAlpha="0.45"
    app:timerProgressDirection="leftToRight"
    app:timerProgressMode="overlay"
    app:timerCornerRadius="18dp"
    app:timerStrokeColor="#7DA2FF"
    app:timerStrokeWidth="1dp" />
```

## XML Attribute Reference

| Attribute | Type | Default | Purpose |
| --- | --- | --- | --- |
| `timerDuration` | integer | `10000` | Total timer duration in milliseconds. |
| `timerProgressColor` | color | teal-ish default | Progress fill or underline color. |
| `timerProgressAlpha` | float | `0.32` | Alpha applied to progress color. |
| `timerProgressDirection` | enum | `leftToRight` | Fill direction. |
| `timerProgressMode` | enum | `overlay` | Progress drawing mode. |
| `timerAutoStart` | boolean | `false` | Start when attached to the window. |
| `timerClickStartsTimer` | boolean | `true` | Let the view start/resume itself on click. |
| `timerAllowClickWhileRunning` | boolean | `false` | Deliver click listener while running. |
| `timerTextIdle` | string | `android:text` | Text shown while idle or cancelled. |
| `timerTextRunning` | string | `android:text` | Text shown while running or paused. |
| `timerTextCompleted` | string | `android:text` | Text shown after completion. |
| `timerCornerRadius` | dimension | `12dp` | Corner radius for background/progress. |
| `timerStrokeColor` | color | transparent | Optional border color. |
| `timerStrokeWidth` | dimension | `0dp` | Optional border width. |
| `timerButtonBackgroundColor` | color | purple default | Enabled button surface. |
| `timerButtonDisabledColor` | color | gray default | Disabled button surface. |
| `timerTextColor` | color | current text color | Label color. |

XML enum values:

| Attribute | Values |
| --- | --- |
| `timerProgressDirection` | `leftToRight`, `rightToLeft`, `topToBottom`, `bottomToTop` |
| `timerProgressMode` | `overlay`, `background`, `underline` |

`timerTextRunning` may contain `%s` or `%d`. The view replaces either token with remaining seconds rounded up.

## XML Kotlin API

```kotlin
binding.timerButton.setDuration(15_000L)
binding.timerButton.start()
binding.timerButton.pause()
binding.timerButton.resume()
binding.timerButton.cancel()
binding.timerButton.reset()
binding.timerButton.restart()
binding.timerButton.release()
```

`TimerButtonView` currently exposes runtime methods for duration, listener, click listener, timer controls, and cleanup. Visual styling is configured from XML attributes or normal TextView APIs at inflation time.

## XML Listener Reference

```kotlin
binding.timerButton.setTimerListener(
    object : TimerButtonListener {
        override fun onTimerStart() = Unit
        override fun onTick(remainingMillis: Long, progress: Float) = Unit
        override fun onTimerComplete() = Unit
        override fun onTimerCancel() = Unit
        override fun onTimerPause() = Unit
        override fun onTimerResume() = Unit
        override fun onTimerReset() = Unit
        override fun onTimerRestart() = Unit
        override fun onStateChange(status: TimerButtonStatus) = Unit
    },
)
```

Click listeners still work:

```kotlin
binding.timerButton.setOnClickListener {
    println("Timer button clicked")
}
```

If `timerClickStartsTimer="true"`, TimerButtonView handles timer start/resume before delivering your click listener. If the timer is already running and `timerAllowClickWhileRunning="false"`, your click listener is not called.

## XML Lifecycle And Cleanup

`TimerButtonView` removes frame callbacks in `onDetachedFromWindow`. That covers normal Activity, Fragment, RecyclerView, and navigation teardown.

Call `release()` when you want explicit cleanup:

```kotlin
override fun onDestroyView() {
    binding.resendButton.release()
    _binding = null
    super.onDestroyView()
}
```

Use `release()` when:

- A listener captures a Fragment binding.
- The view is retained or reused manually.
- You keep a reference outside the normal view hierarchy.

## Recipes

### OTP cooldown

Compose:

```kotlin
val state = rememberTimerButtonState(30_000L)

TimerButton(
    state = state,
    text = "Resend OTP",
    config = TimerButtonConfig(30_000L, clickStartsTimer = false),
    textFormatter = { timerState, label ->
        if (timerState.isRunning) {
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

XML:

```xml
<com.goeslocal.timerbutton.TimerButtonView
    android:id="@+id/resendButton"
    android:layout_width="220dp"
    android:layout_height="56dp"
    android:text="Resend OTP"
    app:timerDuration="30000"
    app:timerClickStartsTimer="false"
    app:timerTextIdle="Resend OTP"
    app:timerTextRunning="Resend in %ss"
    app:timerTextCompleted="Resend now" />
```

```kotlin
if (otpSentSuccessfully) {
    binding.resendButton.start()
}
```

### Auto-start wait screen

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

```xml
<com.goeslocal.timerbutton.TimerButtonView
    android:layout_width="match_parent"
    android:layout_height="56dp"
    android:text="Continue"
    app:timerDuration="5000"
    app:timerAutoStart="true"
    app:timerTextRunning="Continue in %ss"
    app:timerTextCompleted="Continue now" />
```

### Pause/resume UI

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

XML:

```kotlin
binding.uploadButton.start()
binding.pauseButton.setOnClickListener { binding.uploadButton.pause() }
binding.resumeButton.setOnClickListener { binding.uploadButton.resume() }
```

### Multiple independent timers

Compose:

```kotlin
Row {
    TimerButton("First", 3_000L)
    TimerButton("Second", 5_000L)
    TimerButton("Third", 7_000L)
}
```

XML:

```xml
<com.goeslocal.timerbutton.TimerButtonView
    android:id="@+id/firstTimer"
    android:layout_width="160dp"
    android:layout_height="56dp"
    android:text="First"
    app:timerDuration="3000" />

<com.goeslocal.timerbutton.TimerButtonView
    android:id="@+id/secondTimer"
    android:layout_width="180dp"
    android:layout_height="56dp"
    android:text="Second"
    app:timerDuration="5000" />
```

Each instance owns its own timer state.

## Accessibility

Recommendations:

- Use descriptive text such as `Resend in 12s`.
- Keep touch targets at least 48dp.
- Keep text and progress contrast readable.
- Use `contentDescription` for icon-only buttons.
- Do not communicate progress with color alone.

Compose sets button role and progress semantics. XML updates content description with the current text and percentage complete.

## Testing

Run all checks:

```bash
./gradlew check
```

Focused library tests:

```bash
./gradlew :timerbutton:testDebugUnitTest
```

The timer engine has fake-clock coverage for start, completion, one-shot completion consumption, cancel, pause/resume, reset, restart, progress clamping, restore after Activity recreation, and independent timer instances.

For app-level tests, prefer asserting:

- The right state method is called after a business event.
- Labels change to the expected countdown text.
- Completion enables the next action.
- Important cooldown rules are enforced by your ViewModel/domain layer, not only by the UI timer.

## README Media Capture

The repo includes a script for regenerating README screenshots from a connected Android device:

```bash
python3 scripts/capture_readme_media.py
```

It launches the sample app, taps visible timers, captures PNG screenshots, crops device bars, and writes:

- `docs/media/screenshots/showcase-running.png`
- `docs/media/screenshots/directions-running.png`
- `docs/media/screenshots/multiple-and-xml-running.png`
- component crops in `docs/media/components/`

The script requires Pillow.

## Production Checklist

Before shipping a timer flow:

- Decide whether the timer is only UI state or a business rule.
- Store server-enforced timestamps outside TimerButton.
- Use `rememberTimerButtonState` for Compose screens with external controls.
- Use `release()` for retained XML views or Fragment listeners that capture bindings.
- Use descriptive countdown text.
- Test completion, cancellation, pause/resume, and configuration change behavior.
