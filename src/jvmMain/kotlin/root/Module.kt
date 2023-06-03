package root

import org.koin.dsl.module
import kotlin.coroutines.coroutineContext

val rootModule = module {
    factory<RootComponent> {
        DefaultRootComponent(
            componentContext = it[0],
            coroutineContext = it[1],
            dispatcherProvider = get(),
        )
    }
}
