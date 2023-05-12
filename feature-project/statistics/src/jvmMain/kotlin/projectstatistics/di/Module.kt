package projectstatistics.di

import org.koin.dsl.module
import projectstatistics.ui.dialog.StatisticsViewModel

val projectStatisticsModule = module {
    factory {
        StatisticsViewModel(
            dispatcherProvider = get(),
            filePairRepository = get(),
            segmentRepository = get(),
        )
    }
}
