package projectedit.di

import org.koin.dsl.module
import projectedit.ui.dialog.EditProjectViewModel
import projectmetadata.di.projectMetadataModule
import projectsegmentation.di.projectSegmentationModule

val projectEditModule = module {
    includes(projectMetadataModule)
    includes(projectSegmentationModule)

    single {
        EditProjectViewModel()
    }
}
