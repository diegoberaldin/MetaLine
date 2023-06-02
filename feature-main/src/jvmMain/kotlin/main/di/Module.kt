package main.di

import main.ui.DefaultMainComponent
import main.ui.MainComponent
import mainintro.di.projectIntroModule
import org.koin.dsl.module

val mainModule = module {
    includes(projectIntroModule)

    factory<MainComponent> {
        DefaultMainComponent(
            componentContext = it[0],
            coroutineContext = it[1],
            dispatcherProvider = get(),
            exportTmxUseCase = get(),
            keyStore = get(),
            projectRepository = get(),
            filePairRepository = get(),
            segmentRepository = get(),
            notificationCenter = get(),
        )
    }
}
