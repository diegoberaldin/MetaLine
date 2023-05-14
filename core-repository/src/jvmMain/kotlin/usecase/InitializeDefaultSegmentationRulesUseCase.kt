package usecase

import repository.SegmentationRuleRepository

class InitializeDefaultSegmentationRulesUseCase(
    private val segmentationRuleRepository: SegmentationRuleRepository,
) {
    suspend operator fun invoke() {
        val defaultRules = segmentationRuleRepository.getAllDefault()
        if (defaultRules.isEmpty()) {
            for (rule in segmentationRuleRepository.getInitialDefaultRules()) {
                segmentationRuleRepository.create(rule)
            }
        }
    }
}
