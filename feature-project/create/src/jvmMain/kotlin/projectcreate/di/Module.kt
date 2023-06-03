package projectcreate.di

import org.koin.dsl.module
import projectcreate.ui.dialog.CreateProjectComponent
import projectcreate.ui.dialog.DefaultCreateProjectComponent
import projectmetadata.di.projectMetadataModule
import projectsegmentation.di.projectSegmentationModule

val projectCreateModule = module {
    includes(projectMetadataModule)
    includes(projectSegmentationModule)

    factory<CreateProjectComponent> {
        DefaultCreateProjectComponent(
            componentContext = it[0],
            coroutineContext = it[1],
            dispatcherProvider = get(),
        )
    }
}
