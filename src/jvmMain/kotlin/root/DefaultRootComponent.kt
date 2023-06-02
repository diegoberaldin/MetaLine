package root

import androidx.compose.runtime.snapshotFlow
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.slot.ChildSlot
import com.arkivanov.decompose.router.slot.SlotNavigation
import com.arkivanov.decompose.router.slot.activate
import com.arkivanov.decompose.router.slot.childSlot
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.lifecycle.doOnCreate
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.arkivanov.essenty.lifecycle.doOnStart
import common.coroutines.CoroutineDispatcherProvider
import common.utils.asFlow
import common.utils.getByInjection
import data.ProjectModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import main.ui.MainComponent
import projectcreate.ui.dialog.CreateProjectComponent
import projectedit.ui.dialog.EditProjectComponent
import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration

@OptIn(ExperimentalCoroutinesApi::class)
internal class DefaultRootComponent(
    componentContext: ComponentContext,
    coroutineContext: CoroutineContext,
    private val dispatcherProvider: CoroutineDispatcherProvider,
) : RootComponent, ComponentContext by componentContext {

    private lateinit var viewModelScope: CoroutineScope
    private val dialogNavigation = SlotNavigation<RootComponent.DialogConfig>()
    private val mainNavigation = SlotNavigation<RootComponent.MainConfig>()

    override lateinit var currentProject: StateFlow<ProjectModel?>

    override lateinit var isEditing: StateFlow<Boolean>

    override lateinit var needsSaving: StateFlow<Boolean>

    override val dialog: Value<ChildSlot<RootComponent.DialogConfig, *>> = childSlot(
        source = dialogNavigation,
        key = "RootDialogSlot",
        childFactory = { config, context ->
            when (config) {
                RootComponent.DialogConfig.NewProject -> {
                    getByInjection<CreateProjectComponent>(context, coroutineContext)
                }

                RootComponent.DialogConfig.EditProject -> {
                    getByInjection<EditProjectComponent>(context, coroutineContext)
                }

                else -> Unit
            }
        },
    )
    override val main: Value<ChildSlot<RootComponent.MainConfig, MainComponent>> = childSlot(
        source = mainNavigation,
        key = "RootMainSlot",
        childFactory = { _, _ -> getByInjection(componentContext, coroutineContext) },
    )

    init {
        with(lifecycle) {
            doOnCreate {
                viewModelScope = CoroutineScope(coroutineContext + SupervisorJob())
                currentProject = main.asFlow<MainComponent>(true, Duration.INFINITE)
                    .flatMapLatest { it?.uiState ?: snapshotFlow { null } }
                    .mapLatest { it?.project }
                    .stateIn(
                        scope = viewModelScope,
                        started = SharingStarted.Eagerly,
                        initialValue = null,
                    )
                isEditing = main.asFlow<MainComponent>(true, Duration.INFINITE)
                    .flatMapLatest { it?.isEditing ?: snapshotFlow { false } }
                    .stateIn(
                        scope = viewModelScope,
                        started = SharingStarted.Eagerly,
                        initialValue = false,
                    )
                needsSaving = main.asFlow<MainComponent>(true, Duration.INFINITE)
                    .flatMapLatest { it?.needsSaving ?: snapshotFlow { false } }
                    .stateIn(
                        scope = viewModelScope,
                        started = SharingStarted.Eagerly,
                        initialValue = false,
                    )
            }
            doOnStart {
                mainNavigation.activate(RootComponent.MainConfig)
            }
            doOnDestroy {
                viewModelScope.cancel()
            }
        }
    }

    override fun openDialog(type: RootComponent.DialogConfig) {
        viewModelScope.launch(dispatcherProvider.main) {
            dialogNavigation.activate(type)
        }
    }

    override fun closeDialog() {
        viewModelScope.launch(dispatcherProvider.main) {
            dialogNavigation.activate(RootComponent.DialogConfig.None)
        }
    }

    override fun openProject(project: ProjectModel) {
        viewModelScope.launch {
            val mainComponent = main.asFlow<MainComponent>().first()
            mainComponent?.openProject(project)
        }
    }

    override fun closeProject() {
        viewModelScope.launch {
            val mainComponent = main.asFlow<MainComponent>().first()
            mainComponent?.closeProject()
        }
    }

    override fun exportTmx(path: String) {
        viewModelScope.launch {
            val mainComponent = main.asFlow<MainComponent>().first()
            mainComponent?.exportTmx(path)
        }
    }

    override fun moveSegmentUp() {
        viewModelScope.launch {
            val mainComponent = main.asFlow<MainComponent>().first()
            mainComponent?.moveSegmentUp()
        }
    }

    override fun moveSegmentDown() {
        viewModelScope.launch {
            val mainComponent = main.asFlow<MainComponent>().first()
            mainComponent?.moveSegmentDown()
        }
    }

    override fun mergeWithPreviousSegment() {
        viewModelScope.launch {
            val mainComponent = main.asFlow<MainComponent>().first()
            mainComponent?.mergeWithPreviousSegment()
        }
    }

    override fun mergeWithNextSegment() {
        viewModelScope.launch {
            val mainComponent = main.asFlow<MainComponent>().first()
            mainComponent?.mergeWithNextSegment()
        }
    }

    override fun createSegmentBefore() {
        viewModelScope.launch {
            val mainComponent = main.asFlow<MainComponent>().first()
            mainComponent?.createSegmentBefore()
        }
    }

    override fun createSegmentAfter() {
        viewModelScope.launch {
            val mainComponent = main.asFlow<MainComponent>().first()
            mainComponent?.createSegmentAfter()
        }
    }

    override fun save() {
        viewModelScope.launch {
            val mainComponent = main.asFlow<MainComponent>().first()
            mainComponent?.save()
        }
    }

    override fun toggleEditing() {
        viewModelScope.launch {
            val mainComponent = main.asFlow<MainComponent>().first()
            mainComponent?.toggleEditing()
        }
    }

    override fun splitSegment() {
        viewModelScope.launch {
            val mainComponent = main.asFlow<MainComponent>().first()
            mainComponent?.splitSegment()
        }
    }

    override fun deleteSegment() {
        viewModelScope.launch {
            val mainComponent = main.asFlow<MainComponent>().first()
            mainComponent?.deleteSegment()
        }
    }
}
