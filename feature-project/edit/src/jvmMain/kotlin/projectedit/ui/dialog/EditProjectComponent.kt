package projectedit.ui.dialog

import com.arkivanov.decompose.router.slot.ChildSlot
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.parcelable.Parcelable
import com.arkivanov.essenty.parcelable.Parcelize
import data.ProjectModel
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface EditProjectComponent {

    val content: Value<ChildSlot<Config, *>>
    val uiState: StateFlow<EditProjectUiState>
    val onDone: SharedFlow<ProjectModel?>
    var project: ProjectModel?
    fun selectTab(section: EditProjectSection)

    fun submitMetadata()

    sealed interface Config : Parcelable {
        @Parcelize
        object Metadata : Config

        @Parcelize
        object SegmentationRules : Config
    }
}
