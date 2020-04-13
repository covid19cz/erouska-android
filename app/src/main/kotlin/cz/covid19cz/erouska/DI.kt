package cz.covid19cz.erouska

import android.app.Application
import android.bluetooth.BluetoothManager
import android.os.PowerManager
import androidx.core.content.getSystemService
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.room.Room
import cz.covid19cz.erouska.bt.BluetoothRepository
import cz.covid19cz.erouska.db.*
import cz.covid19cz.erouska.db.export.CsvExporter
import cz.covid19cz.erouska.receiver.BatterSaverStateReceiver
import cz.covid19cz.erouska.receiver.BluetoothStateReceiver
import cz.covid19cz.erouska.receiver.LocationStateReceiver
import cz.covid19cz.erouska.service.WakeLockManager
import cz.covid19cz.erouska.ui.about.AboutVM
import cz.covid19cz.erouska.ui.confirm.ConfirmationVM
import cz.covid19cz.erouska.ui.contacts.ContactsVM
import cz.covid19cz.erouska.ui.dashboard.DashboardVM
import cz.covid19cz.erouska.ui.help.BatteryOptimizationVM
import cz.covid19cz.erouska.ui.help.GuideVM
import cz.covid19cz.erouska.ui.help.HelpVM
import cz.covid19cz.erouska.ui.login.LoginVM
import cz.covid19cz.erouska.ui.main.MainVM
import cz.covid19cz.erouska.ui.mydata.MyDataVM
import cz.covid19cz.erouska.ui.permissions.PermissionDisabledVM
import cz.covid19cz.erouska.ui.permissions.onboarding.PermissionsOnboardingVM
import cz.covid19cz.erouska.ui.sandbox.SandboxVM
import cz.covid19cz.erouska.ui.success.SuccessVM
import cz.covid19cz.erouska.ui.welcome.WelcomeVM
import cz.covid19cz.erouska.utils.Markdown
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module


val viewModelModule = module {
    viewModel { MainVM() }
    viewModel { SandboxVM(get(), get()) }
    viewModel { LoginVM(get()) }
    viewModel { WelcomeVM(get(), get()) }
    viewModel { HelpVM() }
    viewModel { AboutVM() }
    viewModel { DashboardVM(get(), get()) }
    viewModel { PermissionsOnboardingVM(get(), get()) }
    viewModel { PermissionDisabledVM(get(), get()) }
    viewModel { ContactsVM() }
    viewModel { MyDataVM(get(), get()) }
    viewModel { ConfirmationVM(get(), get(), get()) }
    viewModel { SuccessVM() }
    viewModel { BatteryOptimizationVM() }
    viewModel { GuideVM() }
}

val databaseModule = module {
    fun provideDatabase(application: Application): AppDatabase {
        return Room.databaseBuilder(application, AppDatabase::class.java, "database")
            .fallbackToDestructiveMigration()
            .build()
    }

    fun provideDao(database: AppDatabase): ScanDataDao {
        return database.scanResultsDao
    }

    single { provideDatabase(androidApplication()) }
    single { provideDao(get()) }
    single { CsvExporter(get()) }
}

val repositoryModule = module {
    fun provideDatabaseRepository(deviceDao: ScanDataDao): DatabaseRepository {
        return ExpositionRepositoryImpl(deviceDao)
    }

    single { provideDatabaseRepository(get()) }
    single { BluetoothRepository(get(), get(), get(), get()) }
    single { SharedPrefsRepository(get()) }
}

val appModule = module {
    single { LocationStateReceiver(get()) }
    single { BluetoothStateReceiver() }
    single { BatterSaverStateReceiver() }
    single { LocalBroadcastManager.getInstance(androidApplication()) }
    single { WakeLockManager(androidContext().getSystemService()) }
    single { androidContext().getSystemService<PowerManager>() }
    single { androidContext().getSystemService<BluetoothManager>() }
    single { Markdown(androidContext()) }
}


val allModules = listOf(appModule, viewModelModule, databaseModule, repositoryModule)
