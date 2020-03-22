package cz.covid19cz.app

import android.app.Application
import android.app.NotificationManager
import android.os.PowerManager
import androidx.core.content.getSystemService
import androidx.room.Room
import cz.covid19cz.app.bt.BluetoothRepository
import cz.covid19cz.app.db.*
import cz.covid19cz.app.db.export.CsvExporter
import cz.covid19cz.app.receiver.BatterSaverStateReceiver
import cz.covid19cz.app.receiver.BluetoothStateReceiver
import cz.covid19cz.app.receiver.LocationStateReceiver
import cz.covid19cz.app.service.WakeLockManager
import cz.covid19cz.app.ui.btdisabled.BtDisabledVM
import cz.covid19cz.app.ui.btenabled.BtEnabledVM
import cz.covid19cz.app.ui.btonboard.BtOnboardVM
import cz.covid19cz.app.ui.dbexplorer.DbExplorerVM
import cz.covid19cz.app.ui.help.HelpVM
import cz.covid19cz.app.ui.login.LoginVM
import cz.covid19cz.app.ui.main.MainVM
import cz.covid19cz.app.ui.sandbox.SandboxVM
import cz.covid19cz.app.ui.welcome.WelcomeVM
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel { MainVM() }
    viewModel { SandboxVM(get(), get(), get()) }
    viewModel { LoginVM(get(), get(), get()) }
    viewModel { WelcomeVM(get(), get(), get()) }
    viewModel { HelpVM() }
    viewModel { BtDisabledVM() }
    viewModel { BtEnabledVM() }
    viewModel { BtOnboardVM() }
    viewModel { DbExplorerVM(get()) }
}

val databaseModule = module {
    fun provideDatabase(application: Application): AppDatabase {
        return Room.databaseBuilder(application, AppDatabase::class.java, "database")
            .fallbackToDestructiveMigration()
            .build()
    }

    fun provideDao(database: AppDatabase): ScanResultsDao {
        return database.scanResultsDao
    }

    single { provideDatabase(androidApplication()) }
    single { provideDao(get()) }
    single { CsvExporter(get(), get()) }
}

val repositoryModule = module {
    fun provideDatabaseRepository(deviceDao: ScanResultsDao): DatabaseRepository {
        return ExpositionRepositoryImpl(deviceDao)
    }

    single { provideDatabaseRepository(get()) }
    single { BluetoothRepository(get(), get()) }
    single { SharedPrefsRepository(get()) }
}

val appModule = module {
    single { LocationStateReceiver() }
    single { BluetoothStateReceiver() }
    single { BatterSaverStateReceiver() }
    single { WakeLockManager(androidContext().getSystemService()) }
    single { androidContext().getSystemService<PowerManager>() }
}



val allModules = listOf(appModule, viewModelModule, databaseModule, repositoryModule)
