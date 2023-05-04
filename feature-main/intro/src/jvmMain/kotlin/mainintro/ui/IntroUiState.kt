package mainintro.ui

import data.ProjectModel

data class IntroUiState(
    val projects: List<ProjectModel> = emptyList(),
)
