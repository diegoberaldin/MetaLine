package projectcreate.ui.dialog

import data.LanguageModel

class CreateProjectUiState(
    val name: String = "",
)

data class CreateProjectLanguagesUiState(
    val sourceLanguage: LanguageModel? = null,
    val targetLanguage: LanguageModel? = null,
    val availableSourceLanguages: List<LanguageModel> = emptyList(),
    val availableTargetLanguages: List<LanguageModel> = emptyList(),
)

data class CreateProjectFileUiState(
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
