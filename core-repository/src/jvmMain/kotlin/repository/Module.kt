package repository

import org.koin.dsl.module

val repositoryModule = module {
    single {
        LanguageRepository()
    }
    single {
        LanguageNameRepository()
    }
    single {
        FlagsRepository()
    }
    single {
        ProjectRepository(
            projectDao = get(),
        )
    }
    single {
        FilePairRepository(
            filePairDao = get(),
        )
    }
    single {
        SegmentRepository(
            segmentDao = get(),
        )
    }
}
