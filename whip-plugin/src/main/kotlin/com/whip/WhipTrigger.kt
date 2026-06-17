package com.whip

import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.WindowManager
import javax.swing.JLayeredPane

object WhipTrigger {
    fun trigger(project: Project, options: WhipRuntimeOptions) {
        val whipCanvas = prepareCanvas(project) ?: return
        if (whipCanvas.isCalibrationActive()) return
        whipCanvas.crack(options)
    }

    fun isCalibrationActive(project: Project): Boolean {
        val frame = WindowManager.getInstance().getFrame(project) ?: return false
        val layeredPane = frame.rootPane?.layeredPane ?: return false
        return findCanvas(layeredPane)?.isCalibrationActive() == true
    }

    fun calibrate(project: Project) {
        val whipCanvas = prepareCanvas(project) ?: return
        val settings = project.service<WhipSettingsState>().state
        whipCanvas.startCalibration(
            options = settings.toRuntimeOptions(),
            onSave = { ratios ->
                settings.originXRatio = ratios.originXRatio
                settings.originYRatio = ratios.originYRatio
                settings.targetXRatio = ratios.targetXRatio
                settings.targetYRatio = ratios.targetYRatio
            },
        )
    }

    private fun prepareCanvas(project: Project): WhipCanvas? {
        val frame = WindowManager.getInstance().getFrame(project) ?: return null
        val layeredPane = frame.rootPane?.layeredPane ?: return null
        val whipCanvas = findCanvas(layeredPane) ?: WhipCanvas().also {
            layeredPane.add(it, JLayeredPane.POPUP_LAYER)
        }

        val canvasWidth = if (layeredPane.width > 0) layeredPane.width else frame.width
        val canvasHeight = if (layeredPane.height > 0) layeredPane.height else frame.height
        whipCanvas.setBounds(0, 0, canvasWidth, canvasHeight)
        whipCanvas.revalidate()
        return whipCanvas
    }

    private fun findCanvas(layeredPane: JLayeredPane): WhipCanvas? {
        for (component in layeredPane.components) {
            if (component is WhipCanvas) {
                return component
            }
        }
        return null
    }
}

