package com.whip

import com.intellij.openapi.components.service
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.project.ProjectManager
import java.awt.GridLayout
import javax.swing.JCheckBox
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel

class WhipSettingsConfigurable : Configurable {
    private val panel = JPanel(GridLayout(0, 1, 0, 6))
    private val autoCheck = JCheckBox("Auto-fire on Enter in editor")
    private val soundCheck = JCheckBox("Enable whip sound")

    init {
        panel.add(JLabel("Whippy settings"))
        panel.add(autoCheck)
        panel.add(soundCheck)
        panel.add(JLabel("Tip: You can still run Whippy manually with the keymap action 'Crack the Whip'."))
    }

    override fun getDisplayName(): String = "Whippy"

    override fun createComponent(): JComponent = panel

    override fun isModified(): Boolean {
        val current = currentSettings() ?: return false
        return autoCheck.isSelected != current.autoEnabled || soundCheck.isSelected != current.soundEnabled
    }

    override fun apply() {
        updateAllOpenProjects(autoCheck.isSelected, soundCheck.isSelected)
    }

    override fun reset() {
        val current = currentSettings() ?: return
        autoCheck.isSelected = current.autoEnabled
        soundCheck.isSelected = current.soundEnabled
    }

    private fun currentSettings(): WhipSettingsState.Settings? {
        val project = ProjectManager.getInstance().openProjects.firstOrNull() ?: return null
        return project.service<WhipSettingsState>().state
    }

    private fun updateAllOpenProjects(autoEnabled: Boolean, soundEnabled: Boolean) {
        ProjectManager.getInstance().openProjects.forEach { project ->
            val settings = project.service<WhipSettingsState>().state
            settings.autoEnabled = autoEnabled
            settings.soundEnabled = soundEnabled
        }
    }
}

