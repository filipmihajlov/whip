package com.whip

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.WindowManager
import javax.swing.JLayeredPane

object WhipTrigger {
    fun trigger(project: Project, playSound: Boolean) {
        val frame = WindowManager.getInstance().getFrame(project) ?: return
        val layeredPane = frame.rootPane?.layeredPane ?: return

        var whipCanvas: WhipCanvas? = null
        for (component in layeredPane.components) {
            if (component is WhipCanvas) {
                whipCanvas = component
                break
            }
        }

        if (whipCanvas == null) {
            whipCanvas = WhipCanvas()
            layeredPane.add(whipCanvas, JLayeredPane.POPUP_LAYER)
        }

        // Keep the canvas pinned to the IDE frame size before each crack.
        val canvasWidth = if (layeredPane.width > 0) layeredPane.width else frame.width
        val canvasHeight = if (layeredPane.height > 0) layeredPane.height else frame.height
        whipCanvas.setBounds(0, 0, canvasWidth, canvasHeight)
        whipCanvas.revalidate()
        whipCanvas.crack(playSound)
    }
}

