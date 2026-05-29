package uz.angrykitten.omerta.ui.common

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel

/**
 * Compose-friendly QR code renderer. Encodes [content] via ZXing into a bit
 * matrix, then draws the matrix directly onto a [Canvas] — avoids allocating
 * a Bitmap or going through the View bridge.
 *
 * Caches the encoded matrix in a [remember] keyed on (content, sizeDp): the
 * matrix is independent of theme, so swapping dark/light doesn't re-encode.
 */
@Composable
fun QrCodeView(
    content: String,
    modifier: Modifier = Modifier,
    size: Dp,
    foreground: Color = Color.Black,
    background: Color = Color.White,
) {
    val matrix = remember(content) {
        runCatching {
            val hints = mapOf(
                EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.M,
                EncodeHintType.MARGIN to 1,
            )
            QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, 0, 0, hints)
        }.getOrNull()
    }
    Canvas(
        modifier = modifier
            .size(size)
            .background(background),
    ) {
        val m = matrix ?: return@Canvas
        val cellSize = this.size.width / m.width
        for (y in 0 until m.height) {
            for (x in 0 until m.width) {
                if (m.get(x, y)) {
                    drawRect(
                        color = foreground,
                        topLeft = Offset(x * cellSize, y * cellSize),
                        size = Size(cellSize, cellSize),
                    )
                }
            }
        }
    }
}
