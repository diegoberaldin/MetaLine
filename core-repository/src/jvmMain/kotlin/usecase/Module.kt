package usecase

import org.koin.dsl.module

val useCaseModule = module {
    single {
        InitializeDefaultSegmentationRulesUseCase(
            languageRepository = get(),
            segmentationRuleRepository = get(),
        )
    }
    single {
        GetCompleteLanguageUseCase(
            languageNameRepository = get(),
            flagsRepository = get(),
        )
    }
    single {
        ExportTmxUseCase()
    }
    single {
        SegmentTxtFileUseCase(
            projectRepository = get(),
            segmentationRuleRepository = get(),
        )
    }
}
