package align.di

import align.ui.AlignViewModel
import org.koin.dsl.module

val alignModule = module {
    factory {
        AlignViewModel(
            dispatcherProvider = get(),
            logManager = get(),
            segmentRepository = get(),
        )
    }
}
