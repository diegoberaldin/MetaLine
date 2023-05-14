package projectedit.di

import org.koin.dsl.module
import projectedit.ui.dialog.EditProjectViewModel

val projectEditModule = module {
    single {
        EditProjectViewModel()
    }
}
