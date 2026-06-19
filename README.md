# TimerButton

TimerButton is a Kotlin-first Android library for Material-style buttons with timed progress. It supports Jetpack Compose and XML/View-based Android apps from a separate reusable `:timerbutton` module.

## Features

- Compose `TimerButton(...)` and `rememberTimerButtonState(...)`
- XML `TimerButtonView`
- Start, pause, resume, cancel, reset, and restart controls
- Accurate monotonic-clock timer engine
- Completion, tick, state change, and lifecycle callbacks
- Overlay, background, and underline progress modes
- Left-to-right, right-to-left, top-to-bottom, and bottom-to-top progress directions
- Custom colors, rounded corners, stroke, disabled state, icons, and formatted remaining-time text
- Unit-tested timer engine with fake clock coverage

## Installation

This project is ready to publish later to Maven Central or JitPack. For local development, include the module:

```kotlin
dependencies {
    implementation(project(":timerbutton"))
}
```

## Compose Basic Usage

```kotlin
TimerButton(
    text = "Start Timer",
    durationMillis = 10_000L,
    onTimerComplete = {
        println("Timer completed")
    }
)
```

## Compose State Usage

```kotlin
val timerState = rememberTimerButtonState(
    durationMillis = 30_000L
)

Column {
    TimerButton(
        state = timerState,
        text = "Resend OTP"
    )

    Button(onClick = { timerState.start() }) {
        Text("Start")
    }

    Button(onClick = { timerState.cancel() }) {
        Text("Cancel")
    }
}
```

## XML Usage

```xml
<com.goeslocal.timerbutton.TimerButtonView
    android:id="@+id/resendButton"
    android:layout_width="match_parent"
    android:layout_height="56dp"
    android:text="Resend OTP"
    app:timerDuration="30000"
    app:timerProgressColor="@color/purple_500"
    app:timerProgressDirection="leftToRight"
    app:timerProgressMode="overlay" />
```

## XML Kotlin Usage

```kotlin
binding.resendButton.setTimerListener(
    object : TimerButtonListener {
        override fun onTimerComplete() {
            Toast.makeText(this@MainActivity, "Completed", Toast.LENGTH_SHORT).show()
        }
    }
)

binding.resendButton.start()
```

## Customization

Compose supports `TimerButtonConfig`, `TimerButtonColors`, `shape`, `border`, `elevation`, `contentPadding`, `textStyle`, `leadingIcon`, and `textFormatter`.

XML supports custom attributes in `attrs.xml`, including duration, progress color, alpha, direction, mode, auto start, click behavior, idle/running/completed text, corner radius, stroke, background color, disabled color, and text color.

## Callbacks

Both APIs support:

```kotlin
onTimerStart()
onTick(remainingMillis, progress)
onTimerComplete()
onTimerCancel()
onTimerPause()
onTimerResume()
onTimerReset()
onTimerRestart()
onStateChange(status)
```

## Demo Screenshots

Screenshots can be added here after publishing sample captures from the demo app.

## Contributing

Keep changes focused, add tests for timer behavior, and avoid dependencies unless they materially improve the public API or reliability.

## License

Apache License 2.0. See [LICENSE](LICENSE).

