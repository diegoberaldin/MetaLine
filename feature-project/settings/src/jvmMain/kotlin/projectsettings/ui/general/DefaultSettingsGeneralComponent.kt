package projectsettings.ui.general

import L10n
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.doOnCreate
import com.arkivanov.essenty.lifecycle.doOnDestroy
import common.coroutines.CoroutineDispatcherProvider
import common.keystore.TemporaryKeyStore
import data.LanguageModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import localized
import usecase.GetCompleteLanguageUseCase
import kotlin.coroutines.CoroutineContext

class DefaultSettingsGeneralComponent(
    componentContext: ComponentContext,
    coroutineContext: CoroutineContext,
    private val dispatcherProvider: CoroutineDispatcherProvider,
    private val completeLanguage: GetCompleteLanguageUseCase,
    private val keyStore: TemporaryKeyStore,
) : SettingsGeneralComponent, ComponentContext by componentContext {

    private val appLanguage = MutableStateFlow<LanguageModel?>(null)
    private val appVersion = MutableStateFlow("")
    private val availableLanguages = MutableStateFlow<List<LanguageModel>>(emptyList())

    private lateinit var viewModelScope: CoroutineScope

    override lateinit var uiState: StateFlow<SettingsGeneralUiState>

    init {
        with(lifecycle) {
            doOnCreate {
                viewModelScope = CoroutineScope(coroutineContext + SupervisorJob())

                appVersion.value = System.getProperty("jpackage.app-version") ?: "[debug]"
                viewModelScope.launch(dispatcherProvider.io) {
                    val langCode = "lang".localized()
                    appLanguage.value = completeLanguage(LanguageModel(code = langCode))
                }

                availableLanguages.value = listOf(
                    "en",
                    "it",
                    "es",
                ).map {
                    completeLanguage(LanguageModel(code = it))
                }

                uiState = combine(
                    appLanguage,
                    appVersion,
                    availableLanguages,
                ) { appLanguage, appVersion, availableLanguages ->
                    SettingsGeneralUiState(
                        appLanguage = appLanguage,
                        appVersion = appVersion,
                        availableLanguages = availableLanguages,
                    )
                }.stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5_000),
                    initialValue = SettingsGeneralUiState(),
                )
            }

            doOnDestroy {
                viewModelScope.cancel()
            }
        }
    }

    override fun setLanguage(value: LanguageModel) {
        appLanguage.value = value
        val langCode = value.code
        L10n.setLanguage(lang = langCode)
        viewModelScope.launch(dispatcherProvider.io) {
            keyStore.save("lang", langCode)
        }
    }
}
