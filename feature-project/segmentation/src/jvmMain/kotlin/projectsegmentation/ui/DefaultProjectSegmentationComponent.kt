package projectsegmentation.ui

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.doOnCreate
import com.arkivanov.essenty.lifecycle.doOnDestroy
import common.coroutines.CoroutineDispatcherProvider
import data.LanguageModel
import data.ProjectModel
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
import repository.ProjectRepository
import repository.SegmentationRuleRepository
import usecase.GetCompleteLanguageUseCase
import kotlin.coroutines.CoroutineContext

internal class DefaultProjectSegmentationComponent(
    componentContext: ComponentContext,
    coroutineContext: CoroutineContext,
    private val dispatcherProvider: CoroutineDispatcherProvider,
    private val segmentationRuleRepository: SegmentationRuleRepository,
    private val projectRepository: ProjectRepository,
    private val completeLanguage: GetCompleteLanguageUseCase,
) : ProjectSegmentationComponent, ComponentContext by componentContext {

    private val applyDefaultRules = MutableStateFlow(true)
    private val rules = MutableStateFlow<List<SegmentationRuleModel>>(emptyList())
    private val availableLanguages = MutableStateFlow<List<LanguageModel>>(emptyList())
    private val currentLanguage = MutableStateFlow<LanguageModel?>(null)
    private val currentEditedRule = MutableStateFlow<Int?>(null)
    private lateinit var viewModelScope: CoroutineScope
    private var projectId: Int = 0

    override lateinit var uiState: StateFlow<ProjectSegmentationUiState>

    init {
        with(lifecycle) {
            doOnCreate {
                viewModelScope = CoroutineScope(coroutineContext + SupervisorJob())
                uiState = combine(
                    applyDefaultRules,
                    currentLanguage,
                    availableLanguages,
                    rules,
                    currentEditedRule,
                ) { applyDefaultRules, currentLanguage, availableLanguages, rules, currentEditedRule ->
                    ProjectSegmentationUiState(
                        applyDefaultRules = applyDefaultRules,
                        currentLanguage = currentLanguage,
                        availableLanguages = availableLanguages,
                        rules = rules,
                        currentEditedRule = currentEditedRule,
                    )
                }.stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5_000),
                    initialValue = ProjectSegmentationUiState(),
                )
            }
            doOnDestroy {
                viewModelScope.cancel()
            }
        }
    }

    override fun load(project: ProjectModel) {
        projectId = project.id

        val projectLanguages = listOf(project.sourceLang, project.targetLang)
            .map { completeLanguage(LanguageModel(code = it)) }
        availableLanguages.value = projectLanguages

        val currentLang = availableLanguages.value.firstOrNull()
        if (currentLang != null) {
            setCurrentLanguage(currentLang)
        }

        applyDefaultRules.value = project.applyDefaultSegmentationRules
    }

    override fun setCurrentLanguage(lang: LanguageModel) {
        currentLanguage.value = lang
        viewModelScope.launch(dispatcherProvider.io) {
            refreshRules()
        }
    }

    override fun deleteRule(index: Int) {
        val rule = rules.value[index]
        viewModelScope.launch(dispatcherProvider.io) {
            segmentationRuleRepository.delete(rule)
            refreshRules()
        }
    }

    override fun createRule() {
        viewModelScope.launch(dispatcherProvider.io) {
            val newRule = SegmentationRuleModel()
            val id = segmentationRuleRepository.create(model = newRule, projectId = projectId)
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

    override fun toggleApplyDefaultRules() {
        val newValue = !applyDefaultRules.value
        applyDefaultRules.value = newValue
        val projectLanguages = availableLanguages.value.map { it.code }
        viewModelScope.launch(dispatcherProvider.io) {
            val project = projectRepository.getById(projectId)
            if (project != null) {
                projectRepository.update(project.copy(applyDefaultSegmentationRules = newValue))
            }

            if (newValue) {
                for (lang in projectLanguages) {
                    val rules = segmentationRuleRepository.getAll(projectId = projectId, lang = lang)
                    for (rule in rules) {
                        segmentationRuleRepository.delete(rule)
                    }
                }
            } else {
                for (lang in projectLanguages) {
                    val rules = segmentationRuleRepository.getAllDefault(lang = lang)
                    for (rule in rules) {
                        segmentationRuleRepository.create(model = rule, projectId = projectId)
                    }
                }
            }
            refreshRules()
        }
    }

    private suspend fun refreshRules() {
        val lang = currentLanguage.value?.code ?: return
        val applyDefault = applyDefaultRules.value
        rules.value = if (applyDefault) {
            segmentationRuleRepository.getAllDefault(
                lang = lang,
            )
        } else {
            segmentationRuleRepository.getAll(
                projectId = projectId,
                lang = lang,
            )
        }
    }
}
