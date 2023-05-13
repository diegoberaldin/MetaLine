package projectsettings.ui.general

import L10n
import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import common.coroutines.CoroutineDispatcherProvider
import common.keystore.TemporaryKeyStore
import data.LanguageModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import localized
import repository.FlagsRepository
import repository.LanguageNameRepository

class SettingsGeneralViewModel(
    private val dispatcherProvider: CoroutineDispatcherProvider,
    private val languageNameRepository: LanguageNameRepository,
    private val flagsRepository: FlagsRepository,
    private val keyStore: TemporaryKeyStore,
) : InstanceKeeper.Instance {

    private val appLanguage = MutableStateFlow<LanguageModel?>(null)
    private val appVersion = MutableStateFlow("")
    private val availableLanguages = MutableStateFlow<List<LanguageModel>>(emptyList())

    private val viewModelScope = CoroutineScope(SupervisorJob())

    val uiState = combine(
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

    init {
        appVersion.value = System.getProperty("jpackage.app-version") ?: "[debug]"
        viewModelScope.launch(dispatcherProvider.io) {
            val langCode = "lang".localized()
            appLanguage.value = LanguageModel(code = langCode, name = getLanguageName(langCode))
        }

        availableLanguages.value = listOf(
            "en",
            "it",
        ).map {
            LanguageModel(code = it, name = getLanguageName(it))
        }
    }

    override fun onDestroy() {
        viewModelScope.cancel()
    }

    private fun getLanguageName(code: String): String = buildString {
        append(flagsRepository.getFlag(code))
        append(" ")
        append(languageNameRepository.getName(code))
    }

    fun setLanguage(value: LanguageModel) {
        appLanguage.value = value
        val langCode = value.code
        L10n.setLanguage(lang = langCode)
        viewModelScope.launch(dispatcherProvider.io) {
            keyStore.save("lang", langCode)
        }
    }
}