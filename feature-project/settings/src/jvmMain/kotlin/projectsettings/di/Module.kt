package projectsettings.di

import org.koin.dsl.module
import projectsettings.ui.dialog.DefaultSettingsComponent
import projectsettings.ui.dialog.SettingsComponent
import projectsettings.ui.general.DefaultSettingsGeneralComponent
import projectsettings.ui.general.SettingsGeneralComponent
import projectsettings.ui.segmentation.SettingsSegmentationViewModel

val projectSettingsModule = module {
    factory<SettingsComponent> {
        DefaultSettingsComponent(
            componentContext = it[0],
            coroutineContext = it[1],
            dispatcherProvider = get(),
        )
    }
    factory<SettingsGeneralComponent> {
        DefaultSettingsGeneralComponent(
            componentContext = it[0],
            coroutineContext = it[1],
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
