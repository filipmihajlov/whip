package com.whip

import java.awt.Color
import kotlin.math.roundToLong

data class WhipTimings(
    val windMs: Long,
    val snapMs: Long,
    val holdMs: Long,
    val recoilMs: Long,
)

data class WhipRuntimeOptions(
    val playSound: Boolean,
    val colorStyle: WhipColorStyle,
    val speedStyle: WhipSpeedStyle,
)

enum class WhipColorStyle(
    val id: String,
    private val displayName: String,
    val shadowColor: Color,
    val startColor: Color,
    val endColor: Color,
) {
    CLASSIC(
        id = "classic",
        displayName = "Classic leather",
        shadowColor = Color(0, 0, 0, 128),
        startColor = Color(80, 50, 30),
        endColor = Color(50, 25, 10),
    ),
    CRIMSON(
        id = "crimson",
        displayName = "Crimson lash",
        shadowColor = Color(45, 6, 10, 128),
        startColor = Color(154, 62, 56),
        endColor = Color(92, 24, 24),
    ),
    EMERALD(
        id = "emerald",
        displayName = "Emerald viper",
        shadowColor = Color(6, 28, 18, 128),
        startColor = Color(72, 132, 88),
        endColor = Color(20, 78, 46),
    ),
    SAPPHIRE(
        id = "sapphire",
        displayName = "Sapphire arc",
        shadowColor = Color(8, 16, 40, 128),
        startColor = Color(78, 116, 190),
        endColor = Color(26, 52, 120),
    ),
    VIOLET(
        id = "violet",
        displayName = "Violet storm",
        shadowColor = Color(26, 10, 34, 128),
        startColor = Color(136, 92, 184),
        endColor = Color(78, 42, 128),
    );

    override fun toString(): String = displayName

    fun colorAt(progress: Double): Color {
        val t = progress.coerceIn(0.0, 1.0)
        return Color(
            lerp(startColor.red, endColor.red, t),
            lerp(startColor.green, endColor.green, t),
            lerp(startColor.blue, endColor.blue, t),
        )
    }

    companion object {
        fun fromId(id: String?): WhipColorStyle = entries.firstOrNull { it.id == id } ?: CLASSIC

        private fun lerp(start: Int, end: Int, t: Double): Int {
            return (start + (end - start) * t).roundToLong().toInt().coerceIn(0, 255)
        }
    }
}

enum class WhipSpeedStyle(
    val id: String,
    private val displayName: String,
    private val durationScale: Double,
) {
    SLOW("slow", "Slow", 1.25),
    NORMAL("normal", "Normal", 1.0),
    FAST("fast", "Fast", 0.8);

    override fun toString(): String = displayName

    fun timings(): WhipTimings = WhipTimings(
        windMs = scaled(169L),
        snapMs = scaled(195L),
        holdMs = scaled(39L),
        recoilMs = scaled(494L),
    )

    private fun scaled(baseMs: Long): Long = (baseMs * durationScale).roundToLong().coerceAtLeast(1L)

    companion object {
        fun fromId(id: String?): WhipSpeedStyle = entries.firstOrNull { it.id == id } ?: NORMAL
    }
}

fun WhipSettingsState.Settings.toRuntimeOptions(): WhipRuntimeOptions = WhipRuntimeOptions(
    playSound = soundEnabled,
    colorStyle = WhipColorStyle.fromId(colorStyleId),
    speedStyle = WhipSpeedStyle.fromId(speedStyleId),
)
