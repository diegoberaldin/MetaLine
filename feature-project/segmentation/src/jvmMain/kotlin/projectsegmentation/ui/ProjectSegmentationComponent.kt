package projectsegmentation.ui

import data.LanguageModel
import data.ProjectModel
import kotlinx.coroutines.flow.StateFlow

interface ProjectSegmentationComponent {
    val uiState: StateFlow<ProjectSegmentationUiState>

    fun load(project: ProjectModel)
    fun setCurrentLanguage(lang: LanguageModel)
    fun deleteRule(index: Int)
    fun createRule()
    fun toggleEditRule(index: Int)
    fun editRuleBeforePattern(text: String, index: Int)
    fun editRuleAfterPattern(text: String, index: Int)
    fun toggleBreaking(index: Int)
    fun moveRuleUp(index: Int)
    fun moveRuleDown(index: Int)
    fun toggleApplyDefaultRules()
}