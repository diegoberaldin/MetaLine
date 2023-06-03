package projectstatistics.ui.dialog

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.doOnCreate
import com.arkivanov.essenty.lifecycle.doOnDestroy
import common.coroutines.CoroutineDispatcherProvider
import data.ProjectModel
import data.SegmentModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import localized
import repository.FilePairRepository
import repository.SegmentRepository
import kotlin.coroutines.CoroutineContext

internal class DefaultStatisticsComponent(
    componentContext: ComponentContext,
    coroutineContext: CoroutineContext,
    private val dispatcherProvider: CoroutineDispatcherProvider,
    private val filePairRepository: FilePairRepository,
    private val segmentRepository: SegmentRepository,
) : StatisticsComponent, ComponentContext by componentContext {

    private val items = MutableStateFlow<List<StatisticsItem>>(emptyList())
    private val loading = MutableStateFlow(false)
    private lateinit var viewModelScope: CoroutineScope

    override lateinit var uiState: StateFlow<StatisticsUiState>

    init {
        with(lifecycle) {
            doOnCreate {
                viewModelScope = CoroutineScope(coroutineContext + SupervisorJob())
                uiState = combine(items, loading) { items, loading ->
                    StatisticsUiState(
                        items = items,
                        loading = loading,
                    )
                }.stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5000),
                    initialValue = StatisticsUiState(),
                )
            }
            doOnDestroy {
                viewModelScope.cancel()
            }
        }
    }

    override fun load(project: ProjectModel) {
        val projectId = project.id
        loading.value = true
        viewModelScope.launch(dispatcherProvider.io) {
            val filePairs = filePairRepository.getAll(projectId)
            val filePairRegistry = mutableMapOf<Int, Pair<Int, Int>>()
            var totalSegments = 0L
            val sourceSegments = mutableListOf<SegmentModel>()
            val targetSegments = mutableListOf<SegmentModel>()
            for (pair in filePairs) {
                val sourcePairSegments = segmentRepository.getAll(pairId = pair.id, lang = project.sourceLang)
                val targetPairSegments = segmentRepository.getAll(pair.id, lang = project.targetLang)
                val segmentCount = minOf(sourcePairSegments.size, targetPairSegments.size)
                sourceSegments += sourcePairSegments.subList(
                    fromIndex = 0,
                    toIndex = segmentCount,
                )
                targetSegments += targetPairSegments.subList(
                    fromIndex = 0,
                    toIndex = segmentCount,
                )
                val cleanSegmentLists = sourceSegments.zip(targetSegments)
                    .mapNotNull {
                        if (it.first.text.isEmpty() || it.second.text.isEmpty()) {
                            null
                        } else {
                            it
                        }
                    }.unzip()
                val unpairedSegments =
                    (sourceSegments.size - cleanSegmentLists.first.size) + (targetSegments.size - cleanSegmentLists.second.size)

                totalSegments += cleanSegmentLists.first.size
                val pairedSegments = cleanSegmentLists.first.size
                filePairRegistry[pair.id] = pairedSegments to unpairedSegments
            }

            items.update {
                buildList {
                    this += StatisticsItem.Header("dialog_statistics_section_general".localized())
                    this += StatisticsItem.TextRow(
                        title = "dialog_statistics_item_total_file_pairs".localized(),
                        value = filePairs.count().toString(),
                    )
                    this += StatisticsItem.TextRow(
                        title = "dialog_statistics_item_translation_units".localized(),
                        value = totalSegments.toString(),
                    )

                    this += StatisticsItem.Divider

                    this += StatisticsItem.Header(title = "dialog_statistics_section_file_pairs".localized())
                    for (pair in filePairs) {
                        this += StatisticsItem.SubHeader(
                            title = pair.name,
                        )
                        val pairedCount = filePairRegistry[pair.id]?.first ?: 0
                        val unpairedCount = filePairRegistry[pair.id]?.second ?: 0

                        this += StatisticsItem.TextRow(
                            title = "dialog_statistics_item_translation_units".localized(),
                            value = pairedCount.toString(),
                        )

                        this += StatisticsItem.BarChartRow(
                            title = "dialog_statistics_item_completion".localized(),
                            value = pairedCount.toFloat() / (pairedCount + unpairedCount),
                        )
                    }
                }
            }
        }
        loading.value = false
    }
}
