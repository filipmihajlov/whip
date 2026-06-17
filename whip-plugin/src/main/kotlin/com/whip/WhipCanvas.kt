package com.whip

import java.awt.BasicStroke
import java.awt.Color
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.event.ActionEvent
import javax.swing.JPanel
import javax.swing.Timer
import kotlin.math.hypot
import kotlin.math.pow

class WhipCanvas : JPanel() {
    private val verletRope = VerletRope(24)
    private var isActive = false
    private var soundPlayer = WhipSound()
    private var animationTimer: Timer? = null
    
    private val easeOut: (Double) -> Double = { t -> 1 - (1 - t).pow(3) }
    private val easeIn: (Double) -> Double = { t -> t * t * t }
    private val easeInOut: (Double) -> Double = { t ->
        if (t < 0.5) 4 * t * t * t else 1 - (-2 * t + 2).pow(3) / 2
    }

    init {
        isOpaque = false
        setSize(1920, 1080)
    }

    fun crack() {
        if (isActive) return
        
        isActive = true
        soundPlayer.playCrack(1.0)
        
        val origin = java.awt.Point(width * 4 / 5, height * 9 / 10)
        val target = java.awt.Point(width / 2, height / 3)
        
        verletRope.resetRope(origin.x.toDouble(), origin.y.toDouble())
        
        val dx = target.x - origin.x
        val dy = target.y - origin.y
        val dist = hypot(dx.toDouble(), dy.toDouble())
        
        val dirX = dx / dist
        val dirY = dy / dist
        val windBack = Pair(
            origin.x - dirX * 108,
            origin.y - dirY * 108 - 96
        )
        
        val T_WIND = 169L
        val T_SNAP = 195L
        val T_HOLD = 39L
        val T_RECOIL = 494L
        val startTime = System.currentTimeMillis()
        
        animationTimer = Timer(16) { event: ActionEvent ->
            val elapsed = System.currentTimeMillis() - startTime
            
            when {
                elapsed < T_WIND -> {
                    val k = easeOut(elapsed.toDouble() / T_WIND)
                    verletRope.tipPos = Pair(
                        origin.x + (windBack.first - origin.x) * k,
                        origin.y + (windBack.second - origin.y) * k
                    )
                    verletRope.handlePos = Pair(
                        origin.x - dirX * 10 * k,
                        origin.y - dirY * 10 * k
                    )
                }
                elapsed < T_WIND + T_SNAP -> {
                    val k = easeIn((elapsed - T_WIND).toDouble() / T_SNAP)
                    verletRope.tipPos = Pair(
                        windBack.first + (target.x - windBack.first) * k,
                        windBack.second + (target.y - windBack.second) * k
                    )
                    val hk = easeOut((elapsed - T_WIND).toDouble() / T_SNAP)
                    verletRope.handlePos = Pair(
                        origin.x + dirX * 35 * hk,
                        origin.y + dirY * 35 * hk
                    )
                }
                elapsed < T_WIND + T_SNAP + T_HOLD -> {
                    verletRope.tipPos = Pair(target.x.toDouble(), target.y.toDouble())
                }
                elapsed < T_WIND + T_SNAP + T_HOLD + T_RECOIL -> {
                    val k = easeInOut((elapsed - T_WIND - T_SNAP - T_HOLD).toDouble() / T_RECOIL)
                    verletRope.handlePos = Pair(
                        origin.x + dirX * 35 * (1 - k),
                        origin.y + dirY * 35 * (1 - k)
                    )
                }
                else -> {
                    isActive = false
                    (event.source as Timer).stop()
                    return@Timer
                }
            }
            
            verletRope.update()
            repaint()
        }
        
        animationTimer?.start()
    }

    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)
        if (!isActive) return

        val g2d = g as Graphics2D
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)

        drawRope(g2d)
    }

    private fun drawRope(g: Graphics2D) {
        val points = verletRope.points
        
        g.stroke = BasicStroke(7f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)
        
        for (i in 0 until points.size - 1) {
            val a = points[i]
            val b = points[i + 1]
            val t = i.toDouble() / (points.size - 1)
            val w = (7 * (1 - t) + 0.8 * t).toFloat()
            
            // Shadow
            g.color = Color(0, 0, 0, 128)
            g.stroke = BasicStroke(w + 1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)
            g.drawLine((a.x + 1.5).toInt(), (a.y + 2.5).toInt(),
                       (b.x + 1.5).toInt(), (b.y + 2.5).toInt())
            
            // Body
            val r = (80 - 30 * t).toInt()
            val gb = (50 - 25 * t).toInt()
            val bch = (30 - 20 * t).toInt()
            g.color = Color(r, gb, bch)
            g.stroke = BasicStroke(w, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)
            g.drawLine(a.x.toInt(), a.y.toInt(), b.x.toInt(), b.y.toInt())
        }
    }
}

