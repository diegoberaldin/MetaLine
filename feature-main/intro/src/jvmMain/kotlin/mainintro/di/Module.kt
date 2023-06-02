package mainintro.di

import mainintro.ui.DefaultIntroComponent
import mainintro.ui.IntroComponent
import org.koin.dsl.module

val projectIntroModule = module {
    factory<IntroComponent> {
        DefaultIntroComponent(
            componentContext = it[0],
            coroutineContext = it[1],
            dispatcherProvider = get(),
            projectRepository = get(),
            notificationCenter = get(),
        )
    }
}
