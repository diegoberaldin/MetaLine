package usecase

import org.koin.dsl.module

val useCaseModule = module {
    single {
        ExportTmxUseCase()
    }
    single {
        SegmentTxtFileUseCase()
    }
}
