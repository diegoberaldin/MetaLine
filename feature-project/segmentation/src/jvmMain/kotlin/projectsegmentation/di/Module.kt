package projectsegmentation.di

import org.koin.dsl.module
import projectsegmentation.ui.DefaultProjectSegmentationComponent
import projectsegmentation.ui.ProjectSegmentationComponent

val projectSegmentationModule = module {
    factory<ProjectSegmentationComponent> {
        DefaultProjectSegmentationComponent(
            componentContext = it[0],
            coroutineContext = it[1],
            dispatcherProvider = get(),
            segmentationRuleRepository = get(),
            projectRepository = get(),
            completeLanguage = get(),
        )
    }
}
