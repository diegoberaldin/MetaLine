package projectedit.ui.dialog

import androidx.compose.runtime.snapshotFlow
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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import projectmetadata.ui.ProjectMetadataComponent
import projectsegmentation.ui.ProjectSegmentationViewModel
import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration

@OptIn(ExperimentalCoroutinesApi::class)
internal class DefaultEditProjectComponent(
    componentContext: ComponentContext,
    coroutineContext: CoroutineContext,
    private val dispatcherProvider: CoroutineDispatcherProvider,
) : EditProjectComponent, ComponentContext by componentContext {

    private val tabs = MutableStateFlow(listOf(EditProjectSection.METADATA, EditProjectSection.SEGMENTATION_RULES))
    private val currentTab = MutableStateFlow(EditProjectSection.METADATA)
    private lateinit var viewModelScope: CoroutineScope
    private val contentNavigation = SlotNavigation<EditProjectComponent.Config>()

    override lateinit var uiState: StateFlow<EditProjectUiState>
    override val content: Value<ChildSlot<EditProjectComponent.Config, *>> = childSlot(
        source = contentNavigation,
        key = "EditProjectContentSlow",
        childFactory = { config, context ->
            when (config) {
                EditProjectComponent.Config.Metadata -> getByInjection<ProjectMetadataComponent>(
                    context,
                    coroutineContext,
                )

                EditProjectComponent.Config.SegmentationRules -> getByInjection<ProjectSegmentationViewModel>(
                    context,
                    coroutineContext,
                )
            }
        },
    )
    override lateinit var onDone: SharedFlow<ProjectModel?>
    override var project: ProjectModel? = null
        set(value) {
            field = value
            loadProject(value)
        }

    init {
        with(lifecycle) {
            doOnCreate {
                viewModelScope = CoroutineScope(coroutineContext + SupervisorJob())
                uiState = combine(
                    tabs,
                    currentTab,
                ) { tabs, currentTab ->
                    EditProjectUiState(
                        tabs = tabs,
                        currentTab = currentTab,
                    )
                }.stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5_000),
                    initialValue = EditProjectUiState(),
                )
                onDone = content.asFlow<ProjectMetadataComponent>(timeout = Duration.INFINITE)
                    .flatMapLatest { it?.onDone ?: snapshotFlow { null } }
                    .shareIn(viewModelScope, started = SharingStarted.WhileSubscribed(5_000))

                contentNavigation.activate(EditProjectComponent.Config.Metadata)
                content.asFlow<ProjectMetadataComponent>(timeout = Duration.INFINITE).filterNotNull().onEach {
                    it.load(project)
                }.launchIn(viewModelScope)
            }
            doOnDestroy {
                viewModelScope.cancel()
            }
        }
    }

    private fun loadProject(value: ProjectModel?) {
        if (!::viewModelScope.isInitialized) return

        viewModelScope.launch {
            val first = content.asFlow<ProjectMetadataComponent>().first()
            first?.load(value)
        }
    }

    override fun selectTab(section: EditProjectSection) {
        currentTab.value = section
        viewModelScope.launch(dispatcherProvider.main) {
            when (section) {
                EditProjectSection.METADATA -> {
                    contentNavigation.activate(EditProjectComponent.Config.Metadata)
                }

                EditProjectSection.SEGMENTATION_RULES -> {
                    contentNavigation.activate(EditProjectComponent.Config.SegmentationRules)
                }
            }
        }
    }

    override fun submitMetadata() {
        viewModelScope.launch {
            content.asFlow<ProjectMetadataComponent>().first()?.submit()
        }
    }
}
