package projectstatistics.di

import org.koin.dsl.module
import projectstatistics.ui.dialog.DefaultStatisticsComponent
import projectstatistics.ui.dialog.StatisticsComponent

val projectStatisticsModule = module {
    factory<StatisticsComponent> {
        DefaultStatisticsComponent(
            componentContext = it[0],
            coroutineContext = it[1],
            dispatcherProvider = get(),
            filePairRepository = get(),
            segmentRepository = get(),
        )
    }
}
