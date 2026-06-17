package com.whip

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage

@State(name = "WhippySettings", storages = [Storage("whippy.xml")])
class WhipSettingsState : PersistentStateComponent<WhipSettingsState.Settings> {
    data class Settings(
        var soundEnabled: Boolean = true,
        var autoEnabled: Boolean = false,
        var colorStyleId: String = WhipColorStyle.CLASSIC.id,
        var speedStyleId: String = WhipSpeedStyle.NORMAL.id,
        var originXRatio: Double? = null,
        var originYRatio: Double? = null,
        var targetXRatio: Double? = null,
        var targetYRatio: Double? = null,
    )

    private var settings = Settings()

    override fun getState(): Settings = settings

    override fun loadState(state: Settings) {
        settings = state
    }
}

