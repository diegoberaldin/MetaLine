package persistence.di

import org.koin.dsl.module
import persistence.AppDatabase

private val innerDbModule = module {
    single {
        AppDatabase(fileManager = get())
    }
}

val persistenceModule = module {
    includes(innerDbModule)

    single {
        val db: AppDatabase = get()
        db.projectDao()
    }
    single {
        val db: AppDatabase = get()
        db.filePairDao()
    }
    single {
        val db: AppDatabase = get()
        db.segmentDao()
    }
}
