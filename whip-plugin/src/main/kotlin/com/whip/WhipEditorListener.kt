package com.whip

import com.intellij.ide.DataManager
import com.intellij.ide.IdeEventQueue
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.editor.event.EditorFactoryEvent
import com.intellij.openapi.editor.event.EditorFactoryListener
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.util.Disposer
import java.awt.Component
import java.awt.event.KeyEvent
import java.util.concurrent.atomic.AtomicBoolean

class WhipEditorListener : EditorFactoryListener {
    companion object {
        private val dispatcherInstalled = AtomicBoolean(false)
        private val dispatcherDisposable = Disposer.newDisposable("WhipGlobalEnterDispatcher")
    }

    override fun editorCreated(event: EditorFactoryEvent) {
        if (!dispatcherInstalled.compareAndSet(false, true)) return
        IdeEventQueue.getInstance().addDispatcher({ awtEvent ->
            val e = awtEvent as? KeyEvent ?: return@addDispatcher false
            if (e.id != KeyEvent.KEY_PRESSED) return@addDispatcher false
            if (e.keyCode != KeyEvent.VK_ENTER) return@addDispatcher false
            if (e.isAltDown || e.isControlDown || e.isMetaDown) return@addDispatcher false

            val component = e.component ?: return@addDispatcher false
            val project = resolveProject(component) ?: return@addDispatcher false
            if (project.isDisposed) return@addDispatcher false

            val settings = project.service<WhipSettingsState>().state
            if (!settings.autoEnabled) return@addDispatcher false
            if (WhipTrigger.isCalibrationActive(project)) return@addDispatcher false

            WhipTrigger.trigger(project, settings.toRuntimeOptions())
            false
        }, dispatcherDisposable)
    }

    private fun resolveProject(component: Component): Project? {
        val project = CommonDataKeys.PROJECT.getData(DataManager.getInstance().getDataContext(component))
        if (project != null) return project

        val openProjects = ProjectManager.getInstance().openProjects
        return openProjects.singleOrNull()
    }
}


