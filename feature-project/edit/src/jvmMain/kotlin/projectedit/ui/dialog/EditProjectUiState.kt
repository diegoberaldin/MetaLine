package projectedit.ui.dialog

import localized

data class EditProjectUiState(
    val tabs: List<EditProjectSection> = emptyList(),
    val currentTab: EditProjectSection = EditProjectSection.METADATA,
)

enum class EditProjectSection {
    METADATA,
    SEGMENTATION_RULES,
}

fun EditProjectSection.toReadableName() = when (this) {
    EditProjectSection.SEGMENTATION_RULES -> "dialog_settings_tab_segmentation".localized()
    else -> "dialog_project_metadata".localized()
}
