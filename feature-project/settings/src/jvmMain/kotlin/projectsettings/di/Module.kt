package projectsettings.di

import org.koin.dsl.module
import projectsettings.ui.dialog.SettingsViewModel
import projectsettings.ui.general.SettingsGeneralViewModel

val projectSettingsModule = module {
    factory {
        SettingsViewModel()
    }
    factory {
        SettingsGeneralViewModel(
            dispatcherProvider = get(),
            languageNameRepository = get(),
            flagsRepository = get(),
            keyStore = get(),
        )
    }
}
