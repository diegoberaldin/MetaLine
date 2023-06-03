package root

import com.arkivanov.decompose.router.slot.ChildSlot
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.parcelable.Parcelable
import com.arkivanov.essenty.parcelable.Parcelize
import data.ProjectModel
import kotlinx.coroutines.flow.StateFlow

interface RootComponent {

    val currentProject: StateFlow<ProjectModel?>
    val isEditing: StateFlow<Boolean>
    val needsSaving: StateFlow<Boolean>
    val dialog: Value<ChildSlot<DialogConfig, *>>
    val main: Value<ChildSlot<MainConfig, *>>

    fun openDialog(type: DialogConfig)
    fun closeDialog()
    fun openProject(project: ProjectModel)
    fun closeProject()
    fun exportTmx(path: String)
    fun moveSegmentUp()
    fun moveSegmentDown()
    fun mergeWithPreviousSegment()
    fun mergeWithNextSegment()
    fun createSegmentBefore()
    fun createSegmentAfter()
    fun save()
    fun toggleEditing()
    fun splitSegment()
    fun deleteSegment()

    @Parcelize
    object MainConfig : Parcelable

    sealed interface DialogConfig : Parcelable {
        @Parcelize
        object None : DialogConfig

        @Parcelize
        object NewProject : DialogConfig

        @Parcelize
        object EditProject : DialogConfig

        @Parcelize
        object Statistics : DialogConfig

        @Parcelize
        object Settings : DialogConfig

        @Parcelize
        object Export : DialogConfig
    }
}
