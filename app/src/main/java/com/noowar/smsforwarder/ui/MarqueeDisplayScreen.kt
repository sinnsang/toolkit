package com.noowar.smsforwarder.ui

import android.app.Activity
import android.graphics.Paint
import android.graphics.Typeface
import android.speech.tts.TextToSpeech
import android.view.WindowManager
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.noowar.smsforwarder.data.MarqueeItem
import kotlinx.coroutines.delay
import java.util.Locale

@Composable
fun MarqueeDisplayScreen(item: MarqueeItem, onBack: () -> Unit) {
    val context = LocalContext.current
    val activity = context as? Activity
    val density = LocalDensity.current

    DisposableEffect(Unit) {
        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        val controller = activity?.let {
            WindowCompat.getInsetsController(it.window, it.window.decorView)
        }
        controller?.hide(WindowInsetsCompat.Type.systemBars())
        controller?.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        onDispose {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            controller?.show(WindowInsetsCompat.Type.systemBars())
        }
    }

    var ttsEngine by remember { mutableStateOf<TextToSpeech?>(null) }
    DisposableEffect(Unit) {
        var engine: TextToSpeech? = null
        engine = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                engine?.language = Locale.getDefault()
                ttsEngine = engine
            }
        }
        onDispose {
            ttsEngine?.stop()
            ttsEngine?.shutdown()
            ttsEngine = null
        }
    }

    val fontSizePx = with(density) { item.fontSize.sp.toPx() }
    val speedPx = with(density) { (item.speed * 2f).dp.toPx() }

    val textPaint = remember(item.fontType, fontSizePx) {
        Paint().apply {
            typeface = when (item.fontType) {
                "SERIF" -> Typeface.SERIF
                "MONO"  -> Typeface.MONOSPACE
                else    -> Typeface.DEFAULT
            }
            textSize = fontSizePx
            isAntiAlias = true
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(marqueeBgColor(item.bgColorKey))
            .pointerInput(Unit) { detectTapGestures { onBack() } }
    ) {
        val screenW = constraints.maxWidth.toFloat()
        val screenH = constraints.maxHeight.toFloat()
        val textWidth = remember(item.text, textPaint) { textPaint.measureText(item.text) }

        var offset by remember(item, screenW, screenH) {
            mutableStateOf(
                when (item.direction) {
                    "LEFT"   -> screenW
                    "RIGHT"  -> -textWidth
                    "UP"     -> screenH + fontSizePx
                    "DOWN"   -> -fontSizePx
                    else     -> screenW   // BOUNCE starts at left
                }
            )
        }
        // BOUNCE init: start at left edge
        var offset2 by remember(item) { mutableStateOf(0f) }
        var bounceDir by remember(item) { mutableStateOf(1f) }

        LaunchedEffect(item, screenW, screenH) {
            while (true) {
                delay(16L)
                when (item.direction) {
                    "LEFT" -> {
                        offset -= speedPx
                        if (offset + textWidth < 0f) {
                            offset = screenW
                            if (item.ttsEnabled) ttsEngine?.speak(item.text, TextToSpeech.QUEUE_FLUSH, null, null)
                        }
                    }
                    "RIGHT" -> {
                        offset += speedPx
                        if (offset > screenW) {
                            offset = -textWidth
                            if (item.ttsEnabled) ttsEngine?.speak(item.text, TextToSpeech.QUEUE_FLUSH, null, null)
                        }
                    }
                    "UP" -> {
                        offset -= speedPx
                        if (offset + fontSizePx < 0f) {
                            offset = screenH + fontSizePx
                            if (item.ttsEnabled) ttsEngine?.speak(item.text, TextToSpeech.QUEUE_FLUSH, null, null)
                        }
                    }
                    "DOWN" -> {
                        offset += speedPx
                        if (offset > screenH) {
                            offset = -fontSizePx
                            if (item.ttsEnabled) ttsEngine?.speak(item.text, TextToSpeech.QUEUE_FLUSH, null, null)
                        }
                    }
                    "BOUNCE" -> {
                        if (textWidth >= screenW) {
                            // too wide: fall back to leftward scroll
                            offset2 -= speedPx
                            if (offset2 + textWidth < 0f) {
                                offset2 = screenW
                                if (item.ttsEnabled) ttsEngine?.speak(item.text, TextToSpeech.QUEUE_FLUSH, null, null)
                            }
                        } else {
                            offset2 += speedPx * bounceDir
                            when {
                                offset2 + textWidth >= screenW -> {
                                    offset2 = screenW - textWidth
                                    bounceDir = -1f
                                    if (item.ttsEnabled) ttsEngine?.speak(item.text, TextToSpeech.QUEUE_FLUSH, null, null)
                                }
                                offset2 <= 0f -> {
                                    offset2 = 0f
                                    bounceDir = 1f
                                }
                            }
                        }
                    }
                }
            }
        }

        val isRainbow = item.textColorKey == "RAINBOW"
        val solidTextColor = if (!isRainbow) marqueeTextColor(item.textColorKey) else null
        val strokeCol = marqueeStrokeColor(item.strokeColorKey)

        Canvas(modifier = Modifier.fillMaxSize()) {
            val isVertical = item.direction == "UP" || item.direction == "DOWN"
            val isBounce = item.direction == "BOUNCE"

            val drawX = when {
                isVertical -> (size.width - textWidth) / 2f
                isBounce   -> offset2
                else       -> offset
            }
            val drawY = when {
                isVertical -> offset
                else       -> size.height / 2f + fontSizePx / 3f
            }

            drawIntoCanvas { canvas ->
                if (isRainbow) {
                    var charX = drawX
                    item.text.forEachIndexed { i, ch ->
                        val charStr = ch.toString()
                        val rainbowCol = MARQUEE_RAINBOW[i % MARQUEE_RAINBOW.size]
                        strokeCol?.let { sc ->
                            textPaint.style = Paint.Style.STROKE
                            textPaint.strokeWidth = fontSizePx * 0.08f
                            textPaint.color = sc.toArgb()
                            canvas.nativeCanvas.drawText(charStr, charX, drawY, textPaint)
                        }
                        textPaint.style = Paint.Style.FILL
                        textPaint.color = rainbowCol.toArgb()
                        canvas.nativeCanvas.drawText(charStr, charX, drawY, textPaint)
                        charX += textPaint.measureText(charStr)
                    }
                } else {
                    strokeCol?.let { sc ->
                        textPaint.style = Paint.Style.STROKE
                        textPaint.strokeWidth = fontSizePx * 0.08f
                        textPaint.color = sc.toArgb()
                        canvas.nativeCanvas.drawText(item.text, drawX, drawY, textPaint)
                    }
                    textPaint.style = Paint.Style.FILL
                    textPaint.color = solidTextColor!!.toArgb()
                    canvas.nativeCanvas.drawText(item.text, drawX, drawY, textPaint)
                }
            }
        }
    }
}
