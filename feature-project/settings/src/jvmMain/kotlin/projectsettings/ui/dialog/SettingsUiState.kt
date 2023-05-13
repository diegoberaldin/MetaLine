package projectsettings.ui.dialog

import localized

data class SettingsUiState(
    val tabs: List<SettingsTab> = emptyList(),
    val currentTab: SettingsTab? = null,
)

enum class SettingsTab {
    GENERAL,
    SEGMENTATION_RULES,
}

fun SettingsTab.toReadableName(): String = when (this) {
    SettingsTab.SEGMENTATION_RULES -> "dialog_settings_tab_segmentation".localized()
    else -> "dialog_settings_tab_general".localized()
}
