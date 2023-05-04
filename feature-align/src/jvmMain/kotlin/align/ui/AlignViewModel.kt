package align.ui

import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import common.log.LogManager
import data.FilePairModel
import data.ProjectModel
import data.SegmentModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import repository.SegmentRepository

class AlignViewModel(
    private val dispatcherProvider: common.coroutines.CoroutineDispatcherProvider,
    private val logManager: LogManager,
    private val segmentRepository: SegmentRepository,
) : InstanceKeeper.Instance {

    private val viewModelScope = CoroutineScope(SupervisorJob())
    private val sourceSegments = MutableStateFlow<List<SegmentModel>>(emptyList())
    private val targetSegments = MutableStateFlow<List<SegmentModel>>(emptyList())
    private val selectedSourceId = MutableStateFlow<Int?>(null)
    private val selectedTargetId = MutableStateFlow<Int?>(null)
    private val isEditing = MutableStateFlow(false)
    private val needsSaving = MutableStateFlow(false)
    private var lastCursorPosition = -1
    private var pairId = 0
    private var sourceLang = ""
    private var targetLang = ""

    val uiState = combine(
        sourceSegments,
        targetSegments,
        selectedSourceId,
        selectedTargetId,
    ) { sourceSegments, targetSegments, selectedSourceIndex, selectedTargetIndex ->
        AlignUiState(
            sourceSegments = sourceSegments,
            targetSegments = targetSegments,
            selectedSourceIndex = selectedSourceIndex,
            selectedTargetIndex = selectedTargetIndex,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AlignUiState(),
    )

    val editUiState = combine(
        isEditing,
        needsSaving,
    ) { isEditing, needsSaving ->
        AlignEditUiState(
            isEditing = isEditing,
            needsSaving = needsSaving,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AlignEditUiState(),
    )

    override fun onDestroy() {
        viewModelScope.cancel()
    }

    fun load(pair: FilePairModel?, project: ProjectModel) {
        pairId = pair?.id ?: 0
        sourceLang = project.sourceLang
        targetLang = project.targetLang
        viewModelScope.launch(dispatcherProvider.io) {
            val allSourceUnits = segmentRepository.getAll(pairId = pairId, lang = sourceLang)
            sourceSegments.value = allSourceUnits

            val allTargetUnits = segmentRepository.getAll(pairId = pairId, lang = targetLang)
            targetSegments.value = allTargetUnits
        }
        isEditing.value = false
        needsSaving.value = false
    }

    // Selection

    fun selectSourceSegment(id: Int) {
        if (isEditing.value) {
            toggleEditing()
        }
        selectedSourceId.getAndUpdate {
            if (it == id) {
                null
            } else {
                id
            }
        }
        selectedTargetId.value = null
    }

    fun selectTargetSegment(id: Int) {
        if (isEditing.value) {
            toggleEditing()
        }
        selectedTargetId.getAndUpdate {
            if (it == id) {
                null
            } else {
                id
            }
        }
        selectedSourceId.value = null
    }

    // Reordering

    fun moveSegmentUp() {
        val sourceSelectedIdx = sourceSegments.value.indexOfFirst { it.id == selectedSourceId.value }
        val targetSelectedIdx = targetSegments.value.indexOfFirst { it.id == selectedTargetId.value }
        when {
            sourceSelectedIdx > 0 -> {
                sourceSegments.getAndUpdate { oldList ->
                    val newList = oldList.toMutableList()
                    newList.add(sourceSelectedIdx - 1, newList.removeAt(sourceSelectedIdx))
                    newList
                }
                needsSaving.value = true
            }

            targetSelectedIdx > 0 -> {
                targetSegments.getAndUpdate { oldList ->
                    val newList = oldList.toMutableList()
                    newList.add(targetSelectedIdx - 1, newList.removeAt(targetSelectedIdx))
                    newList
                }
                needsSaving.value = true
            }
        }
    }

    fun moveSegmentDown() {
        val sourceSelectedIdx = sourceSegments.value.indexOfFirst { it.id == selectedSourceId.value }
        val targetSelectedIdx = targetSegments.value.indexOfFirst { it.id == selectedTargetId.value }
        when {
            sourceSelectedIdx >= 0 && sourceSelectedIdx < sourceSegments.value.size - 1 -> {
                sourceSegments.getAndUpdate { oldList ->
                    val newList = oldList.toMutableList()
                    newList.add(sourceSelectedIdx + 1, newList.removeAt(sourceSelectedIdx))
                    newList
                }
                needsSaving.value = true
            }

            targetSelectedIdx >= 0 && targetSelectedIdx < targetSegments.value.size - 1 -> {
                targetSegments.getAndUpdate { oldList ->
                    val newList = oldList.toMutableList()
                    newList.add(targetSelectedIdx + 1, newList.removeAt(targetSelectedIdx))
                    newList
                }
                needsSaving.value = true
            }
        }
    }

    // Merging
    fun mergeWithPreviousSegment() {
        if (isEditing.value) {
            toggleEditing()
        }
        val sourceSelectedIdx = sourceSegments.value.indexOfFirst { it.id == selectedSourceId.value }
        val targetSelectedIdx = targetSegments.value.indexOfFirst { it.id == selectedTargetId.value }
        when {
            sourceSelectedIdx > 0 -> {
                viewModelScope.launch(dispatcherProvider.io) {
                    sourceSegments.getAndUpdate { oldList ->
                        val newList = oldList.toMutableList()
                        val current = newList.removeAt(sourceSelectedIdx)
                        val previousIndex = sourceSelectedIdx - 1
                        val enlarged = newList[previousIndex].let {
                            it.copy(
                                text = buildString {
                                    append(it.text)
                                    append(" ")
                                    append(current.text)
                                },
                            )
                        }
                        newList[previousIndex] = enlarged
                        selectedSourceId.update { enlarged.id }
                        segmentRepository.update(enlarged)
                        segmentRepository.delete(current)
                        newList
                    }
                    needsSaving.value = true
                }
            }

            targetSelectedIdx > 0 -> {
                viewModelScope.launch(dispatcherProvider.io) {
                    targetSegments.getAndUpdate { oldList ->
                        val newList = oldList.toMutableList()
                        val current = newList.removeAt(targetSelectedIdx)
                        val previousIndex = targetSelectedIdx - 1
                        val enlarged = newList[previousIndex].let {
                            it.copy(
                                text = buildString {
                                    append(it.text)
                                    append(" ")
                                    append(current.text)
                                },
                            )
                        }
                        newList[previousIndex] = enlarged
                        selectedTargetId.update { enlarged.id }
                        segmentRepository.update(enlarged)
                        segmentRepository.delete(current)
                        newList
                    }
                    needsSaving.value = true
                }
            }
        }
    }

    fun mergeWithNextSegment() {
        if (isEditing.value) {
            toggleEditing()
        }
        val sourceSelectedIdx = sourceSegments.value.indexOfFirst { it.id == selectedSourceId.value }
        val targetSelectedIdx = targetSegments.value.indexOfFirst { it.id == selectedTargetId.value }
        when {
            sourceSelectedIdx >= 0 && sourceSelectedIdx < sourceSegments.value.size - 1 -> {
                viewModelScope.launch(dispatcherProvider.io) {
                    sourceSegments.getAndUpdate { oldList ->
                        val newList = oldList.toMutableList()
                        val current = newList.removeAt(sourceSelectedIdx)
                        val enlarged = newList[sourceSelectedIdx].let {
                            it.copy(
                                text = buildString {
                                    append(current.text)
                                    append(" ")
                                    append(it.text)
                                },
                            )
                        }
                        newList[sourceSelectedIdx] = enlarged
                        selectedSourceId.update { enlarged.id }
                        segmentRepository.update(enlarged)
                        segmentRepository.delete(current)
                        newList
                    }
                    needsSaving.value = true
                }
            }

            targetSelectedIdx >= 0 && targetSelectedIdx < targetSegments.value.size - 1 -> {
                viewModelScope.launch(dispatcherProvider.io) {
                    targetSegments.getAndUpdate { oldList ->
                        val newList = oldList.toMutableList()
                        val current = newList.removeAt(targetSelectedIdx)
                        val enlarged = newList[targetSelectedIdx].let {
                            it.copy(
                                text = buildString {
                                    append(current.text)
                                    append(" ")
                                    append(it.text)
                                },
                            )
                        }
                        newList[targetSelectedIdx] = enlarged
                        selectedTargetId.update { enlarged.id }
                        segmentRepository.update(enlarged)
                        segmentRepository.delete(current)
                        newList
                    }
                    needsSaving.value = true
                }
            }
        }
    }

    // Creation and deletion

    fun createSegmentBefore() {
        if (isEditing.value) {
            toggleEditing()
        }
        val sourceSelectedIdx = sourceSegments.value.indexOfFirst { it.id == selectedSourceId.value }
        val targetSelectedIdx = targetSegments.value.indexOfFirst { it.id == selectedTargetId.value }
        when {
            sourceSelectedIdx >= 0 -> {
                viewModelScope.launch(dispatcherProvider.io) {
                    val segment = SegmentModel(lang = sourceLang)
                    val id = segmentRepository.create(model = segment, pairId = pairId)
                    sourceSegments.getAndUpdate {
                        val newList = it.toMutableList()
                        newList.add(sourceSelectedIdx, segment.copy(id = id))
                        newList
                    }
                    selectedSourceId.value = id
                    toggleEditing()
                    needsSaving.value = true
                }
            }

            targetSelectedIdx >= 0 -> {
                viewModelScope.launch(dispatcherProvider.io) {
                    val segment = SegmentModel(lang = targetLang)
                    val id = segmentRepository.create(model = segment, pairId = pairId)
                    targetSegments.getAndUpdate {
                        val newList = it.toMutableList()
                        newList.add(targetSelectedIdx, segment.copy(id = id))
                        newList
                    }
                    selectedTargetId.value = id
                    toggleEditing()
                    needsSaving.value = true
                }
            }
        }
    }

    fun createSegmentAfter() {
        if (isEditing.value) {
            toggleEditing()
        }
        val sourceSelectedIdx = sourceSegments.value.indexOfFirst { it.id == selectedSourceId.value }
        val targetSelectedIdx = targetSegments.value.indexOfFirst { it.id == selectedTargetId.value }
        when {
            sourceSelectedIdx >= 0 -> {
                viewModelScope.launch(dispatcherProvider.io) {
                    val segment = SegmentModel(lang = sourceLang)
                    val id = segmentRepository.create(model = segment, pairId = pairId)
                    sourceSegments.getAndUpdate {
                        val newList = it.toMutableList()
                        newList.add(sourceSelectedIdx + 1, segment.copy(id = id))
                        newList
                    }
                    selectedSourceId.value = id
                    toggleEditing()
                    needsSaving.value = true
                }
            }

            targetSelectedIdx >= 0 -> {
                viewModelScope.launch(dispatcherProvider.io) {
                    val segment = SegmentModel(lang = targetLang)
                    val id = segmentRepository.create(model = segment, pairId = pairId)
                    targetSegments.getAndUpdate {
                        val newList = it.toMutableList()
                        newList.add(targetSelectedIdx + 1, segment.copy(id = id))
                        newList
                    }
                    selectedTargetId.value = id
                    toggleEditing()
                    needsSaving.value = true
                }
            }
        }
    }

    fun deleteSegment() {
        val sourceSelectedIdx = sourceSegments.value.indexOfFirst { it.id == selectedSourceId.value }
        val targetSelectedIdx = targetSegments.value.indexOfFirst { it.id == selectedTargetId.value }
        when {
            sourceSelectedIdx >= 0 -> {
                viewModelScope.launch(dispatcherProvider.io) {
                    sourceSegments.getAndUpdate {
                        val newList = it.toMutableList()
                        val current = newList.removeAt(sourceSelectedIdx)
                        if (newList.isNotEmpty()) {
                            val newSelectedIdx = (sourceSelectedIdx - 1).coerceIn(newList.indices)
                            selectedSourceId.value = newList[newSelectedIdx].id
                        }
                        segmentRepository.delete(current)
                        newList
                    }
                    needsSaving.value = true
                }
            }

            targetSelectedIdx >= 0 -> {
                viewModelScope.launch(dispatcherProvider.io) {
                    targetSegments.getAndUpdate {
                        val newList = it.toMutableList()
                        val current = newList.removeAt(targetSelectedIdx)
                        if (newList.isNotEmpty()) {
                            val newSelectedIdx = (targetSelectedIdx - 1).coerceIn(newList.indices)
                            selectedTargetId.value = newList[newSelectedIdx].id
                        }
                        segmentRepository.delete(current)
                        newList
                    }
                    needsSaving.value = true
                }
            }
        }
    }

    // Editing

    fun toggleEditing() {
        val sourceSelectedIdx = sourceSegments.value.indexOfFirst { it.id == selectedSourceId.value }
        val targetSelectedIdx = targetSegments.value.indexOfFirst { it.id == selectedTargetId.value }
        if (sourceSelectedIdx < 0 && targetSelectedIdx < 0) {
            return
        }

        lastCursorPosition = -1
        isEditing.getAndUpdate {
            if (it) {
                when {
                    sourceSelectedIdx >= 0 -> {
                        viewModelScope.launch {
                            val current = sourceSegments.value[sourceSelectedIdx]
                            segmentRepository.update(current)
                        }
                    }

                    else -> {
                        viewModelScope.launch {
                            val current = targetSegments.value[targetSelectedIdx]
                            segmentRepository.update(current)
                        }
                    }
                }

                false
            } else {
                true
            }
        }
    }

    fun editSegment(id: Int, value: String, position: Int) {
        if (!isEditing.value) {
            return
        }

        lastCursorPosition = position
        val sourceSelectedIdx = sourceSegments.value.indexOfFirst { it.id == selectedSourceId.value }
        val targetSelectedIdx = targetSegments.value.indexOfFirst { it.id == selectedTargetId.value }
        when {
            sourceSelectedIdx >= 0 -> {
                sourceSegments.getAndUpdate { oldList ->
                    oldList.map { segment ->
                        if (segment.id == id) {
                            segment.copy(text = value)
                        } else {
                            segment
                        }
                    }
                }
            }

            targetSelectedIdx >= 0 -> {
                targetSegments.getAndUpdate { oldList ->
                    oldList.map { segment ->
                        if (segment.id == id) {
                            segment.copy(text = value)
                        } else {
                            segment
                        }
                    }
                }
            }
        }
    }

    fun splitSegment() {
        if (!isEditing.value) {
            return
        }
        val sourceSelectedIdx = sourceSegments.value.indexOfFirst { it.id == selectedSourceId.value }
        val targetSelectedIdx = targetSegments.value.indexOfFirst { it.id == selectedTargetId.value }
        when {
            sourceSelectedIdx >= 0 -> {
                viewModelScope.launch(dispatcherProvider.io) {
                    sourceSegments.getAndUpdate { oldList ->
                        val newList = oldList.toMutableList()
                        val current = newList.removeAt(sourceSelectedIdx)
                        if (lastCursorPosition !in current.text.indices) {
                            return@getAndUpdate oldList
                        }
                        val oldSegment = current.copy(text = current.text.substring(0, lastCursorPosition).trim())
                        newList.add(
                            index = sourceSelectedIdx,
                            element = oldSegment,
                        )
                        val newSegment = SegmentModel(
                            lang = sourceLang,
                            text = current.text.substring(lastCursorPosition).trim(),
                        )
                        segmentRepository.update(oldSegment)
                        val id = segmentRepository.create(model = newSegment, pairId = pairId)
                        newList.add(
                            index = sourceSelectedIdx + 1,
                            element = newSegment.copy(id = id),
                        )
                        newList
                    }
                    toggleEditing()
                    needsSaving.value = true
                }
            }

            targetSelectedIdx >= 0 -> {
                viewModelScope.launch(dispatcherProvider.io) {
                    targetSegments.getAndUpdate { oldList ->
                        val newList = oldList.toMutableList()
                        val current = newList.removeAt(targetSelectedIdx)
                        if (lastCursorPosition !in current.text.indices) {
                            return@getAndUpdate oldList
                        }
                        val oldSegment = current.copy(text = current.text.substring(0, lastCursorPosition).trim())
                        newList.add(
                            index = targetSelectedIdx,
                            element = oldSegment,
                        )
                        val newSegment = SegmentModel(
                            lang = targetLang,
                            text = current.text.substring(lastCursorPosition).trim(),
                        )
                        segmentRepository.update(oldSegment)
                        val id = segmentRepository.create(model = newSegment, pairId = pairId)
                        newList.add(
                            index = targetSelectedIdx + 1,
                            element = newSegment.copy(id = id),
                        )
                        newList
                    }
                    toggleEditing()
                    needsSaving.value = true
                }
            }
        }
    }

    fun save() {
        viewModelScope.launch(dispatcherProvider.io) {
            sourceSegments.getAndUpdate { oldList ->
                val newList = oldList.mapIndexed { idx, it -> it.copy(position = idx) }
                segmentRepository.updateAll(newList)
                newList
            }
            targetSegments.getAndUpdate { oldList ->
                val newList = oldList.mapIndexed { idx, it -> it.copy(position = idx) }
                segmentRepository.updateAll(newList)
                newList
            }
            needsSaving.value = false
        }
    }
}
