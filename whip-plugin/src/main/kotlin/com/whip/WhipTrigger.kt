package com.whip

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.WindowManager
import javax.swing.JComponent
import javax.swing.JLayeredPane

object WhipTrigger {
    fun trigger(project: Project, playSound: Boolean) {
        val frame = WindowManager.getInstance().getFrame(project) ?: return
        val rootPane = frame.contentPane as? JComponent ?: return

        var whipCanvas: WhipCanvas? = null
        for (component in rootPane.components) {
            if (component is WhipCanvas) {
                whipCanvas = component
                break
            }
        }

        if (whipCanvas == null) {
            whipCanvas = WhipCanvas()
            rootPane.add(whipCanvas, JLayeredPane.POPUP_LAYER)
            rootPane.setComponentZOrder(whipCanvas, 0)
        }

        whipCanvas.crack(playSound)
    }
}

