package io.featurama.sdk.ui.theme

import androidx.compose.ui.graphics.Color
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

internal object ThemeFactory {
    fun create(accentColor: Color, isDark: Boolean): FeaturamaTheme {
        val hsl = colorToHsl(accentColor)
        val h = hsl[0]
        val s = hsl[1]

        val accentLight = if (isDark) {
            hslToColor(h, min(s, 30f), 20f)
        } else {
            hslToColor(h, min(s, 40f), 92f)
        }

        val accentForeground = if (relativeLuminance(accentColor) > 0.4f) {
            Color.Black
        } else {
            Color.White
        }

        return if (isDark) {
            FeaturamaTheme(
                background = Color(0xFF000000),
                card = Color(0xFF1C1C1E),
                secondary = Color(0xFF2C2C2E),
                text = Color.White,
                textSecondary = Color(0xFF8E8E93),
                accent = accentColor,
                accentLight = accentLight,
                accentForeground = accentForeground,
                border = Color(0xFF38383A),
                borderAccent = accentColor,
                gray100 = Color(0xFF1C1C1E),
            )
        } else {
            FeaturamaTheme(
                background = Color(0xFFF2F2F7),
                card = Color.White,
                secondary = Color(0xFFF2F2F7),
                text = Color.Black,
                textSecondary = Color(0xFF8E8E93),
                accent = accentColor,
                accentLight = accentLight,
                accentForeground = accentForeground,
                border = Color(0xFFE5E5EA),
                borderAccent = accentColor,
                gray100 = Color(0xFFE5E5EA),
            )
        }
    }

    private fun colorToHsl(color: Color): FloatArray {
        val r = color.red
        val g = color.green
        val b = color.blue
        val cMax = max(r, max(g, b))
        val cMin = min(r, min(g, b))
        var h = 0f
        var s = 0f
        val l = (cMax + cMin) / 2f

        if (cMax != cMin) {
            val d = cMax - cMin
            s = if (l > 0.5f) d / (2f - cMax - cMin) else d / (cMax + cMin)
            h = when (cMax) {
                r -> ((g - b) / d + (if (g < b) 6f else 0f)) / 6f
                g -> ((b - r) / d + 2f) / 6f
                else -> ((r - g) / d + 4f) / 6f
            }
        }

        return floatArrayOf(h * 360f, s * 100f, l * 100f)
    }

    private fun hslToColor(h: Float, s: Float, l: Float): Color {
        val sn = s / 100f
        val ln = l / 100f
        val a = sn * min(ln, 1f - ln)
        fun f(n: Float): Float {
            val k = (n + h / 30f) % 12f
            return ln - a * max(min(k - 3f, min(9f - k, 1f)), -1f)
        }
        return Color(
            red = f(0f),
            green = f(8f),
            blue = f(4f),
        )
    }

    private fun relativeLuminance(color: Color): Float {
        fun channel(v: Float): Float =
            if (v <= 0.03928f) v / 12.92f else ((v + 0.055f) / 1.055f).pow(2.4f)
        return 0.2126f * channel(color.red) + 0.7152f * channel(color.green) + 0.0722f * channel(color.blue)
    }
}
