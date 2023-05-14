package usecase

import repository.LanguageRepository
import repository.SegmentationRuleRepository

class InitializeDefaultSegmentationRulesUseCase(
    private val languageRepository: LanguageRepository,
    private val segmentationRuleRepository: SegmentationRuleRepository,
) {
    suspend operator fun invoke() {
        for (lang in languageRepository.getDefaultLanguages()) {
            val defaultRules = segmentationRuleRepository.getAllDefault(lang = lang.code)
            if (defaultRules.isEmpty()) {
                for (rule in segmentationRuleRepository.getInitialDefaultRules()) {
                    segmentationRuleRepository.create(rule)
                }
            }
        }
    }
}
