package com.whip

import java.awt.BasicStroke
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Cursor
import java.awt.FlowLayout
import java.awt.Font
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.event.ActionEvent
import java.awt.event.KeyEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JButton
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.KeyStroke
import javax.swing.SwingConstants
import javax.swing.Timer
import javax.swing.border.CompoundBorder
import javax.swing.border.EmptyBorder
import javax.swing.border.LineBorder
import kotlin.math.hypot
import kotlin.math.pow
import kotlin.math.roundToInt

class WhipCanvas : JPanel() {
    private val verletRope = VerletRope(24)
    private var isActive = false
    private var soundPlayer = WhipSound()
    private var animationTimer: Timer? = null
    private var activeColorStyle = WhipColorStyle.CLASSIC

    private var calibrationState: CalibrationState? = null
    private var draggingHandle: DragHandle? = null

    private val calibrationMargin = 28.0
    private val handleRadius = 22.0
    private val originHandleColor = Color(74, 222, 128)
    private val targetHandleColor = Color(255, 77, 77)
    private val calibrationInfo = JLabel("", SwingConstants.CENTER)
    private val calibrationPanel = JPanel(BorderLayout(0, 8))
    private val calibrationHint = JLabel(
        "<html>Drag the <b><font color='#4ade80'>green</font></b> start and " +
            "<b><font color='#ff4d4d'>red</font></b> tip handles. Press Enter to save, " +
            "Esc to cancel, or Space to test.</html>",
        SwingConstants.CENTER,
    )

    private val easeOut: (Double) -> Double = { t -> 1 - (1 - t).pow(3) }
    private val easeIn: (Double) -> Double = { t -> t * t * t }
    private val easeInOut: (Double) -> Double = { t ->
        if (t < 0.5) 4 * t * t * t else 1 - (-2 * t + 2).pow(3) / 2
    }

    init {
        isOpaque = false
        isVisible = false
        isFocusable = true
        layout = null
        setupCalibrationPanel()
        setupCalibrationInput()
    }

    private fun clamp(value: Double, min: Double, max: Double): Double {
        return value.coerceIn(min, max)
    }

    private fun syncVisibility() {
        calibrationPanel.isVisible = calibrationState != null
        isVisible = isActive || calibrationState != null
        revalidate()
        repaint()
    }

    private fun stopAnimation() {
        isActive = false
        animationTimer?.stop()
        animationTimer = null
        syncVisibility()
    }

    fun isCalibrationActive(): Boolean = calibrationState != null

    fun crack(options: WhipRuntimeOptions = WhipRuntimeOptions(true, WhipColorStyle.CLASSIC, WhipSpeedStyle.NORMAL)) {
        if (isCalibrationActive()) return
        runCrack(options, options.layoutRatios)
    }

    fun startCalibration(
        options: WhipRuntimeOptions,
        onSave: (WhipLayoutRatios) -> Unit,
        onCancel: () -> Unit = {},
    ) {
        stopAnimation()
        draggingHandle = null
        calibrationState = CalibrationState(
            layout = options.layoutRatios.resolve(width, height, calibrationMargin),
            previewOptions = options,
            onSave = onSave,
            onCancel = onCancel,
        )
        updateCalibrationInfo()
        syncVisibility()
        requestFocusInWindow()
    }

    fun cancelCalibration() {
        closeCalibration(save = false)
    }

    override fun doLayout() {
        super.doLayout()
        val pref = calibrationPanel.preferredSize
        val x = ((width - pref.width) / 2).coerceAtLeast(20)
        calibrationPanel.setBounds(x, 24, pref.width, pref.height)
    }

    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)
        if (!isActive && calibrationState == null) return

        val g2d = g as Graphics2D
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)

        calibrationState?.let { drawCalibrationOverlay(g2d, it) }
        if (isActive) {
            drawRope(g2d)
        }
    }

    private fun runCrack(options: WhipRuntimeOptions, layoutRatios: WhipLayoutRatios) {
        if (isActive) return

        isActive = true
        syncVisibility()
        activeColorStyle = options.colorStyle
        if (options.playSound) {
            Thread({ soundPlayer.playCrack(1.0) }, "whippy-sound").apply {
                isDaemon = true
                start()
            }
        }

        val layout = layoutRatios.resolve(width, height, calibrationMargin)
        val origin = layout.originPoint
        val target = layout.targetPoint

        verletRope.resetRope(origin.x.toDouble(), origin.y.toDouble())
        verletRope.setTipPinned(true)
        val timings = options.speedStyle.timings()

        val dx = target.x - origin.x
        val dy = target.y - origin.y
        val dist = hypot(dx.toDouble(), dy.toDouble())
        val safeDist = if (dist > 0.001) dist else 1.0
        verletRope.setSegmentLength(maxOf(14.0, (safeDist * 1.08) / (verletRope.points.size - 1)))

        val dirX = dx / safeDist
        val dirY = dy / safeDist
        val windBack = Pair(
            clamp(origin.x - dirX * 90, calibrationMargin, width - calibrationMargin),
            clamp(origin.y - dirY * 90 - 80, calibrationMargin, height - calibrationMargin),
        )

        val startTime = System.currentTimeMillis()

        animationTimer?.stop()
        animationTimer = Timer(16) { _: ActionEvent ->
            try {
                val elapsed = System.currentTimeMillis() - startTime

                when {
                    elapsed < timings.windMs -> {
                        val k = easeOut(elapsed.toDouble() / timings.windMs)
                        verletRope.tipPos = Pair(
                            origin.x + (windBack.first - origin.x) * k,
                            origin.y + (windBack.second - origin.y) * k,
                        )
                        verletRope.handlePos = Pair(
                            clamp(origin.x - dirX * 10 * k, calibrationMargin, width - calibrationMargin),
                            clamp(origin.y - dirY * 10 * k, calibrationMargin, height - calibrationMargin),
                        )
                    }

                    elapsed < timings.windMs + timings.snapMs -> {
                        val k = easeIn((elapsed - timings.windMs).toDouble() / timings.snapMs)
                        verletRope.tipPos = Pair(
                            windBack.first + (target.x - windBack.first) * k,
                            windBack.second + (target.y - windBack.second) * k,
                        )
                        val hk = easeOut((elapsed - timings.windMs).toDouble() / timings.snapMs)
                        verletRope.handlePos = Pair(
                            clamp(origin.x + dirX * 35 * hk, calibrationMargin, width - calibrationMargin),
                            clamp(origin.y + dirY * 35 * hk, calibrationMargin, height - calibrationMargin),
                        )
                    }

                    elapsed < timings.windMs + timings.snapMs + timings.holdMs -> {
                        verletRope.tipPos = Pair(target.x.toDouble(), target.y.toDouble())
                    }

                    elapsed < timings.windMs + timings.snapMs + timings.holdMs + timings.recoilMs -> {
                        verletRope.setTipPinned(false)
                        val k = easeInOut(
                            (elapsed - timings.windMs - timings.snapMs - timings.holdMs).toDouble() / timings.recoilMs,
                        )
                        verletRope.handlePos = Pair(
                            clamp(origin.x + dirX * 35 * (1 - k), calibrationMargin, width - calibrationMargin),
                            clamp(origin.y + dirY * 35 * (1 - k), calibrationMargin, height - calibrationMargin),
                        )
                    }

                    else -> {
                        stopAnimation()
                        return@Timer
                    }
                }

                verletRope.update()
                repaint()
            } catch (_: Throwable) {
                stopAnimation()
            }
        }

        animationTimer?.start()
    }

    private fun drawRope(g: Graphics2D) {
        val points = verletRope.points

        g.stroke = BasicStroke(7f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)

        for (i in 0 until points.size - 1) {
            val a = points[i]
            val b = points[i + 1]
            val t = i.toDouble() / (points.size - 1)
            val w = (7 * (1 - t) + 0.8 * t).toFloat()

            g.color = activeColorStyle.shadowColor
            g.stroke = BasicStroke(w + 1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)
            g.drawLine(
                (a.x + 1.5).toInt(),
                (a.y + 2.5).toInt(),
                (b.x + 1.5).toInt(),
                (b.y + 2.5).toInt(),
            )

            g.color = activeColorStyle.colorAt(t)
            g.stroke = BasicStroke(w, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)
            g.drawLine(a.x.toInt(), a.y.toInt(), b.x.toInt(), b.y.toInt())
        }
    }

    private fun setupCalibrationPanel() {
        val title = JLabel("Whip layout", SwingConstants.CENTER).apply {
            foreground = Color.WHITE
            font = font.deriveFont(Font.BOLD, 16f)
        }
        calibrationHint.foreground = Color(207, 210, 216)
        calibrationInfo.foreground = Color.WHITE

        val buttons = JPanel(FlowLayout(FlowLayout.CENTER, 8, 0)).apply {
            isOpaque = false
            add(JButton("Save").apply { addActionListener { closeCalibration(save = true) } })
            add(JButton("Test Crack").apply { addActionListener { previewCalibrationCrack() } })
            add(JButton("Reset Defaults").apply { addActionListener { resetCalibrationLayout() } })
            add(JButton("Cancel").apply { addActionListener { closeCalibration(save = false) } })
        }

        val content = JPanel().apply {
            isOpaque = false
            layout = javax.swing.BoxLayout(this, javax.swing.BoxLayout.Y_AXIS)
            add(title)
            add(JLabel(" "))
            add(calibrationHint)
            add(JLabel(" "))
            add(calibrationInfo)
            add(JLabel(" "))
            add(buttons)
        }

        calibrationPanel.apply {
            isOpaque = true
            background = Color(43, 45, 48, 235)
            border = CompoundBorder(LineBorder(Color(57, 59, 64)), EmptyBorder(14, 18, 14, 18))
            add(content, BorderLayout.CENTER)
            isVisible = false
        }
        add(calibrationPanel)
    }

    private fun setupCalibrationInput() {
        val mouseHandler = object : MouseAdapter() {
            override fun mousePressed(e: MouseEvent) {
                val state = calibrationState ?: return
                requestFocusInWindow()
                draggingHandle = findHandle(state.layout, e.x.toDouble(), e.y.toDouble()) ?: return
                updateDraggedHandle(e.x.toDouble(), e.y.toDouble())
            }

            override fun mouseDragged(e: MouseEvent) {
                if (draggingHandle == null) return
                updateDraggedHandle(e.x.toDouble(), e.y.toDouble())
            }

            override fun mouseReleased(e: MouseEvent) {
                draggingHandle = null
                updateCursor(e.x.toDouble(), e.y.toDouble())
            }

            override fun mouseMoved(e: MouseEvent) {
                updateCursor(e.x.toDouble(), e.y.toDouble())
            }
        }
        addMouseListener(mouseHandler)
        addMouseMotionListener(mouseHandler)

        registerKeyboardAction(
            { closeCalibration(save = true) },
            KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0),
            WHEN_IN_FOCUSED_WINDOW,
        )
        registerKeyboardAction(
            { closeCalibration(save = false) },
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
            WHEN_IN_FOCUSED_WINDOW,
        )
        registerKeyboardAction(
            { previewCalibrationCrack() },
            KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0),
            WHEN_IN_FOCUSED_WINDOW,
        )
    }

    private fun updateDraggedHandle(mouseX: Double, mouseY: Double) {
        val state = calibrationState ?: return
        val clampedX = clamp(mouseX, calibrationMargin, width - calibrationMargin)
        val clampedY = clamp(mouseY, calibrationMargin, height - calibrationMargin)
        state.layout = when (draggingHandle) {
            DragHandle.ORIGIN -> state.layout.copy(originX = clampedX, originY = clampedY)
            DragHandle.TARGET -> state.layout.copy(targetX = clampedX, targetY = clampedY)
            null -> state.layout
        }
        updateCalibrationInfo()
        repaint()
    }

    private fun updateCursor(x: Double, y: Double) {
        cursor = if (calibrationState != null && findHandle(calibrationState!!.layout, x, y) != null) {
            Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        } else {
            Cursor.getDefaultCursor()
        }
    }

    private fun findHandle(layout: WhipLayout, x: Double, y: Double): DragHandle? {
        if (distanceSquared(x, y, layout.originX, layout.originY) <= handleRadius * handleRadius) {
            return DragHandle.ORIGIN
        }
        if (distanceSquared(x, y, layout.targetX, layout.targetY) <= handleRadius * handleRadius) {
            return DragHandle.TARGET
        }
        return null
    }

    private fun distanceSquared(ax: Double, ay: Double, bx: Double, by: Double): Double {
        val dx = ax - bx
        val dy = ay - by
        return dx * dx + dy * dy
    }

    private fun drawCalibrationOverlay(g: Graphics2D, state: CalibrationState) {
        g.color = Color(0, 0, 0, 150)
        g.fillRect(0, 0, width, height)

        g.color = Color(255, 255, 255, 120)
        g.stroke = BasicStroke(
            3f,
            BasicStroke.CAP_ROUND,
            BasicStroke.JOIN_ROUND,
            10f,
            floatArrayOf(12f, 10f),
            0f,
        )
        g.drawLine(
            state.layout.originX.roundToInt(),
            state.layout.originY.roundToInt(),
            state.layout.targetX.roundToInt(),
            state.layout.targetY.roundToInt(),
        )

        drawCalibrationHandle(
            g = g,
            x = state.layout.originX,
            y = state.layout.originY,
            color = originHandleColor,
            label = "S",
            active = draggingHandle == DragHandle.ORIGIN,
        )
        drawCalibrationHandle(
            g = g,
            x = state.layout.targetX,
            y = state.layout.targetY,
            color = targetHandleColor,
            label = "T",
            active = draggingHandle == DragHandle.TARGET,
        )
    }

    private fun drawCalibrationHandle(
        g: Graphics2D,
        x: Double,
        y: Double,
        color: Color,
        label: String,
        active: Boolean,
    ) {
        val radius = if (active) handleRadius + 4 else handleRadius
        val left = (x - radius).roundToInt()
        val top = (y - radius).roundToInt()
        val diameter = (radius * 2).roundToInt()

        g.color = Color(color.red, color.green, color.blue, 54)
        g.fillOval(left - 6, top - 6, diameter + 12, diameter + 12)
        g.color = Color(255, 255, 255, 48)
        g.fillOval(left, top, diameter, diameter)
        g.color = color
        g.stroke = BasicStroke(3f)
        g.drawOval(left, top, diameter, diameter)
        g.font = g.font.deriveFont(Font.BOLD, 15f)
        g.color = Color.WHITE
        val metrics = g.fontMetrics
        val textX = x.roundToInt() - metrics.stringWidth(label) / 2
        val textY = y.roundToInt() + (metrics.ascent - metrics.descent) / 2
        g.drawString(label, textX, textY)
    }

    private fun previewCalibrationCrack() {
        val state = calibrationState ?: return
        val ratios = WhipLayoutRatios.fromLayout(state.layout, width, height)
        runCrack(state.previewOptions.copy(layoutRatios = ratios), ratios)
    }

    private fun resetCalibrationLayout() {
        val state = calibrationState ?: return
        state.layout = WhipLayoutRatios().resolve(width, height, calibrationMargin)
        updateCalibrationInfo()
        repaint()
    }

    private fun updateCalibrationInfo() {
        val state = calibrationState ?: return
        calibrationInfo.text = "Whip span: ${state.layout.span().roundToInt()} px — saved relative to the window, so it scales when the IDE resizes."
    }

    private fun closeCalibration(save: Boolean) {
        val state = calibrationState ?: return
        stopAnimation()
        draggingHandle = null
        cursor = Cursor.getDefaultCursor()
        calibrationState = null
        syncVisibility()
        if (save) {
            state.onSave(WhipLayoutRatios.fromLayout(state.layout, width, height))
        } else {
            state.onCancel()
        }
    }

    private data class CalibrationState(
        var layout: WhipLayout,
        val previewOptions: WhipRuntimeOptions,
        val onSave: (WhipLayoutRatios) -> Unit,
        val onCancel: () -> Unit,
    )

    private enum class DragHandle {
        ORIGIN,
        TARGET,
    }
}
