package projectsettings.di

import org.koin.dsl.module
import projectsettings.ui.dialog.SettingsViewModel

val projectSettingsModule = module {
    factory {
        SettingsViewModel()
    }
}
