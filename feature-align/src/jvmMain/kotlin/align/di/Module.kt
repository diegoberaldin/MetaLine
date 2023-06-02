package align.di

import align.ui.AlignComponent
import align.ui.DefaultAlignComponent
import org.koin.dsl.module

val alignModule = module {
    factory<AlignComponent> {
        DefaultAlignComponent(
            componentContext = it[0],
            coroutineContext = it[1],
            dispatcherProvider = get(),
            segmentRepository = get(),
        )
    }
}
