package usecase

import org.koin.dsl.module

val useCaseModule = module {
    single {
        InitializeDefaultSegmentationRulesUseCase(
            segmentationRuleRepository = get(),
        )
    }
    single {
        ExportTmxUseCase()
    }
    single {
        SegmentTxtFileUseCase(
            segmentationRuleRepository = get(),
        )
    }
}
