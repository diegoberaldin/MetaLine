package projectstatistics.ui.dialog

import data.ProjectModel
import kotlinx.coroutines.flow.StateFlow

interface StatisticsComponent {
    val uiState: StateFlow<StatisticsUiState>

    fun load(project: ProjectModel)
}