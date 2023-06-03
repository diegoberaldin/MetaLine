package projectmetadata.ui

import data.LanguageModel
import data.ProjectModel
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface ProjectMetadataComponent {
    val onDone: SharedFlow<ProjectModel>
    val uiState: StateFlow<ProjectMetadataUiState>
    val languagesUiState: StateFlow<ProjectMetadataLanguagesUiState>
    val fileUiState: StateFlow<ProjectMetadataFileUiState>
    val errorUiState: StateFlow<CreateProjectErrorState>

    fun load(project: ProjectModel?)
    fun setName(value: String)
    fun setSourceLanguage(value: LanguageModel)
    fun setTargetLanguage(value: LanguageModel)
    fun selectSourceFile(value: Int)
    fun selectTargetFile(value: Int)
    fun addSourceFile(path: String)
    fun addTargetFile(path: String)
    fun moveSourceUp()
    fun moveSourceDown()
    fun moveTargetUp()
    fun moveTargetDown()
    fun deleteSourceFile()
    fun deleteTargetFile()
    fun submit()
}
