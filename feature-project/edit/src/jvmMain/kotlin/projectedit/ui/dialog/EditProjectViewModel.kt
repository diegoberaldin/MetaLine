package projectedit.ui.dialog

import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class EditProjectViewModel() : InstanceKeeper.Instance {

    private val tabs = MutableStateFlow(listOf(EditProjectSection.METADATA, EditProjectSection.SEGMENTATION_RULES))
    private val currentTab = MutableStateFlow(EditProjectSection.METADATA)
    private val viewModelScope = CoroutineScope(SupervisorJob())

    val uiState = combine(
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

    override fun onDestroy() {
        viewModelScope.cancel()
    }

    fun selectTab(section: EditProjectSection) {
        currentTab.value = section
    }
}
