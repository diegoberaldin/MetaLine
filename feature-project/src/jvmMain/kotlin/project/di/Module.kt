package project.di

import org.koin.dsl.module
import projectcreate.di.projectCreateModule

val projectModule = module {
    includes(projectCreateModule)
}
