package projectsettings.ui.segmentation

import data.LanguageModel
import data.SegmentationRuleModel

data class SettingsSegmentationUiState(
    val currentLanguage: LanguageModel? = null,
    val availableLanguages: List<LanguageModel> = emptyList(),
    val rules: List<SegmentationRuleModel> = emptyList(),
    val currentEditedRule: Int? = null,
)
