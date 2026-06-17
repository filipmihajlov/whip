package com.whip

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage

@State(name = "WhippySettings", storages = [Storage("whippy.xml")])
class WhipSettingsState : PersistentStateComponent<WhipSettingsState.Settings> {
    data class Settings(
        var soundEnabled: Boolean = true,
        var autoEnabled: Boolean = false,
    )

    private var settings = Settings()

    override fun getState(): Settings = settings

    override fun loadState(state: Settings) {
        settings = state
    }
}

