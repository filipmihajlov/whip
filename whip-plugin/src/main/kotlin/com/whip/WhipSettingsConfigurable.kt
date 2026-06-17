package com.whip

import com.intellij.openapi.components.service
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.wm.WindowManager
import java.awt.BorderLayout
import java.awt.GridLayout
import javax.swing.JButton
import javax.swing.JCheckBox
import javax.swing.JComboBox
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel

class WhipSettingsConfigurable : Configurable {
    private val panel = JPanel(GridLayout(0, 1, 0, 6))
    private val autoCheck = JCheckBox("Auto-fire on Enter across IDE (editor, terminal, chat)")
    private val soundCheck = JCheckBox("Enable whip sound")
    private val colorCombo = JComboBox(WhipColorStyle.entries.toTypedArray())
    private val speedCombo = JComboBox(WhipSpeedStyle.entries.toTypedArray())
    private val calibrateButton = JButton("Calibrate whip layout…")

    init {
        calibrateButton.addActionListener {
            activeProject()?.let(WhipTrigger::calibrate)
        }

        panel.add(JLabel("Whippy settings"))
        panel.add(autoCheck)
        panel.add(soundCheck)
        panel.add(labeledRow("Whip color", colorCombo))
        panel.add(labeledRow("Whip speed", speedCombo))
        panel.add(labeledRow("Whip layout", calibrateButton))
        panel.add(JLabel("Classic leather + Normal match the current whip."))
        panel.add(JLabel("Drag the green start and red tip handles to reposition or lengthen the whip."))
        panel.add(JLabel("Tip: You can still run Whippy manually with the keymap action 'Crack the Whip'."))
    }

    override fun getDisplayName(): String = "Whippy"

    override fun createComponent(): JComponent = panel

    override fun isModified(): Boolean {
        val current = currentSettings() ?: return false
        return autoCheck.isSelected != current.autoEnabled ||
            soundCheck.isSelected != current.soundEnabled ||
            selectedColorStyle().id != current.colorStyleId ||
            selectedSpeedStyle().id != current.speedStyleId
    }

    override fun apply() {
        updateAllOpenProjects(
            autoEnabled = autoCheck.isSelected,
            soundEnabled = soundCheck.isSelected,
            colorStyleId = selectedColorStyle().id,
            speedStyleId = selectedSpeedStyle().id,
        )
    }

    override fun reset() {
        val current = currentSettings() ?: return
        autoCheck.isSelected = current.autoEnabled
        soundCheck.isSelected = current.soundEnabled
        colorCombo.selectedItem = WhipColorStyle.fromId(current.colorStyleId)
        speedCombo.selectedItem = WhipSpeedStyle.fromId(current.speedStyleId)
    }

    private fun currentSettings(): WhipSettingsState.Settings? {
        val project = activeProject() ?: return null
        return project.service<WhipSettingsState>().state
    }

    private fun activeProject() = ProjectManager.getInstance().openProjects.firstOrNull { project ->
        WindowManager.getInstance().getFrame(project)?.isActive == true
    } ?: ProjectManager.getInstance().openProjects.firstOrNull()

    private fun updateAllOpenProjects(
        autoEnabled: Boolean,
        soundEnabled: Boolean,
        colorStyleId: String,
        speedStyleId: String,
    ) {
        ProjectManager.getInstance().openProjects.forEach { project ->
            val settings = project.service<WhipSettingsState>().state
            settings.autoEnabled = autoEnabled
            settings.soundEnabled = soundEnabled
            settings.colorStyleId = colorStyleId
            settings.speedStyleId = speedStyleId
        }
    }

    private fun selectedColorStyle(): WhipColorStyle =
        (colorCombo.selectedItem as? WhipColorStyle) ?: WhipColorStyle.CLASSIC

    private fun selectedSpeedStyle(): WhipSpeedStyle =
        (speedCombo.selectedItem as? WhipSpeedStyle) ?: WhipSpeedStyle.NORMAL

    private fun labeledRow(label: String, component: JComponent): JPanel = JPanel(BorderLayout(8, 0)).apply {
        isOpaque = false
        add(JLabel(label), BorderLayout.WEST)
        add(component, BorderLayout.CENTER)
    }
}

