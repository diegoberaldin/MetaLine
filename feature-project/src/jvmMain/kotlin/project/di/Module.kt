package project.di

import org.koin.dsl.module
import projectcreate.di.projectCreateModule
import projectsettings.di.projectSettingsModule
import projectstatistics.di.projectStatisticsModule

val projectModule = module {
    includes(projectCreateModule)
    includes(projectStatisticsModule)
    includes(projectSettingsModule)
}
