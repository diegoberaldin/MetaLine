package mainintro.ui

import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import common.coroutines.CoroutineDispatcherProvider
import common.notification.NotificationCenter
import common.notification.NotificationCenter.Event.OpenProject
import data.ProjectModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import repository.ProjectRepository

class IntroViewModel(
    private val dispatcherProvider: CoroutineDispatcherProvider,
    private val projectRepository: ProjectRepository,
    private val notificationCenter: NotificationCenter,
) : InstanceKeeper.Instance {

    private val projects = MutableStateFlow<List<ProjectModel>>(emptyList())
    private val viewModelScope = CoroutineScope(SupervisorJob())

    val uiState = projects.map {
        IntroUiState(projects = it)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = IntroUiState(),
    )

    init {
        viewModelScope.launch(dispatcherProvider.io) {
            launch {
                projectRepository.observeAll.distinctUntilChanged().collect() {
                    projects.value = it
                }
            }
        }
    }

    override fun onDestroy() {
        viewModelScope.cancel()
    }

    fun open(project: ProjectModel) {
        notificationCenter.send(OpenProject(projectId = project.id))
    }

    fun delete(project: ProjectModel) {
        viewModelScope.launch(dispatcherProvider.io) {
            projectRepository.delete(project)
            projects.update {
                projectRepository.getAll()
            }
        }
    }
}
