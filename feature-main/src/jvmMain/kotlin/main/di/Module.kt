package main.di

import main.ui.MainViewModel
import mainintro.di.projectIntroModule
import org.koin.dsl.module

val mainModule = module {
    includes(projectIntroModule)

    factory {
        MainViewModel(
            dispatcherProvider = get(),
            exportTmxUseCase = get(),
            keyStore = get(),
            projectRepository = get(),
            filePairRepository = get(),
            notificationCenter = get(),
        )
    }
}
