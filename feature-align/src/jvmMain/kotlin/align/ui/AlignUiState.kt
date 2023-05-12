package align.ui

import data.SegmentModel

data class AlignUiState(
    val sourceSegments: List<SegmentModel> = emptyList(),
    val selectedSourceId: Int? = null,
    val targetSegments: List<SegmentModel> = emptyList(),
    val selectedTargetId: Int? = null,
)

data class AlignEditUiState(
    val isEditing: Boolean = false,
    val needsSaving: Boolean = false,
)
