package projectsettings.ui.dialog

import com.arkivanov.decompose.router.slot.ChildSlot
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.parcelable.Parcelable
import com.arkivanov.essenty.parcelable.Parcelize
import kotlinx.coroutines.flow.StateFlow

interface SettingsComponent {

    val content: Value<ChildSlot<Config, *>>
    val uiState: StateFlow<SettingsUiState>

    fun selectTab(value: SettingsTab)

    sealed interface Config : Parcelable {
        @Parcelize
        object General : Config

        @Parcelize
        object Segmentation : Config
    }
}