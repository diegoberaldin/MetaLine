package projectedit.di

import org.koin.dsl.module
import projectedit.ui.dialog.DefaultEditProjectComponent
import projectedit.ui.dialog.EditProjectComponent
import projectmetadata.di.projectMetadataModule
import projectsegmentation.di.projectSegmentationModule

val projectEditModule = module {
    includes(projectMetadataModule)
    includes(projectSegmentationModule)

    single<EditProjectComponent> {
        DefaultEditProjectComponent(
            componentContext = it[0],
            coroutineContext = it[1],
            dispatcherProvider = get(),
        )
    }
}
