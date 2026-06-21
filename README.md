# TimerButton

[![Maven Central](https://img.shields.io/maven-central/v/com.goeslocal/timerbutton.svg?label=Maven%20Central)](https://central.sonatype.com/artifact/com.goeslocal/timerbutton)
[![License](https://img.shields.io/badge/license-Apache--2.0-blue.svg)](LICENSE)
[![Android](https://img.shields.io/badge/platform-Android-brightgreen.svg)](https://developer.android.com/)

TimerButton is a small Android library for buttons that show their own countdown progress. Use it for resend OTP cooldowns, retry waits, temporary lockouts, sync/download waits, quiz timers, or any screen where the user should see when an action becomes available again.

It supports both Android UI stacks:

- Jetpack Compose: `TimerButton(...)` and `rememberTimerButtonState(...)`
- XML/View apps: `TimerButtonView`

The library renders the timer UI and handles local interaction. Your app should still own the real business rule, such as the server-provided OTP resend timestamp or retry deadline.

## Demo

Screenshots are captured from the sample app on a physical Android device while the timers are running.

<table>
  <tr>
    <td><img src="docs/media/screenshots/showcase-running.png" alt="TimerButton showcase screen with multiple running Compose timers" width="280"></td>
    <td><img src="docs/media/screenshots/directions-running.png" alt="TimerButton directions and progress modes screen with running timers" width="280"></td>
    <td><img src="docs/media/screenshots/multiple-and-xml-running.png" alt="TimerButton multiple timers and XML view screen with running timers" width="280"></td>
  </tr>
  <tr>
    <td>Compose Showcase</td>
    <td>Directions and Modes</td>
    <td>Multiple Timers and XML</td>
  </tr>
</table>

## Install

```kotlin
dependencies {
    implementation("com.goeslocal:timerbutton:0.1.0")
}
```

For local development in this repo:

```kotlin
dependencies {
    implementation(project(":timerbutton"))
}
```

## Compose: Minimal Usage

Use this when tapping the button should start the countdown automatically.

```kotlin
import androidx.compose.runtime.Composable
import com.goeslocal.timerbutton.TimerButton

@Composable
fun ResendOtpButton(
    resendOtp: () -> Unit,
) {
    TimerButton(
        text = "Resend OTP",
        durationMillis = 30_000L,
        onClick = resendOtp,
        textFormatter = { state, label ->
            if (state.isRunning || state.isPaused) {
                "Resend in ${(state.remainingMillis + 999) / 1000}s"
            } else {
                label
            }
        },
        onTimerComplete = {
            println("User can request another OTP")
        },
    )
}
```

What happens here:

- First tap calls `onClick` and starts the timer.
- While running, additional taps are blocked by default.
- The label changes through `textFormatter`.
- `onTimerComplete` is delivered once when the countdown finishes.

## Compose: Fully Customizable Usage

Use `rememberTimerButtonState` when the timer is controlled by a ViewModel event, API result, or another button. This example shows manual controls, custom colors, shape, progress mode, callbacks, and text formatting.

```kotlin
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.goeslocal.timerbutton.TimerButton
import com.goeslocal.timerbutton.TimerButtonColors
import com.goeslocal.timerbutton.TimerButtonConfig
import com.goeslocal.timerbutton.TimerButtonStatus
import com.goeslocal.timerbutton.TimerProgressDirection
import com.goeslocal.timerbutton.TimerProgressMode
import com.goeslocal.timerbutton.rememberTimerButtonState

@Composable
fun CustomDownloadTimer() {
    val timerState = rememberTimerButtonState(durationMillis = 8_000L)

    Column {
        TimerButton(
            state = timerState,
            text = "Download report",
            modifier = Modifier
                .width(240.dp)
                .height(56.dp),
            enabled = true,
            config = TimerButtonConfig(
                durationMillis = 8_000L,
                autoStart = false,
                clickStartsTimer = false,
                allowClickWhileRunning = true,
                progressDirection = TimerProgressDirection.LeftToRight,
                progressMode = TimerProgressMode.Overlay,
            ),
            colors = TimerButtonColors(
                containerColor = Color(0xFF172033),
                contentColor = Color.White,
                progressColor = Color(0xFF7DA2FF),
                disabledContainerColor = Color(0xFFE5E7EB),
                disabledContentColor = Color(0xFF6B7280),
                borderColor = Color.Transparent,
            ),
            shape = RoundedCornerShape(18.dp),
            border = BorderStroke(1.dp, Color(0xFF7DA2FF)),
            elevation = 3.dp,
            progressAlpha = 0.42f,
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 10.dp),
            leadingIcon = { Text("D", fontWeight = FontWeight.Bold) },
            textFormatter = { state, label ->
                when (state.timerState) {
                    TimerButtonStatus.Running,
                    TimerButtonStatus.Paused -> "$label ${(state.remainingMillis + 999) / 1000}s"
                    TimerButtonStatus.Completed -> "Download again"
                    TimerButtonStatus.Cancelled -> "Download cancelled"
                    TimerButtonStatus.Idle -> label
                }
            },
            onClick = {
                println("Button clicked")
            },
            onTimerStart = {
                println("Timer started")
            },
            onTick = { remainingMillis, progress ->
                println("remaining=$remainingMillis progress=$progress")
            },
            onTimerComplete = {
                println("Timer completed")
            },
            onTimerCancel = {
                println("Timer cancelled")
            },
            onTimerPause = {
                println("Timer paused")
            },
            onTimerResume = {
                println("Timer resumed")
            },
            onTimerReset = {
                println("Timer reset")
            },
            onTimerRestart = {
                println("Timer restarted")
            },
            onStateChange = { status ->
                println("State changed to $status")
            },
        )

        Button(onClick = timerState::start) { Text("Start") }
        Button(onClick = timerState::pause) { Text("Pause") }
        Button(onClick = timerState::resume) { Text("Resume") }
        Button(onClick = timerState::cancel) { Text("Cancel") }
        Button(onClick = timerState::reset) { Text("Reset") }
        Button(onClick = timerState::restart) { Text("Restart") }
    }
}
```

Compose customization checklist:

- Behavior: `TimerButtonConfig(durationMillis, autoStart, clickStartsTimer, allowClickWhileRunning, progressDirection, progressMode)`
- State: `rememberTimerButtonState(...)` exposes `start()`, `pause()`, `resume()`, `cancel()`, `reset()`, `restart()`, `remainingMillis`, `elapsedMillis`, `progress`, and `timerState`
- Styling: `colors`, `shape`, `border`, `elevation`, `progressAlpha`, `contentPadding`, `textStyle`, `leadingIcon`, and `modifier`
- Events: `onClick`, `onTimerStart`, `onTick`, `onTimerComplete`, `onTimerCancel`, `onTimerPause`, `onTimerResume`, `onTimerReset`, `onTimerRestart`, and `onStateChange`

## XML: Minimal Usage

Add the `app` namespace and place `TimerButtonView` in any normal layout.

```xml
<LinearLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="vertical">

    <com.goeslocal.timerbutton.TimerButtonView
        android:id="@+id/resendButton"
        android:layout_width="220dp"
        android:layout_height="56dp"
        android:text="Resend OTP"
        app:timerDuration="30000"
        app:timerTextIdle="Resend OTP"
        app:timerTextRunning="Resend in %ss"
        app:timerTextCompleted="Resend now" />
</LinearLayout>
```

Then attach your click action:

```kotlin
binding.resendButton.setOnClickListener {
    viewModel.resendOtp()
}
```

What happens here:

- First tap calls your click listener and starts the timer.
- While running, additional taps are blocked by default.
- `%s` or `%d` in `timerTextRunning` is replaced with remaining seconds.
- The button automatically redraws progress until completion.

## XML: Fully Customizable Usage

Use XML attributes for styling and behavior, then use `TimerButtonListener` plus public control methods from Kotlin.

```xml
<com.goeslocal.timerbutton.TimerButtonView
    android:id="@+id/downloadTimerButton"
    android:layout_width="240dp"
    android:layout_height="56dp"
    android:text="Download report"
    android:textStyle="bold"
    app:timerDuration="8000"
    app:timerAutoStart="false"
    app:timerClickStartsTimer="false"
    app:timerAllowClickWhileRunning="true"
    app:timerTextIdle="Download report"
    app:timerTextRunning="Downloading %ss"
    app:timerTextCompleted="Download again"
    app:timerButtonBackgroundColor="#172033"
    app:timerButtonDisabledColor="#E5E7EB"
    app:timerTextColor="#FFFFFF"
    app:timerProgressColor="#7DA2FF"
    app:timerProgressAlpha="0.42"
    app:timerProgressDirection="leftToRight"
    app:timerProgressMode="overlay"
    app:timerCornerRadius="18dp"
    app:timerStrokeColor="#7DA2FF"
    app:timerStrokeWidth="1dp" />
```

```kotlin
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.goeslocal.timerbutton.TimerButtonListener
import com.goeslocal.timerbutton.TimerButtonStatus

class DownloadFragment : Fragment(R.layout.download_screen) {
    private var _binding: DownloadScreenBinding? = null
    private val binding get() = checkNotNull(_binding)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _binding = DownloadScreenBinding.bind(view)

        binding.downloadTimerButton.setTimerListener(
            object : TimerButtonListener {
                override fun onTimerStart() {
                    println("Timer started")
                }

                override fun onTick(remainingMillis: Long, progress: Float) {
                    println("remaining=$remainingMillis progress=$progress")
                }

                override fun onTimerComplete() {
                    println("Timer completed")
                }

                override fun onTimerCancel() {
                    println("Timer cancelled")
                }

                override fun onTimerPause() {
                    println("Timer paused")
                }

                override fun onTimerResume() {
                    println("Timer resumed")
                }

                override fun onTimerReset() {
                    println("Timer reset")
                }

                override fun onTimerRestart() {
                    println("Timer restarted")
                }

                override fun onStateChange(status: TimerButtonStatus) {
                    println("State changed to $status")
                }
            },
        )

        binding.downloadTimerButton.setOnClickListener {
            println("Button clicked")
        }

        binding.startButton.setOnClickListener { binding.downloadTimerButton.start() }
        binding.pauseButton.setOnClickListener { binding.downloadTimerButton.pause() }
        binding.resumeButton.setOnClickListener { binding.downloadTimerButton.resume() }
        binding.cancelButton.setOnClickListener { binding.downloadTimerButton.cancel() }
        binding.resetButton.setOnClickListener { binding.downloadTimerButton.reset() }
        binding.restartButton.setOnClickListener { binding.downloadTimerButton.restart() }
    }

    override fun onDestroyView() {
        binding.downloadTimerButton.release()
        _binding = null
        super.onDestroyView()
    }
}
```

XML customization checklist:

- Behavior: `timerDuration`, `timerAutoStart`, `timerClickStartsTimer`, `timerAllowClickWhileRunning`
- Text: `android:text`, `timerTextIdle`, `timerTextRunning`, `timerTextCompleted`
- Styling: `timerButtonBackgroundColor`, `timerButtonDisabledColor`, `timerTextColor`, `timerProgressColor`, `timerProgressAlpha`, `timerCornerRadius`, `timerStrokeColor`, `timerStrokeWidth`
- Progress: `timerProgressDirection` and `timerProgressMode`
- Controls: `start()`, `pause()`, `resume()`, `cancel()`, `reset()`, `restart()`, `setDuration(...)`, `setTimerListener(...)`, and `release()`

## Progress Directions And Modes

Progress directions:

- Compose: `TimerProgressDirection.LeftToRight`, `RightToLeft`, `TopToBottom`, `BottomToTop`
- XML: `leftToRight`, `rightToLeft`, `topToBottom`, `bottomToTop`

Progress modes:

- Compose: `TimerProgressMode.Overlay`, `Background`, `Underline`
- XML: `overlay`, `background`, `underline`

## Timer States

Both Compose and XML use the same public states:

- `Idle`: no timer has started, or the timer was reset
- `Running`: timer is actively counting down
- `Paused`: timer is stopped at its current progress and can resume
- `Completed`: timer reached the end
- `Cancelled`: timer was stopped before completion

## Lifecycle Behavior

Compose:

- `TimerButton(...)` creates a saveable state internally. Use `rememberTimerButtonState(...)` when you need to control the same state yourself.
- Timer ticking runs in a `LaunchedEffect`, so it is automatically cancelled when the composable leaves composition. You do not need to manually stop a coroutine.
- State is saved with `rememberSaveable`, so normal Activity recreation, such as rotation or theme change, restores remaining time, elapsed time, progress, and status.
- Callback lambdas are read through `rememberUpdatedState`, so recomposition uses the latest lambdas without restarting the timer.
- You do not need lifecycle code for ordinary Compose usage.

XML/View:

- `TimerButtonView` starts drawing ticks only while it is attached to a window.
- When detached, it removes posted animation callbacks and cancels an active timer. This prevents a detached View from continuing to schedule UI work.
- In an Activity, you usually do not need extra cleanup.
- In a Fragment, call `release()` in `onDestroyView()` when the listener or click lambda captures the Fragment binding, the Fragment, or any view reference. This clears listener references early and avoids holding an old view tree.
- XML state is not automatically restored after Activity recreation. If the cooldown matters across rotation or process death, store the authoritative deadline in your ViewModel, repository, saved state, or backend, then start/update the button from that state when the view is recreated.

Important production rule:

TimerButton is UI state, not security or business-rule state. For OTP cooldowns, billing windows, auth lockouts, rate limits, and retry policies, keep the authoritative timestamp outside the button. Let TimerButton display the countdown and handle local interaction.

## API Reference

Compose functions:

- `TimerButton(text, durationMillis, ...)`: easiest Compose entry point
- `TimerButton(state, text, ...)`: controlled Compose entry point
- `rememberTimerButtonState(durationMillis)`: creates a saveable state object
- `TimerButtonConfig(...)`: behavior and progress configuration
- `TimerButtonColors(...)`: color configuration

View class:

- `TimerButtonView`: XML and classic Android View implementation
- `TimerButtonListener`: optional lifecycle callback interface

## More Documentation

- [Usage Guide](docs/implementation-guide.md): deeper Compose, XML, callbacks, lifecycle, testing, and production guidance
- [Roadmap](ROADMAP.md): upcoming work and non-goals
- [Contributing](CONTRIBUTING.md): local checks and release process
- [Wiki Pages](docs/wiki/README.md): advanced recipes, architecture notes, and media capture
- [Release Guide](docs/release.md): Maven Central setup for maintainers

## Development

Run tests and checks:

```bash
./gradlew check
```

Publish a release:

```bash
./gradlew publishAndReleaseToMavenCentral
```

Regenerate README screenshots from a connected Android device:

```bash
python3 scripts/capture_readme_media.py
```

## License

Apache License 2.0. See [LICENSE](LICENSE).
