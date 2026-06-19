package com.goeslocal.timerbutton

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

/**
 * Creates and remembers a [TimerButtonState].
 */
@Composable
fun rememberTimerButtonState(durationMillis: Long = 10_000L): TimerButtonState {
    val state = remember { TimerButtonState(durationMillis) }
    LaunchedEffect(durationMillis) {
        state.setDuration(durationMillis)
    }
    return state
}

/**
 * Material-style Compose button with elapsed-time progress rendering.
 */
@Composable
fun TimerButton(
    text: String,
    durationMillis: Long,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    config: TimerButtonConfig = TimerButtonConfig(durationMillis = durationMillis),
    onClick: () -> Unit = {},
    onTimerStart: () -> Unit = {},
    onTick: (remainingMillis: Long, progress: Float) -> Unit = { _, _ -> },
    onTimerComplete: () -> Unit = {},
    onTimerCancel: () -> Unit = {},
    onTimerPause: () -> Unit = {},
    onTimerResume: () -> Unit = {},
    onTimerReset: () -> Unit = {},
    onTimerRestart: () -> Unit = {},
    onStateChange: (TimerButtonStatus) -> Unit = {},
    textFormatter: ((TimerButtonState, String) -> String)? = null,
    leadingIcon: (@Composable () -> Unit)? = null,
    shape: Shape = RoundedCornerShape(12.dp),
    colors: TimerButtonColors = TimerButtonDefaults.colors(),
    progressAlpha: Float = 0.32f,
    border: BorderStroke? = null,
    elevation: Dp = 2.dp,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    textStyle: TextStyle = MaterialTheme.typography.labelLarge,
) {
    val state = rememberTimerButtonState(durationMillis)
    TimerButton(
        state = state,
        text = text,
        modifier = modifier,
        enabled = enabled,
        config = config.copy(durationMillis = durationMillis),
        onClick = onClick,
        onTimerStart = onTimerStart,
        onTick = onTick,
        onTimerComplete = onTimerComplete,
        onTimerCancel = onTimerCancel,
        onTimerPause = onTimerPause,
        onTimerResume = onTimerResume,
        onTimerReset = onTimerReset,
        onTimerRestart = onTimerRestart,
        onStateChange = onStateChange,
        textFormatter = textFormatter,
        leadingIcon = leadingIcon,
        shape = shape,
        colors = colors,
        progressAlpha = progressAlpha,
        border = border,
        elevation = elevation,
        contentPadding = contentPadding,
        textStyle = textStyle,
    )
}

/**
 * Material-style Compose button controlled by an external [TimerButtonState].
 */
@Composable
fun TimerButton(
    state: TimerButtonState,
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    config: TimerButtonConfig = TimerButtonConfig(durationMillis = state.durationMillis),
    onClick: () -> Unit = {},
    onTimerStart: () -> Unit = {},
    onTick: (remainingMillis: Long, progress: Float) -> Unit = { _, _ -> },
    onTimerComplete: () -> Unit = {},
    onTimerCancel: () -> Unit = {},
    onTimerPause: () -> Unit = {},
    onTimerResume: () -> Unit = {},
    onTimerReset: () -> Unit = {},
    onTimerRestart: () -> Unit = {},
    onStateChange: (TimerButtonStatus) -> Unit = {},
    textFormatter: ((TimerButtonState, String) -> String)? = null,
    leadingIcon: (@Composable () -> Unit)? = null,
    shape: Shape = RoundedCornerShape(12.dp),
    colors: TimerButtonColors = TimerButtonDefaults.colors(),
    progressAlpha: Float = 0.32f,
    border: BorderStroke? = null,
    elevation: Dp = 2.dp,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    textStyle: TextStyle = MaterialTheme.typography.labelLarge,
) {
    val clickCallback by rememberUpdatedState(onClick)
    val startCallback by rememberUpdatedState(onTimerStart)
    val tickCallback by rememberUpdatedState(onTick)
    val completeCallback by rememberUpdatedState(onTimerComplete)
    val cancelCallback by rememberUpdatedState(onTimerCancel)
    val pauseCallback by rememberUpdatedState(onTimerPause)
    val resumeCallback by rememberUpdatedState(onTimerResume)
    val resetCallback by rememberUpdatedState(onTimerReset)
    val restartCallback by rememberUpdatedState(onTimerRestart)
    val stateChangeCallback by rememberUpdatedState(onStateChange)

    SideEffect {
        state.onTimerStart = startCallback
        state.onTick = tickCallback
        state.onTimerComplete = completeCallback
        state.onTimerCancel = cancelCallback
        state.onTimerPause = pauseCallback
        state.onTimerResume = resumeCallback
        state.onTimerReset = resetCallback
        state.onTimerRestart = restartCallback
        state.onStateChange = stateChangeCallback
    }

    LaunchedEffect(config.durationMillis) {
        state.setDuration(config.durationMillis)
    }
    LaunchedEffect(config.autoStart) {
        if (config.autoStart && state.timerState == TimerButtonStatus.Idle) {
            state.start()
        }
    }
    LaunchedEffect(state.timerState) {
        while (state.timerState == TimerButtonStatus.Running) {
            state.tick()
            delay(16L)
        }
    }
    DisposableEffect(state) {
        onDispose {
            if (state.isRunning || state.isPaused) {
                state.cancel()
            }
        }
    }

    val displayText = textFormatter?.invoke(state, text) ?: text
    val buttonEnabled = enabled && (config.allowClickWhileRunning || !state.isRunning)
    val baseContainerColor = if (enabled) colors.containerColor else colors.disabledContainerColor
    val materialColors = ButtonDefaults.buttonColors(
        containerColor = Color.Transparent,
        contentColor = colors.contentColor,
        disabledContainerColor = Color.Transparent,
        disabledContentColor = colors.disabledContentColor,
    )

    Box(
        modifier = modifier
            .clip(shape)
            .background(baseContainerColor)
            .semantics {
                progressBarRangeInfo = ProgressBarRangeInfo(state.progress, 0f..1f)
            },
        propagateMinConstraints = true,
    ) {
        if (config.progressMode == TimerProgressMode.Background && state.progress > 0f) {
            ProgressLayer(
                progress = state.progress,
                direction = config.progressDirection,
                mode = config.progressMode,
                shape = shape,
                colors = colors,
                alpha = progressAlpha,
            )
        }

        Button(
            onClick = {
                if (config.clickStartsTimer) {
                    when (state.timerState) {
                        TimerButtonStatus.Running -> Unit
                        TimerButtonStatus.Paused -> state.resume()
                        TimerButtonStatus.Idle,
                        TimerButtonStatus.Cancelled,
                        TimerButtonStatus.Completed -> state.start()
                    }
                }
                clickCallback()
            },
            enabled = buttonEnabled,
            shape = shape,
            colors = materialColors,
            elevation = ButtonDefaults.buttonElevation(defaultElevation = elevation),
            border = border,
            contentPadding = contentPadding,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (leadingIcon != null) {
                    leadingIcon()
                    Box(Modifier.width(8.dp))
                }
                Text(displayText, style = textStyle)
            }
        }

        if (config.progressMode != TimerProgressMode.Background && state.progress > 0f) {
            ProgressLayer(
                progress = state.progress,
                direction = config.progressDirection,
                mode = config.progressMode,
                shape = shape,
                colors = colors,
                alpha = progressAlpha,
            )
        }
    }
}

/**
 * Factory helpers for Compose TimerButton defaults.
 */
object TimerButtonDefaults {
    /**
     * Returns Material 3 color defaults for [TimerButton].
     */
    @Composable
    fun colors(): TimerButtonColors = TimerButtonColors(
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        progressColor = MaterialTheme.colorScheme.tertiary,
        disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
        disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
    )
}

@Composable
private fun BoxScope.ProgressLayer(
    progress: Float,
    direction: TimerProgressDirection,
    mode: TimerProgressMode,
    shape: Shape,
    colors: TimerButtonColors,
    alpha: Float,
) {
    val normalized = progress.coerceIn(0f, 1f)
    val align = when (direction) {
        TimerProgressDirection.LeftToRight -> Alignment.CenterStart
        TimerProgressDirection.RightToLeft -> Alignment.CenterEnd
        TimerProgressDirection.TopToBottom -> Alignment.TopCenter
        TimerProgressDirection.BottomToTop -> Alignment.BottomCenter
    }
    val progressModifier = when (direction) {
        TimerProgressDirection.LeftToRight,
        TimerProgressDirection.RightToLeft -> Modifier
            .fillMaxHeight()
            .fillMaxWidth(normalized)
        TimerProgressDirection.TopToBottom,
        TimerProgressDirection.BottomToTop -> Modifier
            .fillMaxWidth()
            .fillMaxHeight(normalized)
    }

    Box(Modifier.matchParentSize().clip(shape)) {
        if (mode == TimerProgressMode.Underline) {
            val underlineModifier = when (direction) {
                TimerProgressDirection.LeftToRight -> Modifier
                    .align(Alignment.BottomStart)
                    .height(4.dp)
                    .fillMaxWidth(normalized)
                TimerProgressDirection.RightToLeft -> Modifier
                    .align(Alignment.BottomEnd)
                    .height(4.dp)
                    .fillMaxWidth(normalized)
                TimerProgressDirection.TopToBottom -> Modifier
                    .align(Alignment.TopCenter)
                    .width(4.dp)
                    .fillMaxHeight(normalized)
                TimerProgressDirection.BottomToTop -> Modifier
                    .align(Alignment.BottomCenter)
                    .width(4.dp)
                    .fillMaxHeight(normalized)
            }
            Box(underlineModifier.background(colors.progressColor.copy(alpha = alpha)))
        } else {
            Box(
                modifier = progressModifier
                    .align(align)
                    .background(colors.progressColor.copy(alpha = alpha))
                    .then(
                        if (colors.borderColor != Color.Transparent) {
                            Modifier.border(1.dp, colors.borderColor, shape)
                        } else {
                            Modifier
                        },
                    ),
            )
        }
    }
}
