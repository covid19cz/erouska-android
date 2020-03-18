package cz.covid19cz.app

import android.app.Application
import androidx.room.Room
import cz.covid19cz.app.db.AppDatabase
import cz.covid19cz.app.repository.device.DeviceDao
import cz.covid19cz.app.repository.device.DeviceRepository
import cz.covid19cz.app.repository.device.DeviceRepositoryImpl
import cz.covid19cz.app.ui.login.LoginVM
import cz.covid19cz.app.ui.main.MainVM
import cz.covid19cz.app.ui.sandbox.SandboxVM
import org.koin.android.ext.koin.androidApplication
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel { MainVM() }
    viewModel { SandboxVM() }
    viewModel { LoginVM(get()) }
}

val databaseModule = module {
    fun provideDatabase(application: Application): AppDatabase {
        return Room.databaseBuilder(application, AppDatabase::class.java, "covid.database")
            .fallbackToDestructiveMigration()
            .build()
    }


    fun provideDao(database: AppDatabase): DeviceDao {
        return database.deviceDao
    }

    single { provideDatabase(androidApplication()) }
    single { provideDao(get()) }
}

val repositoryModule = module {
    fun provideDeviceRepository(deviceDao: DeviceDao): DeviceRepository {
        return DeviceRepositoryImpl(deviceDao)
    }

    single { provideDeviceRepository(get()) }
}

val allModules = listOf(viewModelModule, databaseModule, repositoryModule)
