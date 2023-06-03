package projectsettings.ui.dialog

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.slot.ChildSlot
import com.arkivanov.decompose.router.slot.SlotNavigation
import com.arkivanov.decompose.router.slot.activate
import com.arkivanov.decompose.router.slot.childSlot
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.lifecycle.doOnCreate
import com.arkivanov.essenty.lifecycle.doOnDestroy
import common.coroutines.CoroutineDispatcherProvider
import common.utils.getByInjection
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import projectsettings.ui.general.SettingsGeneralComponent
import projectsettings.ui.segmentation.SettingsSegmentationComponent
import kotlin.coroutines.CoroutineContext

internal class DefaultSettingsComponent(
    componentContext: ComponentContext,
    coroutineContext: CoroutineContext,
    private val dispatcherProvider: CoroutineDispatcherProvider,
) : SettingsComponent, ComponentContext by componentContext {

    private val contentNavigation = SlotNavigation<SettingsComponent.Config>()
    private lateinit var viewModelScope: CoroutineScope
    private val tabs = MutableStateFlow<List<SettingsTab>>(emptyList())
    private val currentTab = MutableStateFlow<SettingsTab?>(null)

    override lateinit var uiState: StateFlow<SettingsUiState>
    override val content: Value<ChildSlot<SettingsComponent.Config, *>> = childSlot(
        source = contentNavigation,
        key = "SettingsContentSlot",
        childFactory = { config, context ->
            when (config) {
                SettingsComponent.Config.General -> getByInjection<SettingsGeneralComponent>(
                    context,
                    coroutineContext,
                )

                SettingsComponent.Config.Segmentation -> getByInjection<SettingsSegmentationComponent>(
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

                tabs.value = listOf(
                    SettingsTab.GENERAL,
                    SettingsTab.SEGMENTATION_RULES,
                )

                uiState = combine(
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

                selectTab(SettingsTab.GENERAL)
            }
            doOnDestroy {
                viewModelScope.cancel()
            }
        }
    }

    override fun selectTab(value: SettingsTab) {
        currentTab.value = value
        when (value) {
            SettingsTab.GENERAL -> contentNavigation.activate(SettingsComponent.Config.General)
            SettingsTab.SEGMENTATION_RULES -> contentNavigation.activate(SettingsComponent.Config.Segmentation)
        }
    }
}
