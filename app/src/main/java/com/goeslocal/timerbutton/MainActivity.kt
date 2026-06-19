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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
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
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
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
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("TimerButton", style = MaterialTheme.typography.headlineMedium)
                Text(
                    "Compose and XML examples backed by the reusable library module.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        item {
            DemoSection("Basic, Auto-start, OTP, Download") {
                TimerButton(
                    text = "Basic timer",
                    durationMillis = 4_000L,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    onTimerComplete = { lastEvent = "Basic timer completed" },
                )
                TimerButton(
                    text = "Auto-start",
                    durationMillis = 5_000L,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    config = TimerButtonConfig(durationMillis = 5_000L, autoStart = true),
                    textFormatter = { state, label -> if (state.isRunning) "${state.remainingMillis / 1000 + 1}s left" else label },
                )
                TimerButton(
                    text = "Resend OTP",
                    durationMillis = 6_000L,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    textFormatter = { state, label ->
                        if (state.isRunning) "Resend in ${(state.remainingMillis + 999) / 1000}s" else label
                    },
                )
                TimerButton(
                    text = "Download",
                    durationMillis = 5_000L,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    leadingIcon = {
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .background(Color.White.copy(alpha = 0.22f), CircleShape),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text("D", style = MaterialTheme.typography.labelSmall)
                        }
                    },
                )
                TimerButton(
                    text = "Disabled",
                    durationMillis = 4_000L,
                    enabled = false,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
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
                TimerProgressDirection.entries.forEach { direction ->
                    TimerButton(
                        text = direction.name,
                        durationMillis = 4_000L,
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        config = TimerButtonConfig(durationMillis = 4_000L, progressDirection = direction),
                    )
                }
                TimerProgressMode.entries.forEach { mode ->
                    TimerButton(
                        text = "${mode.name} mode",
                        durationMillis = 4_000L,
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        config = TimerButtonConfig(durationMillis = 4_000L, progressMode = mode),
                    )
                }
            }
        }

        item {
            DemoSection("Custom, Rounded, Outlined") {
                TimerButton(
                    text = "Custom colors",
                    durationMillis = 5_000L,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    colors = TimerButtonColors(
                        containerColor = Color(0xFF0B6E4F),
                        contentColor = Color.White,
                        progressColor = Color(0xFFFFC857),
                        disabledContainerColor = Color.LightGray,
                        disabledContentColor = Color.DarkGray,
                    ),
                )
                TimerButton(
                    text = "Rounded button",
                    durationMillis = 5_000L,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(28.dp),
                )
                TimerButton(
                    text = "Outlined button",
                    durationMillis = 5_000L,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    colors = TimerButtonColors(
                        containerColor = Color.Transparent,
                        contentColor = MaterialTheme.colorScheme.primary,
                        progressColor = MaterialTheme.colorScheme.primary,
                        disabledContainerColor = Color.Transparent,
                        disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                    progressAlpha = 0.12f,
                )
            }
        }

        item {
            DemoSection("Multiple Timers") {
                TimerButton("First timer", 3_000L, Modifier.fillMaxWidth().height(48.dp))
                TimerButton("Second timer", 5_000L, Modifier.fillMaxWidth().height(48.dp))
                TimerButton("Third timer", 7_000L, Modifier.fillMaxWidth().height(48.dp))
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
private fun DemoSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        content()
    }
}

@Preview(showBackground = true)
@Composable
fun TimerButtonDemoPreview() {
    TimerbuttonTheme {
        TimerButtonDemo()
    }
}
