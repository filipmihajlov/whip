package com.whip

import kotlin.math.hypot

data class Point(var x: Double, var y: Double, var ox: Double, var oy: Double)

class VerletRope(private val numPoints: Int) {
    val points = Array(numPoints) { Point(0.0, 0.0, 0.0, 0.0) }
    var handlePos = Pair(0.0, 0.0)
    var tipPos = Pair(0.0, 0.0)
    private var tipPinned = true
    private var segLen = 18.0

    fun resetRope(x: Double, y: Double) {
        for (p in points) {
            p.x = x
            p.y = y
            p.ox = x
            p.oy = y
        }
        handlePos = Pair(x, y)
        tipPos = Pair(x, y)
        tipPinned = true
    }

    fun update() {
        // Verlet integration
        for (p in points) {
            val vx = (p.x - p.ox) * 0.985
            val vy = (p.y - p.oy) * 0.985
            p.ox = p.x
            p.oy = p.y
            p.x += vx
            p.y += vy + 0.45 // gravity
        }

        // Distance constraints
        for (iteration in 0 until 18) {
            points[0].x = handlePos.first
            points[0].y = handlePos.second
            
            if (tipPinned) {
                points[numPoints - 1].x = tipPos.first
                points[numPoints - 1].y = tipPos.second
            }

            for (i in 0 until numPoints - 1) {
                val a = points[i]
                val b = points[i + 1]
                val dx = b.x - a.x
                val dy = b.y - a.y
                val d = hypot(dx, dy)
                val diff = (d - segLen) / d

                val aFixed = (i == 0)
                val bFixed = (i + 1 == numPoints - 1) && tipPinned

                when {
                    aFixed -> {
                        b.x -= dx * diff
                        b.y -= dy * diff
                    }
                    bFixed -> {
                        a.x += dx * diff
                        a.y += dy * diff
                    }
                    else -> {
                        a.x += dx * diff * 0.5
                        a.y += dy * diff * 0.5
                        b.x -= dx * diff * 0.5
                        b.y -= dy * diff * 0.5
                    }
                }
            }
        }
    }
}

