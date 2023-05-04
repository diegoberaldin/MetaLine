package align.ui

import data.SegmentModel

data class AlignUiState(
    val sourceSegments: List<SegmentModel> = emptyList(),
    val selectedSourceIndex: Int? = null,
    val targetSegments: List<SegmentModel> = emptyList(),
    val selectedTargetIndex: Int? = null,
)

data class AlignEditUiState(
    val isEditing: Boolean = false,
    val needsSaving: Boolean = false,
)
