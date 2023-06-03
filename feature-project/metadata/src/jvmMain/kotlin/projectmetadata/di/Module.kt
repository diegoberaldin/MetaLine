package projectmetadata.di

import org.koin.dsl.module
import projectmetadata.ui.DefaultProjectMetadataComponent
import projectmetadata.ui.ProjectMetadataComponent

val projectMetadataModule = module {
    factory<ProjectMetadataComponent> {
        DefaultProjectMetadataComponent(
            componentContext = it[0],
            coroutineContext = it[1],
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
