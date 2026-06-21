package com.goeslocal.timerbutton

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/**
 * Compose color configuration for [TimerButton].
 */
@Immutable
data class TimerButtonColors(
    val containerColor: Color,
    val contentColor: Color,
    val progressColor: Color,
    val disabledContainerColor: Color,
    val disabledContentColor: Color,
    val borderColor: Color = Color.Transparent,
)

