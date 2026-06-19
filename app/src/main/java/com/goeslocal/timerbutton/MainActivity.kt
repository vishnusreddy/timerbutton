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
                FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    TimerButton(
                        text = "Natural size",
                        durationMillis = 4_000L,
                        onTimerComplete = { lastEvent = "Basic timer completed" },
                    )
                    TimerButton(
                        text = "Auto-start",
                        durationMillis = 5_000L,
                        modifier = Modifier.width(168.dp).height(44.dp),
                        config = TimerButtonConfig(durationMillis = 5_000L, autoStart = true),
                        textFormatter = { state, label -> if (state.isRunning) "${state.remainingMillis / 1000 + 1}s left" else label },
                    )
                    TimerButton(
                        text = "Disabled",
                        durationMillis = 4_000L,
                        enabled = false,
                        modifier = Modifier.width(128.dp).height(48.dp),
                    )
                }
                TimerButton(
                    text = "Resend OTP",
                    durationMillis = 6_000L,
                    modifier = Modifier.width(220.dp).height(56.dp),
                    textFormatter = { state, label ->
                        if (state.isRunning) "Resend in ${(state.remainingMillis + 999) / 1000}s" else label
                    },
                )
                TimerButton(
                    text = "Download",
                    durationMillis = 5_000L,
                    modifier = Modifier.width(260.dp).height(64.dp),
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
                    text = "Full-width CTA when you actually want one",
                    durationMillis = 7_000L,
                    modifier = Modifier.fillMaxWidth().height(54.dp),
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
                    )
                    TimerButton(
                        text = "Tall",
                        durationMillis = 4_500L,
                        modifier = Modifier.width(132.dp).height(76.dp),
                        shape = RoundedCornerShape(18.dp),
                    )
                    TimerButton(
                        text = "Pill",
                        durationMillis = 5_000L,
                        modifier = Modifier.width(156.dp).height(48.dp),
                        shape = RoundedCornerShape(50),
                    )
                    TimerButton(
                        text = "Square-ish",
                        durationMillis = 4_000L,
                        modifier = Modifier.size(92.dp),
                        shape = RoundedCornerShape(14.dp),
                        contentPadding = PaddingValues(6.dp),
                    )
                }
                TimerButton(
                    text = "Wide but shorter",
                    durationMillis = 5_000L,
                    modifier = Modifier.width(300.dp).height(42.dp),
                    contentPadding = PaddingValues(horizontal = 18.dp, vertical = 6.dp),
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
                FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    TimerProgressDirection.entries.forEachIndexed { index, direction ->
                        TimerButton(
                            text = direction.name,
                            durationMillis = 4_000L,
                            modifier = Modifier
                                .width(if (index % 2 == 0) 190.dp else 230.dp)
                                .height(if (direction.name.contains("Top") || direction.name.contains("Bottom")) 72.dp else 48.dp),
                            config = TimerButtonConfig(durationMillis = 4_000L, progressDirection = direction),
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
                    modifier = Modifier.width(220.dp).height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                )
                TimerButton(
                    text = "Outlined button",
                    durationMillis = 5_000L,
                    modifier = Modifier.width(210.dp).height(48.dp),
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
                TimerButton(
                    text = "Text padding",
                    durationMillis = 5_000L,
                    modifier = Modifier.width(176.dp).height(44.dp),
                    contentPadding = PaddingValues(horizontal = 28.dp, vertical = 6.dp),
                    colors = TimerButtonDefaults.colors().copy(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        progressColor = MaterialTheme.colorScheme.secondary,
                    ),
                    elevation = 0.dp,
                )
            }
        }

        item {
            DemoSection("Multiple Timers") {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    TimerButton("First timer", 3_000L, Modifier.width(152.dp).height(48.dp))
                    TimerButton("Second timer", 5_000L, Modifier.width(216.dp).height(56.dp))
                    TimerButton("Third timer", 7_000L, Modifier.width(180.dp).height(68.dp))
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
