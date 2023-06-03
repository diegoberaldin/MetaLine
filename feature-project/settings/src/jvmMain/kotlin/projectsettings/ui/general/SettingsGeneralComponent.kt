package projectsettings.ui.general

import data.LanguageModel
import kotlinx.coroutines.flow.StateFlow

interface SettingsGeneralComponent {
    val uiState: StateFlow<SettingsGeneralUiState>

    fun setLanguage(value: LanguageModel)
}