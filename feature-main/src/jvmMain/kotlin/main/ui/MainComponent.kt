package main.ui

import com.arkivanov.decompose.router.slot.ChildSlot
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.parcelable.Parcelable
import com.arkivanov.essenty.parcelable.Parcelize
import data.ProjectModel
import kotlinx.coroutines.flow.StateFlow

interface MainComponent {
    val uiState: StateFlow<MainUiState>
    val isEditing: StateFlow<Boolean>
    val needsSaving: StateFlow<Boolean>
    val main: Value<ChildSlot<MainConfig, *>>

    fun openProject(model: ProjectModel)
    fun closeProject()
    fun openFilePair(index: Int)
    fun selectFilePair(index: Int)
    fun closeFilePair(index: Int)
    fun exportTmx(path: String)

    // proxy to align
    fun moveSegmentUp()
    fun moveSegmentDown()
    fun mergeWithPreviousSegment()
    fun mergeWithNextSegment()
    fun createSegmentBefore()
    fun createSegmentAfter()
    fun deleteSegment()
    fun toggleEditing()
    fun editSegment(id: Int, value: String, position: Int)
    fun splitSegment()
    fun save()

    sealed interface MainConfig : Parcelable {
        @Parcelize
        object Intro : MainConfig

        @Parcelize
        object ChooseFilePair : MainConfig

        @Parcelize
        object Alignment : MainConfig
    }
}
