package com.whip

import java.awt.Point
import kotlin.math.hypot
import kotlin.math.roundToInt

private const val DEFAULT_LAYOUT_MARGIN = 28.0

data class WhipLayoutRatios(
    val originXRatio: Double? = null,
    val originYRatio: Double? = null,
    val targetXRatio: Double? = null,
    val targetYRatio: Double? = null,
) {
    fun resolve(width: Int, height: Int, margin: Double = DEFAULT_LAYOUT_MARGIN): WhipLayout {
        val safeWidth = width.coerceAtLeast(1).toDouble()
        val safeHeight = height.coerceAtLeast(1).toDouble()
        val defaultLayout = defaultLayout(safeWidth, safeHeight, margin)
        return WhipLayout(
            originX = resolveX(originXRatio, defaultLayout.originX, safeWidth, margin),
            originY = resolveY(originYRatio, defaultLayout.originY, safeHeight, margin),
            targetX = resolveX(targetXRatio, defaultLayout.targetX, safeWidth, margin),
            targetY = resolveY(targetYRatio, defaultLayout.targetY, safeHeight, margin),
        )
    }

    companion object {
        fun fromLayout(layout: WhipLayout, width: Int, height: Int): WhipLayoutRatios {
            val safeWidth = width.coerceAtLeast(1).toDouble()
            val safeHeight = height.coerceAtLeast(1).toDouble()
            return WhipLayoutRatios(
                originXRatio = (layout.originX / safeWidth).coerceIn(0.0, 1.0),
                originYRatio = (layout.originY / safeHeight).coerceIn(0.0, 1.0),
                targetXRatio = (layout.targetX / safeWidth).coerceIn(0.0, 1.0),
                targetYRatio = (layout.targetY / safeHeight).coerceIn(0.0, 1.0),
            )
        }

        private fun resolveX(ratio: Double?, defaultValue: Double, width: Double, margin: Double): Double {
            val fallback = defaultValue / width
            return (width * (ratio ?: fallback)).coerceIn(margin, width - margin)
        }

        private fun resolveY(ratio: Double?, defaultValue: Double, height: Double, margin: Double): Double {
            val fallback = defaultValue / height
            return (height * (ratio ?: fallback)).coerceIn(margin, height - margin)
        }

        private fun defaultLayout(width: Double, height: Double, margin: Double): WhipLayout = WhipLayout(
            originX = width * 0.5,
            originY = height - margin,
            targetX = width - margin,
            targetY = margin,
        )
    }
}

data class WhipLayout(
    val originX: Double,
    val originY: Double,
    val targetX: Double,
    val targetY: Double,
) {
    val originPoint: Point
        get() = Point(originX.roundToInt(), originY.roundToInt())

    val targetPoint: Point
        get() = Point(targetX.roundToInt(), targetY.roundToInt())

    fun span(): Double = hypot(targetX - originX, targetY - originY)
}

