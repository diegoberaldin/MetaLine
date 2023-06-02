package align.ui

import data.FilePairModel
import data.ProjectModel
import kotlinx.coroutines.flow.StateFlow

interface AlignComponent {
    val uiState: StateFlow<AlignUiState>
    val editUiState: StateFlow<AlignEditUiState>

    fun load(pair: FilePairModel?, project: ProjectModel)
    fun selectSourceSegment(id: Int)
    fun selectTargetSegment(id: Int)
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
}