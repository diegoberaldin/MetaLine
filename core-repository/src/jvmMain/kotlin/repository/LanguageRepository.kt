package repository

import data.LanguageModel
import java.util.*

class LanguageRepository() {

    fun getDefaultLanguages() = listOf(
        LanguageModel(code = Locale.ENGLISH.language),
        LanguageModel(code = Locale.FRENCH.language),
        LanguageModel(code = Locale.GERMAN.language),
        LanguageModel(code = Locale.ITALIAN.language),
        LanguageModel(code = "bg"),
        LanguageModel(code = "cs"),
        LanguageModel(code = "da"),
        LanguageModel(code = "el"),
        LanguageModel(code = "es"),
        LanguageModel(code = "et"),
        LanguageModel(code = "fi"),
        LanguageModel(code = "ga"),
        LanguageModel(code = "hr"),
        LanguageModel(code = "hu"),
        LanguageModel(code = "lt"),
        LanguageModel(code = "lv"),
        LanguageModel(code = "mt"),
        LanguageModel(code = "nl"),
        LanguageModel(code = "pl"),
        LanguageModel(code = "pt"),
        LanguageModel(code = "ro"),
        LanguageModel(code = "sk"),
        LanguageModel(code = "sl"),
        LanguageModel(code = "sw"),
    )
}
