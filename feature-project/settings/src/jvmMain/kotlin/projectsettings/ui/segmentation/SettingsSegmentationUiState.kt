package projectsettings.ui.segmentation

import data.SegmentationRuleModel

data class SettingsSegmentationUiState(
    val rules: List<SegmentationRuleModel> = emptyList(),
    val currentEditedRule: Int? = null,
)
