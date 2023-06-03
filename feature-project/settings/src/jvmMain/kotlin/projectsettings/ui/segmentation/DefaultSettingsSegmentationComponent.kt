package projectsettings.ui.segmentation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.doOnCreate
import com.arkivanov.essenty.lifecycle.doOnDestroy
import common.coroutines.CoroutineDispatcherProvider
import data.LanguageModel
import data.SegmentationRuleModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import repository.LanguageRepository
import repository.SegmentationRuleRepository
import usecase.GetCompleteLanguageUseCase
import kotlin.coroutines.CoroutineContext

internal class DefaultSettingsSegmentationComponent(
    componentContext: ComponentContext,
    coroutineContext: CoroutineContext,
    private val dispatcherProvider: CoroutineDispatcherProvider,
    private val segmentationRuleRepository: SegmentationRuleRepository,
    private val languageRepository: LanguageRepository,
    private val completeLanguage: GetCompleteLanguageUseCase,
) : SettingsSegmentationComponent, ComponentContext by componentContext {

    private val rules = MutableStateFlow<List<SegmentationRuleModel>>(emptyList())
    private val availableLanguages = MutableStateFlow<List<LanguageModel>>(emptyList())
    private val currentLanguage = MutableStateFlow<LanguageModel?>(null)
    private val currentEditedRule = MutableStateFlow<Int?>(null)
    private lateinit var viewModelScope: CoroutineScope

    override lateinit var uiState: StateFlow<SettingsSegmentationUiState>

    init {
        with(lifecycle) {
            doOnCreate {
                viewModelScope = CoroutineScope(coroutineContext + SupervisorJob())

                uiState = combine(
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

                availableLanguages.value = languageRepository.getDefaultLanguages().map { completeLanguage(it) }
                val currentLang = availableLanguages.value.firstOrNull()
                if (currentLang != null) {
                    setCurrentLanguage(currentLang)
                }
            }
            doOnDestroy {
                viewModelScope.cancel()
            }
        }
    }

    override fun setCurrentLanguage(lang: LanguageModel) {
        currentLanguage.value = lang
        viewModelScope.launch(dispatcherProvider.io) {
            rules.value = segmentationRuleRepository.getAllDefault(lang = lang.code)
        }
    }

    override fun deleteRule(index: Int) {
        val rule = rules.value[index]
        viewModelScope.launch(dispatcherProvider.io) {
            segmentationRuleRepository.delete(rule)
            val lang = currentLanguage.value?.code
            if (lang != null) {
                rules.value = segmentationRuleRepository.getAllDefault(lang = lang)
            }
        }
    }

    override fun createRule() {
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

    override fun toggleEditRule(index: Int) {
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

    override fun editRuleBeforePattern(text: String, index: Int) {
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

    override fun editRuleAfterPattern(text: String, index: Int) {
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

    override fun toggleBreaking(index: Int) {
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

    override fun moveRuleUp(index: Int) {
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

    override fun moveRuleDown(index: Int) {
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
