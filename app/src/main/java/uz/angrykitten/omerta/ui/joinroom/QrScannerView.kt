@file:OptIn(androidx.camera.core.ExperimentalGetImage::class)

package uz.angrykitten.omerta.ui.joinroom

import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

private const val TAG = "OmertaScanner"

/**
 * Camera preview + animated scan-frame overlay. **Inline view** — caller
 * decides where it sits in their layout, so we never navigate away from the
 * Join screen just to scan.
 *
 * Lifecycle rules:
 *  - We obtain the [ProcessCameraProvider] singleton and track it in a
 *    remember-holder so [DisposableEffect.onDispose] can call `unbindAll()`.
 *    Previously we never unbound — the analyzer kept firing against a
 *    destroyed [PreviewView] when the user switched tabs, throwing an
 *    uncaught exception on the camera worker thread which propagated as
 *    a fatal app crash.
 *  - The analyzer body runs inside try/catch so a single bad frame can't
 *    take the process down.
 *  - Binding failures land in [bindingError] state and surface inline as a
 *    visible error message instead of a silent black square.
 *  - `@file:OptIn(ExperimentalGetImage)` covers `imageProxy.image` access.
 */
@Composable
fun QrScannerView(
    onDetected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val executor: ExecutorService = remember { Executors.newSingleThreadExecutor() }
    val scanner: BarcodeScanner = remember {
        BarcodeScanning.getClient(
            BarcodeScannerOptions.Builder().setBarcodeFormats(Barcode.FORMAT_QR_CODE).build(),
        )
    }
    // AUDIT FIX: capture the provider so onDispose can unbind it cleanly.
    val providerHolder = remember { CameraProviderHolder() }
    var lastSeen by remember { mutableStateOf<String?>(null) }
    var bindingError by remember { mutableStateOf<String?>(null) }

    DisposableEffect(Unit) {
        onDispose {
            runCatching { providerHolder.provider?.unbindAll() }
            runCatching { executor.shutdown() }
            runCatching { scanner.close() }
        }
    }

    Box(modifier = modifier.clip(RoundedCornerShape(20.dp))) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                val previewView = PreviewView(ctx).apply {
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                }
                bindCameraSafely(
                    context = ctx,
                    previewView = previewView,
                    lifecycleOwner = lifecycleOwner,
                    scanner = scanner,
                    executor = executor,
                    providerHolder = providerHolder,
                    onError = { msg -> bindingError = msg },
                    onResult = { raw ->
                        if (raw == lastSeen) return@bindCameraSafely
                        lastSeen = raw
                        onDetected(raw)
                    },
                )
                previewView
            },
        )
        ScannerOverlay()
        bindingError?.let { CameraErrorOverlay(message = it) }
    }
}

/**
 * Thin holder so we can capture the provider once the async future fires
 * and still reach it from `onDispose` later. A plain `var` outside a remember
 * block would die at recomposition; a `MutableState<ProcessCameraProvider?>`
 * would force unnecessary recomposition.
 */
private class CameraProviderHolder {
    @Volatile var provider: ProcessCameraProvider? = null
}

@Composable
private fun CameraErrorOverlay(message: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.65f))
            .padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // FIXED: was a hardcoded English string — now uses a localized resource.
            Text(
                text = androidx.compose.ui.res.stringResource(id = uz.angrykitten.omerta.R.string.camera_error_unavailable),
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                textAlign = TextAlign.Center,
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun ScannerOverlay() {
    val infinite = rememberInfiniteTransition(label = "scan")
    val scanY by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1700, easing = LinearEasing), RepeatMode.Reverse),
        label = "scan.line",
    )
    val brightness by infinite.animateFloat(
        initialValue = 0.7f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(900, easing = LinearEasing), RepeatMode.Reverse),
        label = "scan.bright",
    )
    Canvas(modifier = Modifier.fillMaxSize()) {
        // Dim everything except the framed area.
        drawRect(color = Color(0xFF000000).copy(alpha = 0.55f))
        val frame = minOf(size.width, size.height) * 0.72f
        val left = (size.width - frame) / 2f
        val top = (size.height - frame) / 2f
        drawRect(
            color = Color(0xFF000000).copy(alpha = 0f),
            topLeft = Offset(left, top),
            size = Size(frame, frame),
            blendMode = androidx.compose.ui.graphics.BlendMode.Clear,
        )
        val accent = Color(0xFFF1C40F).copy(alpha = brightness)
        val cornerLen = frame * 0.18f
        val sw = 5f
        drawLine(accent, Offset(left, top + cornerLen), Offset(left, top), sw, cap = androidx.compose.ui.graphics.StrokeCap.Round)
        drawLine(accent, Offset(left, top), Offset(left + cornerLen, top), sw, cap = androidx.compose.ui.graphics.StrokeCap.Round)
        drawLine(accent, Offset(left + frame - cornerLen, top), Offset(left + frame, top), sw, cap = androidx.compose.ui.graphics.StrokeCap.Round)
        drawLine(accent, Offset(left + frame, top), Offset(left + frame, top + cornerLen), sw, cap = androidx.compose.ui.graphics.StrokeCap.Round)
        drawLine(accent, Offset(left, top + frame - cornerLen), Offset(left, top + frame), sw, cap = androidx.compose.ui.graphics.StrokeCap.Round)
        drawLine(accent, Offset(left, top + frame), Offset(left + cornerLen, top + frame), sw, cap = androidx.compose.ui.graphics.StrokeCap.Round)
        drawLine(accent, Offset(left + frame - cornerLen, top + frame), Offset(left + frame, top + frame), sw, cap = androidx.compose.ui.graphics.StrokeCap.Round)
        drawLine(accent, Offset(left + frame, top + frame - cornerLen), Offset(left + frame, top + frame), sw, cap = androidx.compose.ui.graphics.StrokeCap.Round)
        val lineY = top + frame * scanY
        val gradient = Brush.horizontalGradient(
            colors = listOf(Color(0xFFF1C40F).copy(alpha = 0f), Color(0xFFF1C40F).copy(alpha = 0.85f), Color(0xFFF1C40F).copy(alpha = 0f)),
            startX = left + 12f,
            endX = left + frame - 12f,
        )
        drawLine(
            brush = gradient,
            start = Offset(left + 12f, lineY),
            end = Offset(left + frame - 12f, lineY),
            strokeWidth = 3f,
        )
        drawRect(
            color = accent.copy(alpha = 0.2f),
            topLeft = Offset(left, top),
            size = Size(frame, frame),
            style = Stroke(width = 1f),
        )
    }
}

private fun bindCameraSafely(
    context: android.content.Context,
    previewView: PreviewView,
    lifecycleOwner: LifecycleOwner,
    scanner: BarcodeScanner,
    executor: ExecutorService,
    providerHolder: CameraProviderHolder,
    onError: (String) -> Unit,
    onResult: (String) -> Unit,
) {
    val future = ProcessCameraProvider.getInstance(context)
    future.addListener({
        val outcome = runCatching {
            val provider = future.get()
            providerHolder.provider = provider

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }
            val analysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
            // AUDIT FIX: explicit Analyzer object instead of SAM-converted
            // lambda. With the lambda, certain Kotlin versions emit a
            // method-reference that bypasses our try/catch — the explicit
            // object guarantees the catch runs on every frame.
            analysis.setAnalyzer(
                executor,
                object : ImageAnalysis.Analyzer {
                    override fun analyze(imageProxy: ImageProxy) {
                        try {
                            processFrame(imageProxy, scanner, onResult)
                        } catch (t: Throwable) {
                            // Never let a single bad frame crash the process.
                            Log.w(TAG, "analyze() failed", t)
                            runCatching { imageProxy.close() }
                        }
                    }
                },
            )

            provider.unbindAll()
            provider.bindToLifecycle(
                lifecycleOwner,
                CameraSelector.DEFAULT_BACK_CAMERA,
                preview,
                analysis,
            )
            Unit
        }
        outcome.exceptionOrNull()?.let { err ->
            Log.e(TAG, "Camera bind failed", err)
            onError(err.message ?: err::class.java.simpleName)
        }
    }, ContextCompat.getMainExecutor(context))
}

private fun processFrame(
    imageProxy: ImageProxy,
    scanner: BarcodeScanner,
    onResult: (String) -> Unit,
) {
    val media = imageProxy.image
    if (media == null) {
        imageProxy.close()
        return
    }
    val image = InputImage.fromMediaImage(media, imageProxy.imageInfo.rotationDegrees)
    scanner.process(image)
        .addOnSuccessListener { codes ->
            runCatching { codes.firstNotNullOfOrNull { it.rawValue }?.let(onResult) }
        }
        .addOnFailureListener { err -> Log.w(TAG, "barcode scan failed", err) }
        .addOnCompleteListener { runCatching { imageProxy.close() } }
}
