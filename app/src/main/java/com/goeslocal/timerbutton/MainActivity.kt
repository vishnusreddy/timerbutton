package com.goeslocal.timerbutton

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.goeslocal.timerbutton.ui.theme.TimerbuttonTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TimerbuttonTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = MaterialTheme.colorScheme.background,
                ) { innerPadding ->
                    TimerButtonDemo(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TimerButtonDemo(modifier: Modifier = Modifier) {
    val pauseState = rememberTimerButtonState(8_000L)
    val resetState = rememberTimerButtonState(6_000L)
    var lastEvent by remember { mutableStateOf("No events yet") }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(22.dp),
    ) {
        item {
            ShowcaseHeader()
        }

        item {
            DemoSection("Showcase") {
                TimerButton(
                    text = "Continue securely",
                    durationMillis = 8_000L,
                    modifier = Modifier.fillMaxWidth().height(58.dp),
                    colors = ShowcaseColors.primary(),
                    progressAlpha = 0.34f,
                    shape = RoundedCornerShape(18.dp),
                    textFormatter = { state, label ->
                        if (state.isRunning) "Continuing ${(state.progress * 100).toInt()}%" else label
                    },
                )

                FlowRow(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    TimerButton(
                        text = "Resend OTP",
                        durationMillis = 6_000L,
                        modifier = Modifier.width(178.dp).height(50.dp),
                        colors = ShowcaseColors.teal(),
                        shape = RoundedCornerShape(14.dp),
                        textFormatter = { state, label ->
                            if (state.isRunning) "${(state.remainingMillis + 999) / 1000}s left" else label
                        },
                    )
                    TimerButton(
                        text = "Sync data",
                        durationMillis = 5_000L,
                        modifier = Modifier.width(154.dp).height(50.dp),
                        colors = ShowcaseColors.amber(),
                        progressAlpha = 0.30f,
                        shape = RoundedCornerShape(14.dp),
                    )
                }

                TimerButton(
                    text = "Download report",
                    durationMillis = 7_000L,
                    modifier = Modifier.width(270.dp).height(62.dp),
                    colors = ShowcaseColors.slate(),
                    progressAlpha = 0.42f,
                    shape = RoundedCornerShape(20.dp),
                    leadingIcon = {
                        DemoIcon("D", Color.White.copy(alpha = 0.18f))
                    },
                )

                FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    TimerButton(
                        text = "Compact",
                        durationMillis = 4_000L,
                        modifier = Modifier.width(118.dp).height(38.dp),
                        colors = ShowcaseColors.primary(),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(10.dp),
                    )
                    TimerButton(
                        text = "Outlined",
                        durationMillis = 4_000L,
                        modifier = Modifier.width(132.dp).height(42.dp),
                        colors = ShowcaseColors.outlined(),
                        border = BorderStroke(1.dp, Color(0xFF7DA2FF)),
                        progressAlpha = 0.18f,
                        shape = RoundedCornerShape(12.dp),
                        elevation = 0.dp,
                    )
                    TimerButton(
                        text = "Pill",
                        durationMillis = 4_000L,
                        modifier = Modifier.width(128.dp).height(42.dp),
                        colors = ShowcaseColors.teal(),
                        shape = RoundedCornerShape(50),
                    )
                }
            }
        }

        item {
            DemoSection("Basic, Auto-start, OTP, Download") {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    TimerButton(
                        text = "Natural size",
                        durationMillis = 4_000L,
                        colors = ShowcaseColors.primary(),
                        onTimerComplete = { lastEvent = "Basic timer completed" },
                    )
                    TimerButton(
                        text = "Auto-start",
                        durationMillis = 5_000L,
                        modifier = Modifier.width(168.dp).height(44.dp),
                        config = TimerButtonConfig(durationMillis = 5_000L, autoStart = true),
                        colors = ShowcaseColors.slate(),
                        textFormatter = { state, label -> if (state.isRunning) "${state.remainingMillis / 1000 + 1}s left" else label },
                    )
                    TimerButton(
                        text = "Disabled",
                        durationMillis = 4_000L,
                        enabled = false,
                        modifier = Modifier.width(128.dp).height(48.dp),
                        colors = ShowcaseColors.primary(),
                    )
                }
                TimerButton(
                    text = "Resend OTP",
                    durationMillis = 6_000L,
                    modifier = Modifier.width(220.dp).height(56.dp),
                    colors = ShowcaseColors.teal(),
                    textFormatter = { state, label ->
                        if (state.isRunning) "Resend in ${(state.remainingMillis + 999) / 1000}s" else label
                    },
                )
                TimerButton(
                    text = "Download",
                    durationMillis = 5_000L,
                    modifier = Modifier.width(260.dp).height(64.dp),
                    colors = ShowcaseColors.slate(),
                    leadingIcon = {
                        DemoIcon("D", Color.White.copy(alpha = 0.18f))
                    },
                )
                TimerButton(
                    text = "Full-width CTA when you actually want one",
                    durationMillis = 7_000L,
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    colors = ShowcaseColors.primary(),
                )
            }
        }

        item {
            DemoSection("Sizing and Shape Gallery") {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    TimerButton(
                        text = "Compact",
                        durationMillis = 3_500L,
                        modifier = Modifier.width(116.dp).height(36.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ShowcaseColors.primary(),
                    )
                    TimerButton(
                        text = "Tall",
                        durationMillis = 4_500L,
                        modifier = Modifier.width(132.dp).height(76.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = ShowcaseColors.slate(),
                    )
                    TimerButton(
                        text = "Pill",
                        durationMillis = 5_000L,
                        modifier = Modifier.width(156.dp).height(48.dp),
                        shape = RoundedCornerShape(50),
                        colors = ShowcaseColors.teal(),
                    )
                    TimerButton(
                        text = "Square-ish",
                        durationMillis = 4_000L,
                        modifier = Modifier.size(92.dp),
                        shape = RoundedCornerShape(14.dp),
                        contentPadding = PaddingValues(6.dp),
                        colors = ShowcaseColors.amber(),
                    )
                }
                TimerButton(
                    text = "Wide but shorter",
                    durationMillis = 5_000L,
                    modifier = Modifier.width(300.dp).height(42.dp),
                    contentPadding = PaddingValues(horizontal = 18.dp, vertical = 6.dp),
                    colors = ShowcaseColors.primary(),
                )
            }
        }

        item {
            DemoSection("Pause, Resume, Cancel, Reset, Restart") {
                TimerButton(
                    state = pauseState,
                    text = "Pause / resume example",
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    config = TimerButtonConfig(durationMillis = 8_000L, clickStartsTimer = false, allowClickWhileRunning = true),
                    colors = ShowcaseColors.primary(),
                    textFormatter = { state, label ->
                        when {
                            state.isRunning -> "Running ${(state.progress * 100).toInt()}%"
                            state.isPaused -> "Paused at ${(state.progress * 100).toInt()}%"
                            else -> label
                        }
                    },
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { pauseState.start() }) { Text("Start") }
                    Button(onClick = { pauseState.pause() }) { Text("Pause") }
                    Button(onClick = { pauseState.resume() }) { Text("Resume") }
                }

                TimerButton(
                    state = resetState,
                    text = "Cancel / reset example",
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    config = TimerButtonConfig(durationMillis = 6_000L, clickStartsTimer = false),
                    colors = ShowcaseColors.slate(),
                    onTimerCancel = { lastEvent = "Timer cancelled" },
                    onTimerReset = { lastEvent = "Timer reset" },
                    onTimerRestart = { lastEvent = "Timer restarted" },
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { resetState.start() }) { Text("Start") }
                    Button(onClick = { resetState.cancel() }) { Text("Cancel") }
                    Button(onClick = { resetState.reset() }) { Text("Reset") }
                    Button(onClick = { resetState.restart() }) { Text("Restart") }
                }
            }
        }

        item {
            DemoSection("Directions and Modes") {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    TimerProgressDirection.entries.forEachIndexed { index, direction ->
                        TimerButton(
                            text = direction.name,
                            durationMillis = 4_000L,
                            modifier = Modifier
                                .width(if (index % 2 == 0) 190.dp else 230.dp)
                                .height(if (direction.name.contains("Top") || direction.name.contains("Bottom")) 72.dp else 48.dp),
                            config = TimerButtonConfig(durationMillis = 4_000L, progressDirection = direction),
                            colors = if (index % 2 == 0) ShowcaseColors.primary() else ShowcaseColors.teal(),
                        )
                    }
                }
                FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    TimerProgressMode.entries.forEach { mode ->
                        TimerButton(
                            text = "${mode.name} mode",
                            durationMillis = 4_000L,
                            modifier = Modifier.width(190.dp).height(50.dp),
                            config = TimerButtonConfig(durationMillis = 4_000L, progressMode = mode),
                            colors = ShowcaseColors.slate(),
                        )
                    }
                }
            }
        }

        item {
            DemoSection("Custom, Rounded, Outlined") {
                TimerButton(
                    text = "Custom colors",
                    durationMillis = 5_000L,
                    modifier = Modifier.width(248.dp).height(56.dp),
                    colors = TimerButtonColors(
                        containerColor = Color(0xFF0F766E),
                        contentColor = Color.White,
                        progressColor = Color(0xFF99F6E4),
                        disabledContainerColor = Color(0xFFE5E7EB),
                        disabledContentColor = Color(0xFF6B7280),
                    ),
                )
                TimerButton(
                    text = "Rounded button",
                    durationMillis = 5_000L,
                    modifier = Modifier.width(220.dp).height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ShowcaseColors.primary(),
                )
                TimerButton(
                    text = "Outlined button",
                    durationMillis = 5_000L,
                    modifier = Modifier.width(210.dp).height(48.dp),
                    colors = TimerButtonColors(
                        containerColor = Color.Transparent,
                        contentColor = Color(0xFF355DCE),
                        progressColor = Color(0xFF355DCE),
                        disabledContainerColor = Color.Transparent,
                        disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                    border = BorderStroke(1.dp, Color(0xFF355DCE)),
                    progressAlpha = 0.12f,
                    elevation = 0.dp,
                )
                TimerButton(
                    text = "Text padding",
                    durationMillis = 5_000L,
                    modifier = Modifier.width(176.dp).height(44.dp),
                    contentPadding = PaddingValues(horizontal = 28.dp, vertical = 6.dp),
                    colors = TimerButtonDefaults.colors().copy(
                        containerColor = Color(0xFF172033),
                        contentColor = Color(0xFFEAF0FF),
                        progressColor = Color(0xFF7DA2FF),
                    ),
                    elevation = 0.dp,
                )
            }
        }

        item {
            DemoSection("Multiple Timers") {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    TimerButton("First timer", 3_000L, Modifier.width(152.dp).height(48.dp), colors = ShowcaseColors.primary())
                    TimerButton("Second timer", 5_000L, Modifier.width(216.dp).height(56.dp), colors = ShowcaseColors.teal())
                    TimerButton("Third timer", 7_000L, Modifier.width(180.dp).height(68.dp), colors = ShowcaseColors.amber())
                }
            }
        }

        item {
            DemoSection("XML View Example") {
                AndroidView(
                    modifier = Modifier.fillMaxWidth(),
                    factory = { context ->
                        val view = LayoutInflater.from(context).inflate(R.layout.view_timer_button_demo, null)
                        view.findViewById<TimerButtonView>(R.id.xmlTimerButton).setTimerListener(
                            object : TimerButtonListener {
                                override fun onTimerComplete() {
                                    Toast.makeText(context, "XML timer complete", Toast.LENGTH_SHORT).show()
                                }
                            },
                        )
                        view
                    },
                )
            }
        }

        item {
            Text(
                text = "Last callback: $lastEvent",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 24.dp),
            )
        }
    }
}

@Composable
private fun ShowcaseHeader() {
    Column(
        modifier = Modifier.padding(top = 10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("TimerButton", style = MaterialTheme.typography.headlineLarge)
        Text(
            "Production-ready timed buttons for Compose and XML apps.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun DemoSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        content()
    }
}

@Composable
private fun DemoIcon(label: String, color: Color) {
    Box(
        modifier = Modifier
            .size(22.dp)
            .background(color, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = Color.White)
    }
}

private object ShowcaseColors {
    fun primary() = TimerButtonColors(
        containerColor = Color(0xFF2F6BFF),
        contentColor = Color.White,
        progressColor = Color(0xFFAFC6FF),
        disabledContainerColor = Color(0xFF202735),
        disabledContentColor = Color(0xFF7C879A),
    )

    fun teal() = TimerButtonColors(
        containerColor = Color(0xFF0F766E),
        contentColor = Color.White,
        progressColor = Color(0xFF99F6E4),
        disabledContainerColor = Color(0xFF202735),
        disabledContentColor = Color(0xFF7C879A),
    )

    fun amber() = TimerButtonColors(
        containerColor = Color(0xFFB45309),
        contentColor = Color.White,
        progressColor = Color(0xFFFDE68A),
        disabledContainerColor = Color(0xFF202735),
        disabledContentColor = Color(0xFF7C879A),
    )

    fun slate() = TimerButtonColors(
        containerColor = Color(0xFF172033),
        contentColor = Color(0xFFEAF0FF),
        progressColor = Color(0xFF7DA2FF),
        disabledContainerColor = Color(0xFF202735),
        disabledContentColor = Color(0xFF7C879A),
    )

    fun outlined() = TimerButtonColors(
        containerColor = Color.Transparent,
        contentColor = Color(0xFF355DCE),
        progressColor = Color(0xFF355DCE),
        disabledContainerColor = Color.Transparent,
        disabledContentColor = Color(0xFF7C879A),
    )
}

@Preview(showBackground = true)
@Composable
fun TimerButtonDemoPreview() {
    TimerbuttonTheme {
        TimerButtonDemo()
    }
}
