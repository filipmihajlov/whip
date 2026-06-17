package com.whip

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.wm.WindowManager
import javax.swing.JComponent
import javax.swing.JLayeredPane

class WhipAction : AnAction(), DumbAware {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val frame = WindowManager.getInstance().getFrame(project) ?: return
        
        // Find or create the whip overlay component
        val rootPane = (frame.contentPane as? JComponent) ?: return
        var whipCanvas: WhipCanvas? = null
        
        // Search for existing whip canvas
        for (component in rootPane.components) {
            if (component is WhipCanvas) {
                whipCanvas = component
                break
            }
        }
        
        // Create if not exists
        if (whipCanvas == null) {
            whipCanvas = WhipCanvas()
            rootPane.add(whipCanvas, JLayeredPane.POPUP_LAYER)
            rootPane.setComponentZOrder(whipCanvas, 0)
        }
        
        // Trigger the whip
        whipCanvas.crack()
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabled = e.project != null
    }
}

