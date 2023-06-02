package projectcreate.ui.dialog

import com.arkivanov.decompose.router.slot.ChildSlot
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.parcelable.Parcelable
import com.arkivanov.essenty.parcelable.Parcelize
import data.ProjectModel
import kotlinx.coroutines.flow.StateFlow

interface CreateProjectComponent {

    val content: Value<ChildSlot<Config, *>>
    val uiState: StateFlow<CreateProjectUiState>

    fun setProject(value: ProjectModel?)
    fun next()
    fun submitMetadata()

    sealed interface Config : Parcelable {
        @Parcelize
        object Metadata : Config

        @Parcelize
        object SegmentationRules : Config
    }
}
