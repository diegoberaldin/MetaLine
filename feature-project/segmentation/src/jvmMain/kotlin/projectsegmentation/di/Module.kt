package projectsegmentation.di

import org.koin.dsl.module
import projectsegmentation.ui.ProjectSegmentationViewModel

val projectSegmentationModule = module {
    factory {
        ProjectSegmentationViewModel(
            dispatcherProvider = get(),
            segmentationRuleRepository = get(),
            completeLanguage = get(),
        )
    }
}
