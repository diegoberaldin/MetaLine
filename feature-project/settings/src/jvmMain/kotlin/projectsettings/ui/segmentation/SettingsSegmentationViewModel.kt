package projectsettings.ui.segmentation

import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import common.coroutines.CoroutineDispatcherProvider
import data.LanguageModel
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
import repository.LanguageRepository
import repository.SegmentationRuleRepository
import usecase.GetCompleteLanguageUseCase

class SettingsSegmentationViewModel(
    private val dispatcherProvider: CoroutineDispatcherProvider,
    private val segmentationRuleRepository: SegmentationRuleRepository,
    private val languageRepository: LanguageRepository,
    private val completeLanguage: GetCompleteLanguageUseCase,
) : InstanceKeeper.Instance {

    private val rules = MutableStateFlow<List<SegmentationRuleModel>>(emptyList())
    private val availableLanguages = MutableStateFlow<List<LanguageModel>>(emptyList())
    private val currentLanguage = MutableStateFlow<LanguageModel?>(null)
    private val currentEditedRule = MutableStateFlow<Int?>(null)
    private val viewModelScope = CoroutineScope(SupervisorJob())

    val uiState = combine(
        currentLanguage,
        availableLanguages,
        rules,
        currentEditedRule,
    ) { currentLanguage, availableLanguages, rules, currentEditedRule ->
        SettingsSegmentationUiState(
            currentLanguage = currentLanguage,
            availableLanguages = availableLanguages,
            rules = rules,
            currentEditedRule = currentEditedRule,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SettingsSegmentationUiState(),
    )

    init {
        availableLanguages.value = languageRepository.getDefaultLanguages().map { completeLanguage(it) }
        val currentLang = availableLanguages.value.firstOrNull()
        if (currentLang != null) {
            setCurrentLanguage(currentLang)
        }
    }

    override fun onDestroy() {
        viewModelScope.cancel()
    }

    fun setCurrentLanguage(lang: LanguageModel) {
        currentLanguage.value = lang
        viewModelScope.launch(dispatcherProvider.io) {
            rules.value = segmentationRuleRepository.getAllDefault(lang = lang.code)
        }
    }

    fun deleteRule(index: Int) {
        val rule = rules.value[index]
        viewModelScope.launch(dispatcherProvider.io) {
            segmentationRuleRepository.delete(rule)
            val lang = currentLanguage.value?.code
            if (lang != null) {
                rules.value = segmentationRuleRepository.getAllDefault(lang = lang)
            }
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

    fun moveRuleUp(index: Int) {
        if (index <= 0) {
            return
        }
        currentEditedRule.value?.also {
            toggleEditRule(it)
        }
        rules.getAndUpdate {
            val newList = it.toMutableList()
            newList.add(index - 1, newList.removeAt(index))
            newList
        }
        updatePositions()
    }

    fun moveRuleDown(index: Int) {
        if (index >= rules.value.size - 1) {
            return
        }
        currentEditedRule.value?.also {
            toggleEditRule(it)
        }
        rules.getAndUpdate {
            val newList = it.toMutableList()
            newList.add(index + 1, newList.removeAt(index))
            newList
        }
        updatePositions()
    }

    private fun updatePositions() {
        viewModelScope.launch(dispatcherProvider.io) {
            rules.getAndUpdate {
                val newList = it.mapIndexed { idx, rule ->
                    rule.copy(position = idx)
                }
                segmentationRuleRepository.updateAll(newList)
                newList
            }
        }
    }
}
