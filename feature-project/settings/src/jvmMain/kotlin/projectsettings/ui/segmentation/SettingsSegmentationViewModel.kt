package projectsettings.ui.segmentation

import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import common.coroutines.CoroutineDispatcherProvider
import data.SegmentationRuleModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import repository.SegmentationRuleRepository

class SettingsSegmentationViewModel(
    private val dispatcherProvider: CoroutineDispatcherProvider,
    private val segmentationRuleRepository: SegmentationRuleRepository,
) : InstanceKeeper.Instance {

    private val rules = MutableStateFlow<List<SegmentationRuleModel>>(emptyList())
    private val currentEditedRule = MutableStateFlow<Int?>(null)
    private val viewModelScope = CoroutineScope(SupervisorJob())

    val uiState = combine(rules, currentEditedRule) { rules, currentEditedRule ->
        SettingsSegmentationUiState(
            rules = rules,
            currentEditedRule = currentEditedRule,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SettingsSegmentationUiState(),
    )

    init {
        viewModelScope.launch(dispatcherProvider.io) {
            rules.value = segmentationRuleRepository.getAllDefault()
        }
    }

    override fun onDestroy() {
        viewModelScope.cancel()
    }

    fun deleteRule(index: Int) {
        val rule = rules.value[index]
        viewModelScope.launch(dispatcherProvider.io) {
            segmentationRuleRepository.delete(rule)
        }
    }

    fun createRule() {
        viewModelScope.launch(dispatcherProvider.io) {
            val newRule = SegmentationRuleModel()
            val id = segmentationRuleRepository.create(model = newRule, projectId = null)
            rules.getAndUpdate {
                buildList {
                    addAll(it)
                    this += newRule.copy(id = id)
                }
            }
            currentEditedRule.value = rules.value.size - 1
        }
    }

    fun toggleEditRule(index: Int) {
        viewModelScope.launch(dispatcherProvider.io) {
            currentEditedRule.getAndUpdate { oldIndex ->
                if (oldIndex != null) {
                    val rule = rules.value[oldIndex]
                    segmentationRuleRepository.update(rule)
                }

                if (oldIndex == index) {
                    null
                } else {
                    index
                }
            }
        }
    }

    fun editRuleBeforePattern(text: String, index: Int) {
        rules.getAndUpdate { oldList ->
            oldList.mapIndexed { idx, rule ->
                if (idx != index) {
                    rule
                } else {
                    rule.copy(before = text)
                }
            }
        }
    }

    fun editRuleAfterPattern(text: String, index: Int) {
        rules.getAndUpdate { oldList ->
            oldList.mapIndexed { idx, rule ->
                if (idx != index) {
                    rule
                } else {
                    rule.copy(after = text)
                }
            }
        }
    }

    fun toggleBreaking(index: Int) {
        rules.getAndUpdate { oldList ->
            oldList.mapIndexed { idx, rule ->
                if (idx != index) {
                    rule
                } else {
                    rule.copy(breaking = !rule.breaking)
                }
            }
        }
    }
}
