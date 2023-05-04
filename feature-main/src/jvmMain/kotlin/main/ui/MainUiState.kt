package main.ui

import data.FilePairModel
import data.ProjectModel

data class MainUiState(
    val project: ProjectModel? = null,
    val filePairs: List<FilePairModel> = emptyList(),
    val openFilePairs: List<FilePairModel> = emptyList(),
    val currentFilePairIndex: Int? = null,
)
