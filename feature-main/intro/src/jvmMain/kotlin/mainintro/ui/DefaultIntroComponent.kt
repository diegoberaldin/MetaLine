package mainintro.ui

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.doOnCreate
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.arkivanov.essenty.lifecycle.doOnStart
import common.coroutines.CoroutineDispatcherProvider
import common.notification.NotificationCenter
import common.notification.NotificationCenter.Event.OpenProject
import data.ProjectModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import repository.ProjectRepository
import kotlin.coroutines.CoroutineContext

internal class DefaultIntroComponent(
    componentContext: ComponentContext,
    coroutineContext: CoroutineContext,
    private val dispatcherProvider: CoroutineDispatcherProvider,
    private val projectRepository: ProjectRepository,
    private val notificationCenter: NotificationCenter,
) : IntroComponent, ComponentContext by componentContext {

    private val projects = MutableStateFlow<List<ProjectModel>>(emptyList())
    private lateinit var viewModelScope: CoroutineScope

    override lateinit var uiState: StateFlow<IntroUiState>

    init {
        with(lifecycle) {
            doOnCreate {
                viewModelScope = CoroutineScope(coroutineContext + SupervisorJob())
                uiState = projects.map {
                    IntroUiState(projects = it)
                }.stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5_000),
                    initialValue = IntroUiState(),
                )
            }
            doOnStart {
                viewModelScope.launch(dispatcherProvider.io) {
                    launch {
                        projectRepository.observeAll.distinctUntilChanged().collect() {
                            projects.value = it
                        }
                    }
                }
            }
            doOnDestroy {
                viewModelScope.cancel()
            }
        }
    }

    override fun open(project: ProjectModel) {
        notificationCenter.send(OpenProject(projectId = project.id))
    }

    override fun delete(project: ProjectModel) {
        viewModelScope.launch(dispatcherProvider.io) {
            projectRepository.delete(project)
            projects.update {
                projectRepository.getAll()
            }
        }
    }
}
