package projectsegmentation.ui

import data.LanguageModel
import data.SegmentationRuleModel

data class ProjectSegmentationUiState(
    val applyDefaultRules: Boolean = true,
    val currentLanguage: LanguageModel? = null,
    val availableLanguages: List<LanguageModel> = emptyList(),
    val rules: List<SegmentationRuleModel> = emptyList(),
    val currentEditedRule: Int? = null,
)
