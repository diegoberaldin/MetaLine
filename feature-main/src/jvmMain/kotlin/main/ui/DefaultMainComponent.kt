package main.ui

import align.ui.AlignComponent
import androidx.compose.runtime.snapshotFlow
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.slot.ChildSlot
import com.arkivanov.decompose.router.slot.SlotNavigation
import com.arkivanov.decompose.router.slot.activate
import com.arkivanov.decompose.router.slot.childSlot
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.lifecycle.doOnCreate
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.arkivanov.essenty.lifecycle.doOnStart
import common.coroutines.CoroutineDispatcherProvider
import common.keystore.TemporaryKeyStore
import common.notification.NotificationCenter
import common.utils.asFlow
import common.utils.getByInjection
import data.FilePairModel
import data.ProjectModel
import data.SegmentModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.coroutines.launch
import mainintro.ui.IntroComponent
import repository.FilePairRepository
import repository.ProjectRepository
import repository.SegmentRepository
import usecase.ExportTmxUseCase
import java.io.File
import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration

@OptIn(ExperimentalCoroutinesApi::class)
internal class DefaultMainComponent(
    componentContext: ComponentContext,
    coroutineContext: CoroutineContext,
    private val dispatcherProvider: CoroutineDispatcherProvider,
    private val keyStore: TemporaryKeyStore,
    private val projectRepository: ProjectRepository,
    private val filePairRepository: FilePairRepository,
    private val segmentRepository: SegmentRepository,
    private val exportTmxUseCase: ExportTmxUseCase,
    private val notificationCenter: NotificationCenter,
) : MainComponent, ComponentContext by componentContext {

    private lateinit var viewModelScope: CoroutineScope
    private val project = MutableStateFlow<ProjectModel?>(null)
    private val filePairs = MutableStateFlow<List<FilePairModel>>(emptyList())
    private val openFilePairs = MutableStateFlow<List<FilePairModel>>(emptyList())
    private val currentFilePairIndex = MutableStateFlow<Int?>(null)
    private val mainNavigation = SlotNavigation<MainComponent.MainConfig>()

    override val main: Value<ChildSlot<MainComponent.MainConfig, *>> = childSlot(
        source = mainNavigation,
        key = "MainMainSlot",
        childFactory = { config, context ->
            when (config) {
                MainComponent.MainConfig.Intro -> getByInjection<IntroComponent>(context, coroutineContext)
                MainComponent.MainConfig.Alignment -> getByInjection<AlignComponent>(context, coroutineContext)
                else -> Unit
            }
        },
    )
    override lateinit var uiState: StateFlow<MainUiState>
    override lateinit var isEditing: StateFlow<Boolean>
    override lateinit var needsSaving: StateFlow<Boolean>

    init {
        with(lifecycle) {
            doOnCreate {
                viewModelScope = CoroutineScope(coroutineContext + SupervisorJob())
                uiState = combine(
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
                isEditing = main.asFlow<AlignComponent>(true, Duration.INFINITE)
                    .flatMapLatest { it?.editUiState ?: snapshotFlow { null } }.map {
                        it?.isEditing == true
                    }.stateIn(
                        scope = viewModelScope,
                        started = SharingStarted.WhileSubscribed(5_000),
                        initialValue = false,
                    )
                needsSaving = main.asFlow<AlignComponent>(true, Duration.INFINITE)
                    .flatMapLatest { it?.editUiState ?: snapshotFlow { null } }.map {
                        it?.needsSaving == true
                    }.stateIn(
                        scope = viewModelScope,
                        started = SharingStarted.WhileSubscribed(5_000),
                        initialValue = false,
                    )
                mainNavigation.activate(MainComponent.MainConfig.Intro)
            }
            doOnStart {
                openLastProject()

                viewModelScope.launch(dispatcherProvider.io) {
                    launch {
                        notificationCenter.events.filter { it is NotificationCenter.Event.OpenProject }.collect {
                            val id = (it as NotificationCenter.Event.OpenProject).projectId
                            openProject(id)
                        }
                    }
                    launch {
                        notificationCenter.events.filter { it is NotificationCenter.Event.CurrentProjectEdited }
                            .collect {
                                // refresh project
                                openLastProject()
                            }
                    }
                }
            }
            doOnDestroy {
                viewModelScope.cancel()
            }
        }
    }

    private fun openLastProject() {
        viewModelScope.launch(dispatcherProvider.io) {
            val id = keyStore.get("lastOpenedProject", 0)
            if (id > 0) {
                openProject(id)
            }
        }
    }

    override fun openProject(model: ProjectModel) {
        viewModelScope.launch(dispatcherProvider.io) {
            openProject(model.id)
        }
    }

    override fun closeProject() {
        viewModelScope.launch(dispatcherProvider.io) {
            keyStore.save("lastOpenedProject", 0)
        }
        project.value = null
        filePairs.value = emptyList()
        openFilePairs.value = emptyList()
        currentFilePairIndex.value = null
        viewModelScope.launch(dispatcherProvider.main) {
            mainNavigation.activate(MainComponent.MainConfig.Intro)
        }
    }

    private suspend fun openProject(id: Int) {
        keyStore.save("lastOpenedProject", id)
        project.value = projectRepository.getById(id)
        filePairs.value = filePairRepository.getAll(id)
        openFilePairs.value = emptyList()
        currentFilePairIndex.value = null
        viewModelScope.launch(dispatcherProvider.main) {
            mainNavigation.activate(MainComponent.MainConfig.ChooseFilePair)
        }
    }

    override fun openFilePair(index: Int) {
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
        viewModelScope.launch(dispatcherProvider.main) {
            mainNavigation.activate(MainComponent.MainConfig.Alignment)
        }
    }

    override fun selectFilePair(index: Int) {
        currentFilePairIndex.value = index.takeIf { it >= 0 }
    }

    override fun closeFilePair(index: Int) {
        openFilePairs.updateAndGet {
            val newList = it.filterIndexed { i, _ -> i != index }
            if (newList.isEmpty()) {
                currentFilePairIndex.value = null
            } else if (currentFilePairIndex.value == index) {
                currentFilePairIndex.value = (index - 1).coerceAtLeast(0)
            }
            newList
        }
        if (openFilePairs.value.isEmpty()) {
            mainNavigation.activate(MainComponent.MainConfig.ChooseFilePair)
        }
    }

    override fun exportTmx(path: String) {
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

    override fun moveSegmentUp() {
        viewModelScope.launch {
            main.asFlow<AlignComponent>().first()?.moveSegmentUp()
        }
    }

    override fun moveSegmentDown() {
        viewModelScope.launch {
            main.asFlow<AlignComponent>().first()?.moveSegmentDown()
        }
    }

    override fun mergeWithPreviousSegment() {
        viewModelScope.launch {
            main.asFlow<AlignComponent>().first()?.mergeWithPreviousSegment()
        }
    }

    override fun mergeWithNextSegment() {
        viewModelScope.launch {
            main.asFlow<AlignComponent>().first()?.mergeWithNextSegment()
        }
    }

    override fun createSegmentBefore() {
        viewModelScope.launch {
            main.asFlow<AlignComponent>().first()?.createSegmentBefore()
        }
    }

    override fun createSegmentAfter() {
        viewModelScope.launch {
            main.asFlow<AlignComponent>().first()?.createSegmentAfter()
        }
    }

    override fun deleteSegment() {
        viewModelScope.launch {
            main.asFlow<AlignComponent>().first()?.deleteSegment()
        }
    }

    override fun toggleEditing() {
        viewModelScope.launch {
            main.asFlow<AlignComponent>().first()?.toggleEditing()
        }
    }

    override fun editSegment(id: Int, value: String, position: Int) {
        viewModelScope.launch {
            main.asFlow<AlignComponent>().first()?.editSegment(id = id, value = value, position = position)
        }
    }

    override fun splitSegment() {
        viewModelScope.launch {
            main.asFlow<AlignComponent>().first()?.splitSegment()
        }
    }

    override fun save() {
        viewModelScope.launch {
            main.asFlow<AlignComponent>().first()?.save()
        }
    }
}
