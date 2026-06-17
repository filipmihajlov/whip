package com.whip

import com.intellij.openapi.editor.event.EditorFactoryEvent
import com.intellij.openapi.editor.event.EditorFactoryListener
import com.intellij.openapi.components.service
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.util.WeakHashMap

class WhipEditorListener : EditorFactoryListener {
    private val listeners = WeakHashMap<com.intellij.openapi.editor.Editor, KeyAdapter>()

    override fun editorCreated(event: EditorFactoryEvent) {
        val editor = event.editor
        val project = editor.project ?: return

        val keyListener = object : KeyAdapter() {
            override fun keyPressed(e: KeyEvent) {
                if (e.keyCode != KeyEvent.VK_ENTER) return
                if (e.isAltDown || e.isControlDown || e.isMetaDown) return

                val settings = project.service<WhipSettingsState>().state
                if (!settings.autoEnabled) return

                WhipTrigger.trigger(project, settings.soundEnabled)
            }
        }

        editor.contentComponent.addKeyListener(keyListener)
        listeners[editor] = keyListener
    }

    override fun editorReleased(event: EditorFactoryEvent) {
        val editor = event.editor
        val keyListener = listeners.remove(editor) ?: return
        editor.contentComponent.removeKeyListener(keyListener)
    }
}


