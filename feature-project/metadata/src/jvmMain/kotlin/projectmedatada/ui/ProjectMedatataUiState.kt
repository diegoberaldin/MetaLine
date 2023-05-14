package projectmedatada.ui

import data.LanguageModel

class ProjectMetadataUiState(
    val name: String = "",
)

data class ProjectMetadataLanguagesUiState(
    val sourceLanguage: LanguageModel? = null,
    val targetLanguage: LanguageModel? = null,
    val availableSourceLanguages: List<LanguageModel> = emptyList(),
    val availableTargetLanguages: List<LanguageModel> = emptyList(),
)

data class ProjectMetadataFileUiState(
    val sourceFiles: List<String> = emptyList(),
    val targetFiles: List<String> = emptyList(),
    val selectedSource: Int? = null,
    val selectedTarget: Int? = null,
)

data class CreateProjectErrorState(
    val nameError: String = "",
    val languagesError: String = "",
    val filesError: String = "",
)
