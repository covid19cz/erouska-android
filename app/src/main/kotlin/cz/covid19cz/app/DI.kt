package cz.covid19cz.app

import android.app.Application
import androidx.room.Room
import cz.covid19cz.app.db.AppDatabase
import cz.covid19cz.app.db.ExpositionDao
import cz.covid19cz.app.db.ExpositionRepository
import cz.covid19cz.app.db.ExpositionRepositoryImpl
import cz.covid19cz.app.db.export.CsvExporter
import cz.covid19cz.app.ui.login.LoginVM
import cz.covid19cz.app.ui.main.MainVM
import cz.covid19cz.app.ui.sandbox.SandboxVM
import cz.covid19cz.app.bt.BluetoothRepository
import org.koin.android.ext.koin.androidApplication
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel { MainVM() }
    viewModel { SandboxVM(get(), get()) }
    viewModel { LoginVM(get()) }
}

val databaseModule = module {
    fun provideDatabase(application: Application): AppDatabase {
        return Room.databaseBuilder(application, AppDatabase::class.java, "database")
            .fallbackToDestructiveMigration()
            .build()
    }

    fun provideDao(database: AppDatabase): ExpositionDao {
        return database.expositionDao
    }

    single { provideDatabase(androidApplication()) }
    single { provideDao(get()) }
    single { CsvExporter(get(), get()) }
}

val repositoryModule = module {
    fun provideDeviceRepository(deviceDao: ExpositionDao): ExpositionRepository {
        return ExpositionRepositoryImpl(deviceDao)
    }

    single { provideDeviceRepository(get()) }
    single { BluetoothRepository(get()) }
}

val allModules = listOf(viewModelModule, databaseModule, repositoryModule)
