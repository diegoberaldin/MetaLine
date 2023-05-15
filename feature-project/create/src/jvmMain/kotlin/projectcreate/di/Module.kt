package projectcreate.di

import org.koin.dsl.module
import projectcreate.ui.dialog.CreateProjectViewModel

val projectCreateModule = module {
    factory {
        CreateProjectViewModel(
            dispatcherProvider = get(),
            languageRepository = get(),
            languageNameRepository = get(),
            flagsRepository = get(),
            segmentUseCase = get(),
            projectRepository = get(),
            filePairRepository = get(),
            segmentRepository = get(),
            notificationCenter = get(),
        )
    }
}
