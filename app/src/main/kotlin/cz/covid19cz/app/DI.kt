package cz.covid19cz.app

import android.app.Application
import android.bluetooth.BluetoothManager
import android.os.PowerManager
import androidx.core.content.getSystemService
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.room.Room
import com.google.firebase.analytics.FirebaseAnalytics
import cz.covid19cz.app.bt.BluetoothRepository
import cz.covid19cz.app.db.*
import cz.covid19cz.app.db.export.CsvExporter
import cz.covid19cz.app.receiver.BatterSaverStateReceiver
import cz.covid19cz.app.receiver.BluetoothStateReceiver
import cz.covid19cz.app.receiver.LocationStateReceiver
import cz.covid19cz.app.receiver.ScreenStateReceiver
import cz.covid19cz.app.service.WakeLockManager
import cz.covid19cz.app.ui.confirm.ConfirmationVM
import cz.covid19cz.app.ui.contacts.ContactsVM
import cz.covid19cz.app.ui.dashboard.DashboardVM
import cz.covid19cz.app.ui.help.HelpVM
import cz.covid19cz.app.ui.login.LoginVM
import cz.covid19cz.app.ui.main.MainVM
import cz.covid19cz.app.ui.mydata.MyDataVM
import cz.covid19cz.app.ui.permissions.PermissionDisabledVM
import cz.covid19cz.app.ui.permissions.onboarding.PermissionsOnboardingVM
import cz.covid19cz.app.ui.sandbox.SandboxVM
import cz.covid19cz.app.ui.welcome.WelcomeVM
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module


val viewModelModule = module {
    viewModel { MainVM(get()) }
    viewModel { SandboxVM(get(), get(), get()) }
    viewModel { LoginVM(get()) }
    viewModel { WelcomeVM(get(), get()) }
    viewModel { HelpVM() }
    viewModel { PermissionDisabledVM(get(), get()) }
    viewModel { DashboardVM(get(), get()) }
    viewModel { ContactsVM() }
    viewModel { PermissionsOnboardingVM(get(), get()) }
    viewModel { MyDataVM(get(), get()) }
    viewModel { ConfirmationVM(get(), get(), get()) }
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
    single { CsvExporter(get(), get()) }
}

val repositoryModule = module {
    fun provideDatabaseRepository(deviceDao: ScanDataDao): DatabaseRepository {
        return ExpositionRepositoryImpl(deviceDao)
    }

    single { provideDatabaseRepository(get()) }
    single { BluetoothRepository(get(), get(), get()) }
    single { SharedPrefsRepository(get()) }
}

val appModule = module {
    single { LocationStateReceiver() }
    single { BluetoothStateReceiver() }
    single { ScreenStateReceiver() }
    single { BatterSaverStateReceiver() }
    single { FirebaseAnalytics.getInstance(androidApplication()) }
    single { LocalBroadcastManager.getInstance(androidApplication()) }
    single { WakeLockManager(androidContext().getSystemService()) }
    single { androidContext().getSystemService<PowerManager>() }
    single { androidContext().getSystemService<BluetoothManager>() }
}


val allModules = listOf(appModule, viewModelModule, databaseModule, repositoryModule)
