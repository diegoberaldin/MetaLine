package mainintro.di

import org.koin.dsl.module
import mainintro.ui.IntroViewModel

val projectIntroModule = module {
    factory {
        IntroViewModel(
            dispatcherProvider = get(),
            projectRepository = get(),
            notificationCenter = get(),
        )
    }
}
