package projectsettings.ui.general

import data.LanguageModel

data class SettingsGeneralUiState(
    val appLanguage: LanguageModel? = null,
    val appVersion: String = "",
    val availableLanguages: List<LanguageModel> = emptyList(),
)
