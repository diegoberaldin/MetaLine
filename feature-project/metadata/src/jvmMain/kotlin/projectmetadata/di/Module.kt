package projectmetadata.di

import org.koin.dsl.module
import projectmetadata.ui.ProjectMetadataViewModel

val projectMetadataModule = module {
    factory {
        ProjectMetadataViewModel(
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
