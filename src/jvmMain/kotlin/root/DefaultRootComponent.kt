package root

import align.ui.AlignViewModel
import androidx.compose.runtime.snapshotFlow
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.slot.ChildSlot
import com.arkivanov.decompose.router.slot.SlotNavigation
import com.arkivanov.decompose.router.slot.activate
import com.arkivanov.decompose.router.slot.childSlot
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.instancekeeper.getOrCreate
import com.arkivanov.essenty.lifecycle.doOnCreate
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.arkivanov.essenty.lifecycle.doOnStart
import common.utils.AppBusiness
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
import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration

@OptIn(ExperimentalCoroutinesApi::class)
internal class DefaultRootComponent(
    componentContext: ComponentContext,
    coroutineContext: CoroutineContext,
) : RootComponent, ComponentContext by componentContext {

    private lateinit var viewModelScope: CoroutineScope
    private val dialogNavigation = SlotNavigation<RootComponent.DialogConfig>()
    private val mainNavigation = SlotNavigation<RootComponent.MainConfig>()
    private val alignViewModel: AlignViewModel = AppBusiness.instanceKeeper.getOrCreate { getByInjection() }

    override lateinit var currentProject: StateFlow<ProjectModel?>

    override lateinit var isEditing: StateFlow<Boolean>

    override lateinit var needsSaving: StateFlow<Boolean>

    override val dialog: Value<ChildSlot<RootComponent.DialogConfig, *>> = childSlot(
        source = dialogNavigation,
        key = "DialogSlot",
        childFactory = { _, _ -> },
    )
    override val main: Value<ChildSlot<RootComponent.MainConfig, MainComponent>> = childSlot(
        source = mainNavigation,
        key = "MainSlot",
        childFactory = { _, _ -> getByInjection(componentContext, coroutineContext) },
    )

    init {
        with(lifecycle) {
            doOnCreate {
                viewModelScope = CoroutineScope(coroutineContext + SupervisorJob())
                currentProject = main.asFlow<MainComponent>(true, Duration.INFINITE)
                    .flatMapLatest { it?.uiState ?: snapshotFlow { null } }
                    .mapLatest { it?.project }.stateIn(
                        scope = viewModelScope,
                        started = SharingStarted.Eagerly,
                        initialValue = null,
                    )
                isEditing = alignViewModel.editUiState.mapLatest { it.isEditing }.stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.Eagerly,
                    initialValue = false,
                )
                needsSaving = alignViewModel.editUiState.mapLatest { it.needsSaving }.stateIn(
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
        dialogNavigation.activate(type)
    }

    override fun closeDialog() {
        dialogNavigation.activate(RootComponent.DialogConfig.None)
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
        alignViewModel.moveSegmentUp()
    }

    override fun moveSegmentDown() {
        alignViewModel.moveSegmentDown()
    }

    override fun mergeWithPreviousSegment() {
        alignViewModel.mergeWithPreviousSegment()
    }

    override fun mergeWithNextSegment() {
        alignViewModel.mergeWithNextSegment()
    }

    override fun createSegmentBefore() {
        alignViewModel.createSegmentBefore()
    }

    override fun createSegmentAfter() {
        alignViewModel.createSegmentAfter()
    }

    override fun save() {
        alignViewModel.save()
    }

    override fun toggleEditing() {
        alignViewModel.toggleEditing()
    }

    override fun splitSegment() {
        alignViewModel.splitSegment()
    }

    override fun deleteSegment() {
        alignViewModel.deleteSegment()
    }
}
