package projectsettings.di

import org.koin.dsl.module
import projectsettings.ui.dialog.DefaultSettingsComponent
import projectsettings.ui.dialog.SettingsComponent
import projectsettings.ui.general.SettingsGeneralViewModel
import projectsettings.ui.segmentation.SettingsSegmentationViewModel

val projectSettingsModule = module {
    factory<SettingsComponent> {
        DefaultSettingsComponent(
            componentContext = it[0],
            coroutineContext = it[1],
            dispatcherProvider = get(),
        )
    }
    factory {
        SettingsGeneralViewModel(
            dispatcherProvider = get(),
            completeLanguage = get(),
            keyStore = get(),
        )
    }
    factory {
        SettingsSegmentationViewModel(
            dispatcherProvider = get(),
            segmentationRuleRepository = get(),
            languageRepository = get(),
            completeLanguage = get(),
        )
    }
}
