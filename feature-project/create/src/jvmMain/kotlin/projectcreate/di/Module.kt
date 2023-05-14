package projectcreate.di

import org.koin.dsl.module
import projectcreate.ui.dialog.CreateProjectViewModel
import projectmedatada.di.projectMetadataModule

val projectCreateModule = module {
    includes(projectMetadataModule)

    factory {
        CreateProjectViewModel()
    }
}
