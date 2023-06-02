package main.ui

import data.ProjectModel
import kotlinx.coroutines.flow.StateFlow

interface MainComponent {
    val uiState: StateFlow<MainUiState>
    fun openProject(model: ProjectModel)
    fun closeProject()
    fun openFilePair(index: Int)
    fun selectFilePair(index: Int)
    fun closeFilePair(index: Int)
    fun exportTmx(path: String)
}
