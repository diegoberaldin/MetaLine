package projectcreate.di

import org.koin.dsl.module
import projectcreate.ui.dialog.CreateProjectViewModel
import projectmetadata.di.projectMetadataModule
import projectsegmentation.di.projectSegmentationModule

val projectCreateModule = module {
    includes(projectMetadataModule)
    includes(projectSegmentationModule)

    factory {
        CreateProjectViewModel()
    }
}
