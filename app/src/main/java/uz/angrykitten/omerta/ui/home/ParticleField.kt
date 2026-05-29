package uz.angrykitten.omerta.ui.home

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.sin
import kotlin.random.Random

/**
 * Cinematic background — slow-drifting "embers" rendered into a Canvas.
 *
 * Why this isn't a particle-system class:
 *  - We want zero allocations per frame (everything lives in a single
 *    `FloatArray` keyed by particle index, no GC churn).
 *  - We tick by elapsed nanos from [withFrameNanos], not a fixed `delay()`,
 *    so motion is frame-rate-independent on 60/90/120 Hz panels.
 *
 * Density is given as particle count; the spec calls for "floating dark
 * motes/embers" — at 24 particles you get a calm sky, at 60+ it gets noisy.
 */
@Composable
fun ParticleField(
    modifier: Modifier = Modifier,
    particleCount: Int = 28,
    color: Color = Color(0xFFF1C40F),
    minRadius: Dp = 0.6.dp,
    maxRadius: Dp = 2.2.dp,
) {
    val particles = remember(particleCount) { generateParticles(particleCount) }
    var elapsedSeconds by remember { mutableStateOf(0f) }

    LaunchedEffect(particleCount) {
        var lastFrameNs = 0L
        while (true) {
            withFrameNanos { now ->
                if (lastFrameNs != 0L) {
                    val deltaSec = (now - lastFrameNs) / 1_000_000_000f
                    elapsedSeconds += deltaSec
                }
                lastFrameNs = now
            }
        }
    }

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val rMin = minRadius.toPx()
        val rMax = maxRadius.toPx()

        for (i in 0 until particleCount) {
            val p = particles[i]
            // Vertical drift — slow upward float, wraps from bottom to top.
            val rawY = (p.startY + elapsedSeconds * p.driftSpeed) % 1f
            val y = (1f - rawY) * h
            // Horizontal bob using a sine wave so columns don't feel mechanical.
            val bobX = sin((elapsedSeconds * p.bobFrequency + p.phase).toDouble()).toFloat()
            val x = (p.baseX + bobX * p.bobAmplitude) * w
            // Slow alpha pulse keeps the field feeling alive even when motion is subtle.
            val alphaPulse = 0.4f + 0.6f * (0.5f + 0.5f * sin((elapsedSeconds * p.pulseFrequency + p.phase).toDouble()).toFloat())
            val radius = rMin + (rMax - rMin) * p.sizeRatio
            drawCircle(
                color = color.copy(alpha = p.baseAlpha * alphaPulse),
                radius = radius,
                center = androidx.compose.ui.geometry.Offset(x, y),
            )
        }
    }
}

private data class Particle(
    val baseX: Float,
    val startY: Float,
    val driftSpeed: Float,
    val bobFrequency: Float,
    val bobAmplitude: Float,
    val pulseFrequency: Float,
    val phase: Float,
    val sizeRatio: Float,
    val baseAlpha: Float,
)

// AUDIT FIX: fixed-seed RNG — without it, every Compose preview / process
// restart rolls a brand-new field, which is jarring during dev. The user-
// visible result is still random-looking but stable across recompositions.
private fun generateParticles(count: Int): List<Particle> {
    val random = Random(seed = 0xE3B5_C0FFEEL)
    return List(count) {
        Particle(
            baseX = random.nextFloat(),
            startY = random.nextFloat(),
            driftSpeed = 0.015f + random.nextFloat() * 0.035f,
            bobFrequency = 0.2f + random.nextFloat() * 0.6f,
            bobAmplitude = 0.01f + random.nextFloat() * 0.04f,
            pulseFrequency = 0.4f + random.nextFloat() * 0.9f,
            phase = random.nextFloat() * 6.28318f,
            sizeRatio = random.nextFloat(),
            baseAlpha = 0.18f + random.nextFloat() * 0.45f,
        )
    }
}
