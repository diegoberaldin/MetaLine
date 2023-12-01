package projectmetadata.ui

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.doOnCreate
import com.arkivanov.essenty.lifecycle.doOnDestroy
import common.coroutines.CoroutineDispatcherProvider
import common.notification.NotificationCenter
import data.FilePairModel
import data.LanguageModel
import data.ProjectModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import localized
import repository.FilePairRepository
import repository.LanguageRepository
import repository.ProjectRepository
import repository.SegmentRepository
import usecase.GetCompleteLanguageUseCase
import usecase.SegmentTxtFileUseCase
import kotlin.coroutines.CoroutineContext

internal class DefaultProjectMetadataComponent(
    componentContext: ComponentContext,
    coroutineContext: CoroutineContext,
    private val dispatcherProvider: CoroutineDispatcherProvider,
    private val languageRepository: LanguageRepository,
    private val segmentUseCase: SegmentTxtFileUseCase,
    private val projectRepository: ProjectRepository,
    private val filePairRepository: FilePairRepository,
    private val segmentRepository: SegmentRepository,
    private val completeLanguage: GetCompleteLanguageUseCase,
    private val notificationCenter: NotificationCenter,
) : ProjectMetadataComponent, ComponentContext by componentContext {

    private val name = MutableStateFlow("")
    private val sourceLanguage = MutableStateFlow<LanguageModel?>(null)
    private val targetLanguage = MutableStateFlow<LanguageModel?>(null)
    private val availableSourceLanguages = MutableStateFlow<List<LanguageModel>>(emptyList())
    private val availableTargetLanguages = MutableStateFlow<List<LanguageModel>>(emptyList())
    private val sourceFiles = MutableStateFlow<List<String>>(emptyList())
    private val targetFiles = MutableStateFlow<List<String>>(emptyList())
    private val selectedSource = MutableStateFlow<Int?>(null)
    private val selectedTarget = MutableStateFlow<Int?>(null)
    private val nameError = MutableStateFlow("")
    private val languagesError = MutableStateFlow("")
    private val filesError = MutableStateFlow("")
    private lateinit var viewModelScope: CoroutineScope
    private var projectId = 0

    private val defaultLanguages: List<LanguageModel> = languageRepository.getDefaultLanguages().map {
        completeLanguage(it)
    }

    override val onDone = MutableSharedFlow<ProjectModel>()
    override lateinit var uiState: StateFlow<ProjectMetadataUiState>
    override lateinit var languagesUiState: StateFlow<ProjectMetadataLanguagesUiState>
    override lateinit var fileUiState: StateFlow<ProjectMetadataFileUiState>
    override lateinit var errorUiState: StateFlow<CreateProjectErrorState>

    init {
        with(lifecycle) {
            doOnCreate {
                viewModelScope = CoroutineScope(coroutineContext + SupervisorJob())
                uiState = name.map { name ->
                    ProjectMetadataUiState(
                        name = name,
                    )
                }.stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5_000),
                    initialValue = ProjectMetadataUiState(),
                )
                languagesUiState = combine(
                    sourceLanguage,
                    targetLanguage,
                    availableSourceLanguages,
                    availableTargetLanguages,
                ) { sourceLanguage, targetLanguage, availableSourceLanguages, availableTargetLanguages ->
                    ProjectMetadataLanguagesUiState(
                        sourceLanguage = sourceLanguage,
                        targetLanguage = targetLanguage,
                        availableSourceLanguages = availableSourceLanguages,
                        availableTargetLanguages = availableTargetLanguages,
                    )
                }.stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5_000),
                    initialValue = ProjectMetadataLanguagesUiState(),
                )
                fileUiState = combine(
                    sourceFiles,
                    targetFiles,
                    selectedSource,
                    selectedTarget,
                ) { sourceFiles, targetFiles, selectedSource, selectedTarget ->
                    ProjectMetadataFileUiState(
                        sourceFiles = sourceFiles,
                        targetFiles = targetFiles,
                        selectedSource = selectedSource,
                        selectedTarget = selectedTarget,
                    )
                }.stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5_000),
                    initialValue = ProjectMetadataFileUiState(),
                )
                errorUiState = combine(
                    nameError,
                    languagesError,
                    filesError,
                ) { nameError, languagesError, filesError ->
                    CreateProjectErrorState(
                        nameError = nameError,
                        languagesError = languagesError,
                        filesError = filesError,
                    )
                }.stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5_000),
                    initialValue = CreateProjectErrorState(),
                )

                refreshAvailableLanguages()
            }
            doOnDestroy {
                viewModelScope.cancel()
            }
        }
    }

    override fun load(project: ProjectModel?) {
        name.value = project?.name ?: ""
        sourceLanguage.value = defaultLanguages.find { it.code == project?.sourceLang }
        targetLanguage.value = defaultLanguages.find { it.code == project?.targetLang }
        projectId = project?.id ?: 0
        viewModelScope.launch(dispatcherProvider.io) {
            val filePairs = filePairRepository.getAll(projectId = projectId)
            sourceFiles.value = filePairs.map { it.sourcePath }
            targetFiles.value = filePairs.map { it.targetPath }
            refreshAvailableLanguages()
        }
    }

    private fun refreshAvailableLanguages() {
        availableSourceLanguages.value = buildList {
            add(LanguageModel(name = "select_placeholder".localized()))
            addAll(defaultLanguages.filter { it.code != targetLanguage.value?.code })
        }
        availableTargetLanguages.value = buildList {
            add(LanguageModel(name = "select_placeholder".localized()))
            addAll(defaultLanguages.filter { it.code != sourceLanguage.value?.code })
        }
    }

    override fun setName(value: String) {
        name.value = value
    }

    override fun setSourceLanguage(value: LanguageModel) {
        if (value.code.isEmpty()) {
            sourceLanguage.value = null
        } else {
            sourceLanguage.value = value
        }
        refreshAvailableLanguages()
    }

    override fun setTargetLanguage(value: LanguageModel) {
        if (value.code.isEmpty()) {
            targetLanguage.value = null
        } else {
            targetLanguage.value = value
        }
        refreshAvailableLanguages()
    }

    override fun selectSourceFile(value: Int) {
        selectedSource.getAndUpdate { oldIdx ->
            if (oldIdx == value) {
                null
            } else {
                value
            }
        }
    }

    override fun selectTargetFile(value: Int) {
        selectedTarget.getAndUpdate { oldIdx ->
            if (oldIdx == value) {
                null
            } else {
                value
            }
        }
    }

    override fun addSourceFile(path: String) {
        sourceFiles.getAndUpdate { oldList ->
            if (oldList.contains(path)) {
                oldList
            } else {
                oldList.toMutableList() + path
            }
        }
    }

    override fun addTargetFile(path: String) {
        targetFiles.getAndUpdate { oldList ->
            if (oldList.contains(path)) {
                oldList
            } else {
                oldList.toMutableList() + path
            }
        }
    }

    override fun moveSourceUp() {
        val index = selectedSource.value?.takeIf { it > 0 } ?: return
        sourceFiles.getAndUpdate { oldList ->
            val newList = oldList.toMutableList()
            newList.add(index - 1, newList.removeAt(index))
            selectedSource.getAndUpdate { idx ->
                if (idx != null) {
                    idx - 1
                } else {
                    null
                }
            }
            newList
        }
    }

    override fun moveSourceDown() {
        val index = selectedSource.value?.takeIf { it < sourceFiles.value.size - 1 } ?: return
        sourceFiles.getAndUpdate { oldList ->
            val newList = oldList.toMutableList()
            newList.add(index + 1, newList.removeAt(index))
            selectedSource.getAndUpdate { idx ->
                if (idx != null) {
                    idx + 1
                } else {
                    null
                }
            }
            newList
        }
    }

    override fun moveTargetUp() {
        val index = selectedTarget.value?.takeIf { it > 0 } ?: return
        targetFiles.getAndUpdate { oldList ->
            val newList = oldList.toMutableList()
            newList.add(index - 1, newList.removeAt(index))
            selectedTarget.getAndUpdate { idx ->
                if (idx != null) {
                    idx - 1
                } else {
                    null
                }
            }
            newList
        }
    }

    override fun moveTargetDown() {
        val index = selectedTarget.value?.takeIf { it < targetFiles.value.size - 1 } ?: return
        targetFiles.getAndUpdate { oldList ->
            val newList = oldList.toMutableList()
            newList.add(index + 1, newList.removeAt(index))
            selectedTarget.getAndUpdate { idx ->
                if (idx != null) {
                    idx + 1
                } else {
                    null
                }
            }
            newList
        }
    }

    override fun deleteSourceFile() {
        val index = selectedSource.value ?: return
        selectedSource.value = null
        sourceFiles.getAndUpdate { oldList ->
            oldList.toMutableList().apply {
                removeAt(index)
            }
        }
    }

    override fun deleteTargetFile() {
        val index = selectedTarget.value ?: return
        selectedTarget.value = null
        targetFiles.getAndUpdate { oldList ->
            oldList.toMutableList().apply {
                removeAt(index)
            }
        }
    }

    override fun submit() {
        val name = name.value
        val sourceLang = sourceLanguage.value?.code.orEmpty()
        val targetLang = targetLanguage.value?.code.orEmpty()
        val sourceFiles = sourceFiles.value
        val targetFiles = targetFiles.value
        var valid = true

        nameError.value = ""
        languagesError.value = ""
        filesError.value = ""

        if (name.isEmpty()) {
            nameError.value = "message_error_missing_name".localized()
            valid = false
        }
        if (sourceLang.isEmpty()) {
            languagesError.value = "message_error_missing_source_language".localized()
            valid = false
        } else if (targetLang.isEmpty()) {
            languagesError.value = "message_error_missing_target_language".localized()
            valid = false
        }
        if (sourceFiles.isEmpty() && targetFiles.isEmpty()) {
            filesError.value = "message_error_missing_files".localized()
            valid = false
        } else if (sourceFiles.size != targetFiles.size) {
            filesError.value = "message_error_not_matching_file_number".localized()
            valid = false
        }
        if (!valid) {
            return
        }

        viewModelScope.launch(dispatcherProvider.io) {
            val project = ProjectModel(
                id = projectId,
                name = name,
                sourceLang = sourceLang,
                targetLang = targetLang,
            )
            if (projectId == 0) {
                projectId = projectRepository.create(project)
            } else {
                projectRepository.update(project)
            }

            val filePairs = buildList {
                for (i in sourceFiles.indices) {
                    val filePair = FilePairModel(
                        sourcePath = sourceFiles[i],
                        targetPath = targetFiles[i],
                    )
                    this += filePair
                }
            }

            // removes old non-existing pairs
            val oldPairs = filePairRepository.getAll(projectId)
            for (pair in oldPairs) {
                if (filePairs.none { it.sourcePath == pair.sourcePath && it.targetPath == pair.targetPath }) {
                    filePairRepository.delete(pair)
                }
            }

            // creates new pairs
            for (pair in filePairs) {
                val existing = filePairRepository.find(
                    projectId = projectId,
                    sourcePath = pair.sourcePath,
                    targetPath = pair.targetPath,
                )
                if (existing == null) {
                    val pairId = filePairRepository.create(model = pair, projectId = projectId)
                    val sourceSegments = segmentUseCase(
                        path = pair.sourcePath,
                        lang = sourceLang,
                        projectId = projectId,
                    ).map { it.copy(lang = sourceLang) }
                    val targetSegments = segmentUseCase(
                        path = pair.targetPath,
                        lang = targetLang,
                        projectId = projectId,
                    ).map { it.copy(lang = targetLang) }
                    segmentRepository.createAll(models = sourceSegments, pairId = pairId)
                    segmentRepository.createAll(models = targetSegments, pairId = pairId)
                }
            }

            if (oldPairs.isNotEmpty()) {
                // existing project, current
                notificationCenter.send(NotificationCenter.Event.CurrentProjectEdited)
            }
            onDone.emit(project.copy(id = projectId))
        }
    }
}
