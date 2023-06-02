package root

import align.ui.AlignViewModel
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.slot.ChildSlot
import com.arkivanov.decompose.router.slot.SlotNavigation
import com.arkivanov.decompose.router.slot.activate
import com.arkivanov.decompose.router.slot.childSlot
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.instancekeeper.getOrCreate
import com.arkivanov.essenty.lifecycle.doOnCreate
import com.arkivanov.essenty.lifecycle.doOnDestroy
import common.utils.AppBusiness
import common.utils.getByInjection
import data.ProjectModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import main.ui.MainViewModel
import kotlin.coroutines.CoroutineContext

@OptIn(ExperimentalCoroutinesApi::class)
internal class DefaultRootComponent(
    componentContext: ComponentContext,
    coroutineContext: CoroutineContext,
) : RootComponent, ComponentContext by componentContext {

    private lateinit var viewModelScope: CoroutineScope
    private val dialogNavigation = SlotNavigation<RootComponent.DialogConfig>()
    private val mainViewModel: MainViewModel = AppBusiness.instanceKeeper.getOrCreate { getByInjection() }
    private val alignViewModel: AlignViewModel = AppBusiness.instanceKeeper.getOrCreate { getByInjection() }

    override lateinit var currentProject: StateFlow<ProjectModel?>

    override lateinit var isEditing: StateFlow<Boolean>

    override lateinit var needsSaving: StateFlow<Boolean>

    override val dialog: Value<ChildSlot<RootComponent.DialogConfig, *>> = childSlot(
        source = dialogNavigation,
        key = "DialogSlot",
        childFactory = ::createDialogChild,
    )

    init {
        with(lifecycle) {
            doOnCreate {
                viewModelScope = CoroutineScope(coroutineContext + SupervisorJob())
                currentProject = mainViewModel.uiState.mapLatest { it.project }.stateIn(
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
            doOnDestroy {
                viewModelScope.cancel()
            }
        }
    }

    private fun createDialogChild(config: RootComponent.DialogConfig, context: ComponentContext) {
        return when (config) {
            else -> Unit
        }
    }

    override fun openDialog(type: RootComponent.DialogConfig) {
        dialogNavigation.activate(type)
    }

    override fun closeDialog() {
        dialogNavigation.activate(RootComponent.DialogConfig.None)
    }

    override fun openProject(project: ProjectModel) {
        mainViewModel.openProject(project)
    }

    override fun closeProject() {
        mainViewModel.closeProject()
    }

    override fun exportTmx(path: String) {
        mainViewModel.exportTmx(path)
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
