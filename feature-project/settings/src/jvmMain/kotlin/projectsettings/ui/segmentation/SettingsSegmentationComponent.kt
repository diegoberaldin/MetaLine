package projectsettings.ui.segmentation

import data.LanguageModel
import kotlinx.coroutines.flow.StateFlow

interface SettingsSegmentationComponent {
    val uiState: StateFlow<SettingsSegmentationUiState>

    fun setCurrentLanguage(lang: LanguageModel)
    fun deleteRule(index: Int)
    fun createRule()
    fun toggleEditRule(index: Int)
    fun editRuleBeforePattern(text: String, index: Int)
    fun editRuleAfterPattern(text: String, index: Int)
    fun toggleBreaking(index: Int)
    fun moveRuleUp(index: Int)
    fun moveRuleDown(index: Int)
}