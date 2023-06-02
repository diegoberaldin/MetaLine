package mainintro.ui

import data.ProjectModel
import kotlinx.coroutines.flow.StateFlow

interface IntroComponent {

    val uiState: StateFlow<IntroUiState>

    fun open(project: ProjectModel)
    fun delete(project: ProjectModel)
}