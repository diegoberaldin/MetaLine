package projectcreate.ui.dialog

import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import data.ProjectModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.stateIn

class CreateProjectViewModel() : InstanceKeeper.Instance {

    private val step = MutableStateFlow(0)
    private val viewModelScope = CoroutineScope(SupervisorJob())
    private val project = MutableStateFlow<ProjectModel?>(null)

    val uiState = combine(
        step,
        project,
    ) { step, project ->
        CreateProjectUiState(
            step = step,
            project = project,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = CreateProjectUiState(),
    )

    override fun onDestroy() {
        viewModelScope.cancel()
    }

    fun setProject(value: ProjectModel?) {
        project.value = value
    }

    fun next() {
        step.getAndUpdate { it + 1 }
    }
}
