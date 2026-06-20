package com.goeslocal.timerbutton

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.os.SystemClock
import android.text.format.DateUtils
import android.util.AttributeSet
import android.view.Gravity
import android.widget.TextView
import androidx.core.content.withStyledAttributes
import com.goeslocal.timerbutton.lib.R
import kotlin.math.roundToInt

/**
 * XML/View implementation of TimerButton.
 *
 * This view owns no Activity or Fragment references. Call [release] if you want to explicitly
 * clear listeners before the view detaches.
 */
class TimerButtonView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = android.R.attr.textViewStyle,
) : TextView(context, attrs, defStyleAttr) {
    private val engine = TimerButtonEngine(10_000L, TimerClock { SystemClock.elapsedRealtime() })
    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val progressPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
    }
    private val bounds = RectF()
    private val progressBounds = RectF()

    private var listener: TimerButtonListener? = null
    private var progressDirection = TimerProgressDirection.LeftToRight
    private var progressMode = TimerProgressMode.Overlay
    private var progressAlpha = 0.32f
    private var autoStart = false
    private var clickStartsTimer = true
    private var allowClickWhileRunning = false
    private var idleText: String? = null
    private var runningText: String? = null
    private var completedText: String? = null
    private var cornerRadius = dp(12f)
    private var strokeWidth = 0f
    private var backgroundColor = Color.rgb(98, 0, 238)
    private var disabledColor = Color.rgb(190, 190, 190)
    private var progressColor = Color.rgb(3, 218, 198)
    private var strokeColor = Color.TRANSPARENT
    private var userTextColor = Color.WHITE
    private var defaultText = ""

    private val ticker = object : Runnable {
        override fun run() {
            if (engine.status != TimerButtonStatus.Running) return
            engine.tick()
            syncText()
            listener?.onTick(engine.remainingMillis, engine.progress)
            invalidate()
            if (engine.consumeCompletion()) {
                syncText()
                listener?.onStateChange(TimerButtonStatus.Completed)
                listener?.onTimerComplete()
                return
            }
            postOnAnimation(this)
        }
    }

    init {
        gravity = Gravity.CENTER
        isClickable = true
        isFocusable = true
        minHeight = dp(48f).roundToInt()
        setPadding(dp(16f).roundToInt(), paddingTop, dp(16f).roundToInt(), paddingBottom)

        context.withStyledAttributes(attrs, R.styleable.TimerButtonView, defStyleAttr, 0) {
            setDuration(getInt(R.styleable.TimerButtonView_timerDuration, 10_000).toLong())
            progressColor = getColor(R.styleable.TimerButtonView_timerProgressColor, progressColor)
            progressAlpha = getFloat(R.styleable.TimerButtonView_timerProgressAlpha, progressAlpha)
            progressDirection = directionFromOrdinal(getInt(R.styleable.TimerButtonView_timerProgressDirection, 0))
            progressMode = modeFromOrdinal(getInt(R.styleable.TimerButtonView_timerProgressMode, 0))
            autoStart = getBoolean(R.styleable.TimerButtonView_timerAutoStart, false)
            clickStartsTimer = getBoolean(R.styleable.TimerButtonView_timerClickStartsTimer, true)
            allowClickWhileRunning = getBoolean(R.styleable.TimerButtonView_timerAllowClickWhileRunning, false)
            idleText = getString(R.styleable.TimerButtonView_timerTextIdle)
            runningText = getString(R.styleable.TimerButtonView_timerTextRunning)
            completedText = getString(R.styleable.TimerButtonView_timerTextCompleted)
            cornerRadius = getDimension(R.styleable.TimerButtonView_timerCornerRadius, cornerRadius)
            strokeColor = getColor(R.styleable.TimerButtonView_timerStrokeColor, strokeColor)
            strokeWidth = getDimension(R.styleable.TimerButtonView_timerStrokeWidth, strokeWidth)
            backgroundColor = getColor(R.styleable.TimerButtonView_timerButtonBackgroundColor, backgroundColor)
            disabledColor = getColor(R.styleable.TimerButtonView_timerButtonDisabledColor, disabledColor)
            userTextColor = getColor(R.styleable.TimerButtonView_timerTextColor, currentTextColor)
        }
        defaultText = text?.toString().orEmpty()
        setTextColor(userTextColor)
        idleText?.let(::setText)
        background = null

        super.setOnClickListener {
            val wasRunningBeforeClick = engine.status == TimerButtonStatus.Running
            if (clickStartsTimer) {
                when (engine.status) {
                    TimerButtonStatus.Idle,
                    TimerButtonStatus.Cancelled,
                    TimerButtonStatus.Completed -> start()
                    TimerButtonStatus.Paused -> resume()
                    TimerButtonStatus.Running -> Unit
                }
            }
            if (allowClickWhileRunning || !wasRunningBeforeClick) {
                performTimerClick?.invoke()
            }
        }
    }

    private var performTimerClick: (() -> Unit)? = null

    override fun setOnClickListener(listener: OnClickListener?) {
        performTimerClick = listener?.let { clickListener -> { clickListener.onClick(this) } }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (autoStart && engine.status == TimerButtonStatus.Idle) {
            start()
        }
    }

    override fun onDetachedFromWindow() {
        removeCallbacks(ticker)
        if (engine.status == TimerButtonStatus.Running || engine.status == TimerButtonStatus.Paused) {
            engine.cancel()
        }
        super.onDetachedFromWindow()
    }

    override fun onDraw(canvas: Canvas) {
        bounds.set(0f, 0f, width.toFloat(), height.toFloat())
        backgroundPaint.color = if (isEnabled) backgroundColor else disabledColor
        canvas.drawRoundRect(bounds, cornerRadius, cornerRadius, backgroundPaint)

        if (engine.progress > 0f) {
            progressPaint.color = progressColor.withAlpha(progressAlpha)
            calculateProgressBounds()
            if (progressMode == TimerProgressMode.Underline) {
                canvas.drawRoundRect(progressBounds, 0f, 0f, progressPaint)
            } else {
                canvas.drawRoundRect(progressBounds, cornerRadius, cornerRadius, progressPaint)
            }
        }

        if (strokeWidth > 0f) {
            strokePaint.color = strokeColor
            strokePaint.strokeWidth = strokeWidth
            val inset = strokeWidth / 2f
            bounds.inset(inset, inset)
            canvas.drawRoundRect(bounds, cornerRadius, cornerRadius, strokePaint)
            bounds.inset(-inset, -inset)
        }
        super.onDraw(canvas)
    }

    /**
     * Sets total timer duration.
     */
    fun setDuration(durationMillis: Long) {
        engine.setDuration(durationMillis)
        syncText()
        invalidate()
    }

    /**
     * Registers a listener for timer lifecycle callbacks.
     */
    fun setTimerListener(listener: TimerButtonListener?) {
        this.listener = listener
    }

    /**
     * Starts the timer from zero.
     */
    fun start() {
        val wasIdle = engine.status == TimerButtonStatus.Idle || engine.status == TimerButtonStatus.Cancelled
        if (!engine.start()) return
        syncText()
        invalidate()
        listener?.onStateChange(TimerButtonStatus.Running)
        if (wasIdle) listener?.onTimerStart()
        postOnAnimation(ticker)
    }

    /**
     * Pauses a running timer.
     */
    fun pause() {
        if (!engine.pause()) return
        removeCallbacks(ticker)
        syncText()
        invalidate()
        listener?.onTimerPause()
        listener?.onStateChange(TimerButtonStatus.Paused)
    }

    /**
     * Resumes a paused timer.
     */
    fun resume() {
        if (!engine.resume()) return
        syncText()
        invalidate()
        listener?.onTimerResume()
        listener?.onStateChange(TimerButtonStatus.Running)
        postOnAnimation(ticker)
    }

    /**
     * Cancels a running or paused timer.
     */
    fun cancel() {
        if (!engine.cancel()) return
        removeCallbacks(ticker)
        syncText()
        invalidate()
        listener?.onTimerCancel()
        listener?.onStateChange(TimerButtonStatus.Cancelled)
    }

    /**
     * Resets the timer to idle.
     */
    fun reset() {
        if (!engine.reset()) return
        removeCallbacks(ticker)
        syncText()
        invalidate()
        listener?.onTimerReset()
        listener?.onStateChange(TimerButtonStatus.Idle)
    }

    /**
     * Restarts the timer from zero.
     */
    fun restart() {
        engine.restart()
        syncText()
        invalidate()
        listener?.onTimerRestart()
        listener?.onTimerStart()
        listener?.onStateChange(TimerButtonStatus.Running)
        postOnAnimation(ticker)
    }

    /**
     * Clears callbacks and listener references.
     */
    fun release() {
        removeCallbacks(ticker)
        listener = null
        performTimerClick = null
    }

    private fun syncText() {
        text = when (engine.status) {
            TimerButtonStatus.Idle -> idleText ?: defaultText
            TimerButtonStatus.Running -> runningText?.formatRemaining(engine.remainingMillis) ?: defaultText
            TimerButtonStatus.Paused -> runningText?.formatRemaining(engine.remainingMillis) ?: defaultText
            TimerButtonStatus.Completed -> completedText ?: defaultText
            TimerButtonStatus.Cancelled -> idleText ?: defaultText
        }
        contentDescription = "$text, ${(engine.progress * 100).roundToInt()} percent complete"
    }

    private fun calculateProgressBounds() {
        val progress = engine.progress.coerceIn(0f, 1f)
        if (progressMode == TimerProgressMode.Underline) {
            val underlineHeight = dp(4f)
            when (progressDirection) {
                TimerProgressDirection.LeftToRight -> progressBounds.set(0f, height - underlineHeight, width * progress, height.toFloat())
                TimerProgressDirection.RightToLeft -> progressBounds.set(width * (1f - progress), height - underlineHeight, width.toFloat(), height.toFloat())
                TimerProgressDirection.TopToBottom -> progressBounds.set(0f, 0f, underlineHeight, height * progress)
                TimerProgressDirection.BottomToTop -> progressBounds.set(0f, height * (1f - progress), underlineHeight, height.toFloat())
            }
            return
        }

        when (progressDirection) {
            TimerProgressDirection.LeftToRight -> progressBounds.set(0f, 0f, width * progress, height.toFloat())
            TimerProgressDirection.RightToLeft -> progressBounds.set(width * (1f - progress), 0f, width.toFloat(), height.toFloat())
            TimerProgressDirection.TopToBottom -> progressBounds.set(0f, 0f, width.toFloat(), height * progress)
            TimerProgressDirection.BottomToTop -> progressBounds.set(0f, height * (1f - progress), width.toFloat(), height.toFloat())
        }
    }

    private fun String.formatRemaining(remainingMillis: Long): String {
        val seconds = ((remainingMillis + 999L) / DateUtils.SECOND_IN_MILLIS).coerceAtLeast(0L)
        return replace("%s", seconds.toString()).replace("%d", seconds.toString())
    }

    private fun Int.withAlpha(alpha: Float): Int {
        val clamped = (alpha.coerceIn(0f, 1f) * 255).roundToInt()
        return Color.argb(clamped, Color.red(this), Color.green(this), Color.blue(this))
    }

    private fun dp(value: Float): Float = value * resources.displayMetrics.density

    private fun directionFromOrdinal(value: Int): TimerProgressDirection = when (value) {
        1 -> TimerProgressDirection.RightToLeft
        2 -> TimerProgressDirection.TopToBottom
        3 -> TimerProgressDirection.BottomToTop
        else -> TimerProgressDirection.LeftToRight
    }

    private fun modeFromOrdinal(value: Int): TimerProgressMode = when (value) {
        1 -> TimerProgressMode.Background
        2 -> TimerProgressMode.Underline
        else -> TimerProgressMode.Overlay
    }
}
