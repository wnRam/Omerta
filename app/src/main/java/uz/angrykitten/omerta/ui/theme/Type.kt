package uz.angrykitten.omerta.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.sp
import uz.angrykitten.omerta.R

/**
 * Downloadable Google Fonts. Provider auth strings come from res/values/font_certs.xml,
 * which Studio generates when adding a downloadable font — we declare it manually
 * since we want full control over the FontFamily definitions in code.
 */
private val googleFontsProvider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs,
)

private val cinzelFont = GoogleFont("Cinzel")
private val interFont = GoogleFont("Inter")
private val jetBrainsMonoFont = GoogleFont("JetBrains Mono")

val CinzelFamily = FontFamily(
    Font(googleFont = cinzelFont, fontProvider = googleFontsProvider, weight = FontWeight.Normal),
    Font(googleFont = cinzelFont, fontProvider = googleFontsProvider, weight = FontWeight.Medium),
    Font(googleFont = cinzelFont, fontProvider = googleFontsProvider, weight = FontWeight.SemiBold),
    Font(googleFont = cinzelFont, fontProvider = googleFontsProvider, weight = FontWeight.Bold),
    Font(googleFont = cinzelFont, fontProvider = googleFontsProvider, weight = FontWeight.Black),
)

val InterFamily = FontFamily(
    Font(googleFont = interFont, fontProvider = googleFontsProvider, weight = FontWeight.Light),
    Font(googleFont = interFont, fontProvider = googleFontsProvider, weight = FontWeight.Normal),
    Font(googleFont = interFont, fontProvider = googleFontsProvider, weight = FontWeight.Medium),
    Font(googleFont = interFont, fontProvider = googleFontsProvider, weight = FontWeight.SemiBold),
    Font(googleFont = interFont, fontProvider = googleFontsProvider, weight = FontWeight.Bold),
    Font(
        googleFont = interFont,
        fontProvider = googleFontsProvider,
        weight = FontWeight.Normal,
        style = FontStyle.Italic,
    ),
)

val MonoFamily = FontFamily(
    Font(googleFont = jetBrainsMonoFont, fontProvider = googleFontsProvider, weight = FontWeight.Normal),
    Font(googleFont = jetBrainsMonoFont, fontProvider = googleFontsProvider, weight = FontWeight.Medium),
    Font(googleFont = jetBrainsMonoFont, fontProvider = googleFontsProvider, weight = FontWeight.Bold),
)

// AUDIT FIX: Material3's default Typography uses Roboto. Override every slot
// so Cinzel applies to display/headline/title, Inter to body/label, leaving
// monospace specifically for room codes (used inline, not via the type scale).
val OmertaTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = CinzelFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = (-0.25).sp,
    ),
    displayMedium = TextStyle(
        fontFamily = CinzelFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 45.sp,
        lineHeight = 52.sp,
    ),
    displaySmall = TextStyle(
        fontFamily = CinzelFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 36.sp,
        lineHeight = 44.sp,
    ),
    headlineLarge = TextStyle(
        fontFamily = CinzelFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = CinzelFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
    ),
    headlineSmall = TextStyle(
        fontFamily = CinzelFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 24.sp,
        lineHeight = 32.sp,
    ),
    titleLarge = TextStyle(
        fontFamily = CinzelFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.5.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = CinzelFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.4.sp,
    ),
    titleSmall = TextStyle(
        fontFamily = InterFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = InterFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = InterFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = InterFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = InterFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = InterFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = InterFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp,
    ),
)

/** Room codes etc. — use inline via `Text(style = ... .copy(fontFamily = MonoFamily))`. */
val MonoCodeStyle: TextStyle = TextStyle(
    fontFamily = MonoFamily,
    fontWeight = FontWeight.Bold,
    fontSize = 32.sp,
    letterSpacing = 6.sp,
)
