package align.ui

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.doOnCreate
import com.arkivanov.essenty.lifecycle.doOnDestroy
import data.FilePairModel
import data.ProjectModel
import data.SegmentModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import repository.SegmentRepository
import kotlin.coroutines.CoroutineContext
import kotlin.math.abs

internal class DefaultAlignComponent(
    componentContext: ComponentContext,
    coroutineContext: CoroutineContext,
    private val dispatcherProvider: common.coroutines.CoroutineDispatcherProvider,
    private val segmentRepository: SegmentRepository,
) : AlignComponent, ComponentContext by componentContext {

    private lateinit var viewModelScope: CoroutineScope
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

    override lateinit var uiState: StateFlow<AlignUiState>
    override lateinit var editUiState: StateFlow<AlignEditUiState>

    init {
        with(lifecycle) {
            doOnCreate {
                viewModelScope = CoroutineScope(coroutineContext + SupervisorJob())
                uiState = combine(
                    sourceSegments,
                    targetSegments,
                    selectedSourceId,
                    selectedTargetId,
                ) { sourceSegments, targetSegments, sourceId, targetId ->
                    AlignUiState(
                        sourceSegments = sourceSegments,
                        targetSegments = targetSegments,
                        selectedSourceId = sourceId,
                        selectedTargetId = targetId,
                    )
                }.stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5_000),
                    initialValue = AlignUiState(),
                )
                editUiState = combine(
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
            }
            doOnDestroy {
                viewModelScope.cancel()
            }
        }
    }

    override fun load(pair: FilePairModel?, project: ProjectModel) {
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

    override fun selectSourceSegment(id: Int) {
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

    override fun selectTargetSegment(id: Int) {
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

    override fun moveSegmentUp() {
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

    override fun moveSegmentDown() {
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

    private suspend fun trimEmptySegments() {
        val originalSourceSegments = sourceSegments.value
        val originalTargetSegments = targetSegments.value
        val removedIds = originalSourceSegments.zip(originalTargetSegments).filter {
            it.first.text.isEmpty() && it.second.text.isEmpty()
        }.map {
            segmentRepository.delete(it.first)
            segmentRepository.delete(it.second)
            it.first.id to it.second.id
        }.unzip()
        sourceSegments.getAndUpdate {
            it.filter { s -> s.id !in removedIds.first }
        }
        targetSegments.getAndUpdate {
            it.filter { s -> s.id !in removedIds.second }
        }
    }

    private suspend fun insertPlaceholders(removeTrailingEmptySegments: Boolean = true) {
        val sourceSize = sourceSegments.value.size
        val targetSize = targetSegments.value.size
        if (sourceSize == targetSize) {
            if (removeTrailingEmptySegments) {
                trimEmptySegments()
                save()
            }
            return
        }

        val insertCount = abs(sourceSize - targetSize)
        if (sourceSize < targetSize) {
            // adds to source
            for (i in 0 until insertCount) {
                val segment = SegmentModel(lang = sourceLang)
                val id = segmentRepository.create(model = segment, pairId = pairId)
                sourceSegments.getAndUpdate {
                    val newList = it.toMutableList()
                    newList.add(sourceSize + i, segment.copy(id = id))
                    newList
                }
            }
        } else {
            // adds to target
            for (i in 0 until insertCount) {
                val segment = SegmentModel(lang = targetLang)
                val id = segmentRepository.create(model = segment, pairId = pairId)
                targetSegments.getAndUpdate {
                    val newList = it.toMutableList()
                    newList.add(targetSize + i, segment.copy(id = id))
                    newList
                }
            }
        }
        if (removeTrailingEmptySegments) {
            trimEmptySegments()
        }
        save()
    }

    override fun mergeWithPreviousSegment() {
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
                    insertPlaceholders()
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
                    insertPlaceholders()
                }
            }
        }
    }

    override fun mergeWithNextSegment() {
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
                    insertPlaceholders()
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
                    insertPlaceholders()
                }
            }
        }
    }

    // Creation and deletion

    override fun createSegmentBefore() {
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
                    insertPlaceholders()
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
                    insertPlaceholders()
                }
            }
        }
    }

    override fun createSegmentAfter() {
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
                    insertPlaceholders(removeTrailingEmptySegments = false)
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
                    insertPlaceholders(removeTrailingEmptySegments = false)
                }
            }
        }
    }

    override fun deleteSegment() {
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
                    insertPlaceholders()
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
                    insertPlaceholders()
                }
            }
        }
    }

// Editing

    override fun toggleEditing() {
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

    override fun editSegment(id: Int, value: String, position: Int) {
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

    override fun splitSegment() {
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
                    insertPlaceholders()
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
                    insertPlaceholders()
                }
            }
        }
    }

    override fun save() {
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
