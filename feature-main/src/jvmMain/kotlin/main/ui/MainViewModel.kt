package main.ui

import L10n
import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import common.keystore.TemporaryKeyStore
import common.notification.NotificationCenter
import common.notification.NotificationCenter.Event.CurrentProjectEdited
import common.notification.NotificationCenter.Event.OpenProject
import data.FilePairModel
import data.ProjectModel
import data.SegmentModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.coroutines.launch
import repository.FilePairRepository
import repository.ProjectRepository
import repository.SegmentRepository
import usecase.ExportTmxUseCase
import java.io.File

class MainViewModel(
    private val dispatcherProvider: common.coroutines.CoroutineDispatcherProvider,
    private val keyStore: TemporaryKeyStore,
    private val projectRepository: ProjectRepository,
    private val filePairRepository: FilePairRepository,
    private val segmentRepository: SegmentRepository,
    private val exportTmxUseCase: ExportTmxUseCase,
    private val notificationCenter: NotificationCenter,
) : InstanceKeeper.Instance {

    private val viewModelScope = CoroutineScope(SupervisorJob())
    private val project = MutableStateFlow<ProjectModel?>(null)
    private val filePairs = MutableStateFlow<List<FilePairModel>>(emptyList())
    private val openFilePairs = MutableStateFlow<List<FilePairModel>>(emptyList())
    private val currentFilePairIndex = MutableStateFlow<Int?>(null)

    val uiState = combine(
        project,
        filePairs,
        openFilePairs,
        currentFilePairIndex,
    ) { project, filePairs, openFilePairs, currentFilePairIndex ->
        MainUiState(
            project = project,
            filePairs = filePairs,
            openFilePairs = openFilePairs,
            currentFilePairIndex = currentFilePairIndex,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = MainUiState(),
    )

    init {
        openLastProject()

        viewModelScope.launch(dispatcherProvider.io) {
            launch {
                notificationCenter.events.filter { it is OpenProject }.collect {
                    val id = (it as OpenProject).projectId
                    openProject(id)
                }
            }
            launch {
                notificationCenter.events.filter { it is CurrentProjectEdited }.collect {
                    // refresh project
                    openLastProject()
                }
            }
        }
    }

    override fun onDestroy() {
        viewModelScope.cancel()
    }

    private fun openLastProject() {
        viewModelScope.launch(dispatcherProvider.io) {
            val id = keyStore.get("lastOpenedProject", 0)
            if (id > 0) {
                openProject(id)
            }
        }
    }

    fun openProject(model: ProjectModel) {
        viewModelScope.launch(dispatcherProvider.io) {
            openProject(model.id)
        }
    }

    fun closeProject() {
        viewModelScope.launch(dispatcherProvider.io) {
            keyStore.save("lastOpenedProject", 0)
        }
        project.value = null
        filePairs.value = emptyList()
        openFilePairs.value = emptyList()
        currentFilePairIndex.value = null
    }

    private suspend fun openProject(id: Int) {
        keyStore.save("lastOpenedProject", id)
        project.value = projectRepository.getById(id)
        filePairs.value = filePairRepository.getAll(id)
        openFilePairs.value = emptyList()
        currentFilePairIndex.value = null
    }

    fun openFilePair(index: Int) {
        val filePair = filePairs.value[index]
        var newIndex = 0
        openFilePairs.getAndUpdate { oldList ->
            newIndex = oldList.indexOf(filePair)
            if (newIndex < 0) {
                val newList = oldList + filePair
                newIndex = newList.size - 1
                newList
            } else {
                oldList
            }
        }
        currentFilePairIndex.value = newIndex
    }

    fun selectFilePair(index: Int) {
        currentFilePairIndex.value = index.takeIf { it >= 0 }
    }

    fun closeFilePair(index: Int) {
        openFilePairs.updateAndGet {
            val newList = it.filterIndexed { i, _ -> i != index }
            if (newList.isEmpty()) {
                currentFilePairIndex.value = null
            } else if (currentFilePairIndex.value == index) {
                currentFilePairIndex.value = (index - 1).coerceAtLeast(0)
            }
            newList
        }
    }

    fun exportTmx(path: String) {
        val project = project.value ?: return

        viewModelScope.launch(dispatcherProvider.io) {
            val sourceLang = project.sourceLang
            val targetLang = project.targetLang

            val pairs = filePairRepository.getAll(project.id)
            val sourceSegments = mutableListOf<SegmentModel>()
            val targetSegments = mutableListOf<SegmentModel>()
            for (pair in pairs) {
                val sourcePairSegments = segmentRepository.getAll(pairId = pair.id, lang = sourceLang)
                val targetPairSegments = segmentRepository.getAll(pair.id, lang = targetLang)
                val segmentCount = minOf(sourcePairSegments.size, targetPairSegments.size)
                sourceSegments += sourcePairSegments.subList(
                    fromIndex = 0,
                    toIndex = segmentCount,
                )
                targetSegments += targetPairSegments.subList(
                    fromIndex = 0,
                    toIndex = segmentCount,
                )
            }
            val cleanSegmentLists = sourceSegments.zip(targetSegments)
                .mapNotNull {
                    if (it.first.text.isEmpty() || it.second.text.isEmpty()) {
                        null
                    } else {
                        it
                    }
                }.unzip()

            val input = ExportTmxUseCase.Input(
                sourceLang = sourceLang,
                targetLang = targetLang,
                sourceSegments = cleanSegmentLists.first,
                targetSegments = cleanSegmentLists.second,
            )
            exportTmxUseCase(input = input, destination = File(path))
        }
    }
}
