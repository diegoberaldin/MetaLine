package projectcreate.ui.dialog

import data.ProjectModel

data class CreateProjectUiState(
    val step: Int = 0,
    val project: ProjectModel? = null,
)
