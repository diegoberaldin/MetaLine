package projectsettings.ui.dialog

import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import data.ProjectModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class SettingsViewModel : InstanceKeeper.Instance {

    private val viewModelScope = CoroutineScope(SupervisorJob())

    private val tabs = MutableStateFlow<List<SettingsTab>>(emptyList())
    private val currentTab = MutableStateFlow<SettingsTab?>(null)

    val uiState = combine(
        tabs,
        currentTab,
    ) { tabs, currentTab ->
        SettingsUiState(
            tabs = tabs,
            currentTab = currentTab,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SettingsUiState(),
    )

    override fun onDestroy() {
        viewModelScope.cancel()
    }

    fun load(project: ProjectModel?) {
        if (project == null) {
            tabs.value = listOf(SettingsTab.GENERAL)
        } else {
            tabs.value = listOf(
                SettingsTab.GENERAL,
                SettingsTab.SEGMENTATION_RULES,
            )
        }
        currentTab.value = SettingsTab.GENERAL
    }

    fun selectTab(value: SettingsTab) {
        currentTab.value = value
    }
}
