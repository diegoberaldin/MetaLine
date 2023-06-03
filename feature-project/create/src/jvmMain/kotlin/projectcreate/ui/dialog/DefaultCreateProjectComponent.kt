package projectcreate.ui.dialog

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.slot.ChildSlot
import com.arkivanov.decompose.router.slot.SlotNavigation
import com.arkivanov.decompose.router.slot.activate
import com.arkivanov.decompose.router.slot.childSlot
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.lifecycle.doOnCreate
import com.arkivanov.essenty.lifecycle.doOnDestroy
import common.coroutines.CoroutineDispatcherProvider
import common.utils.asFlow
import common.utils.getByInjection
import data.ProjectModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import projectmetadata.ui.ProjectMetadataComponent
import projectsegmentation.ui.ProjectSegmentationComponent
import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration

internal class DefaultCreateProjectComponent(
    componentContext: ComponentContext,
    coroutineContext: CoroutineContext,
    private val dispatcherProvider: CoroutineDispatcherProvider,
) : CreateProjectComponent, ComponentContext by componentContext {

    private val contentNavigation = SlotNavigation<CreateProjectComponent.Config>()
    private lateinit var viewModelScope: CoroutineScope
    private val project = MutableStateFlow<ProjectModel?>(null)
    override lateinit var uiState: StateFlow<CreateProjectUiState>
    override val content: Value<ChildSlot<CreateProjectComponent.Config, *>> = childSlot(
        source = contentNavigation,
        key = "CreateProjectContent",
        childFactory = { config, context ->
            when (config) {
                CreateProjectComponent.Config.Metadata -> getByInjection<ProjectMetadataComponent>(
                    context,
                    coroutineContext,
                )

                CreateProjectComponent.Config.SegmentationRules -> getByInjection<ProjectSegmentationComponent>(
                    context,
                    coroutineContext,
                )
            }
        },
    )

    init {
        with(lifecycle) {
            doOnCreate {
                viewModelScope = CoroutineScope(coroutineContext + SupervisorJob())
                uiState = combine(
                    project,
                ) { args ->
                    CreateProjectUiState(
                        project = args.first(),
                    )
                }.stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5_000),
                    initialValue = CreateProjectUiState(),
                )
                content.asFlow<ProjectMetadataComponent>(timeout = Duration.INFINITE).filterNotNull().onEach {
                    it.onDone.collect { project ->
                        setProject(project)
                        next()
                    }
                }.launchIn(viewModelScope)
                contentNavigation.activate(CreateProjectComponent.Config.Metadata)
            }
            doOnDestroy {
                viewModelScope.cancel()
            }
        }
    }

    override fun setProject(value: ProjectModel?) {
        project.value = value
    }

    override fun next() {
        val config = content.value.child?.configuration
        if (config == CreateProjectComponent.Config.Metadata) {
            viewModelScope.launch(dispatcherProvider.main) {
                contentNavigation.activate(CreateProjectComponent.Config.SegmentationRules)
            }
        }
    }

    override fun submitMetadata() {
        viewModelScope.launch {
            content.asFlow<ProjectMetadataComponent>().first()?.submit()
        }
    }
}
