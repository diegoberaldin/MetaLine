package projectcreate.di

import org.koin.dsl.module
import projectcreate.ui.dialog.CreateProjectViewModel

val projectCreateModule = module {
    factory {
        CreateProjectViewModel(
            dispatcherProvider = get(),
            languageRepository = get(),
            completeLanguage = get(),
            segmentUseCase = get(),
            projectRepository = get(),
            filePairRepository = get(),
            segmentRepository = get(),
            notificationCenter = get(),
        )
    }
}
