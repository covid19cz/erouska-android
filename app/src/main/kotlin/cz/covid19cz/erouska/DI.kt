package cz.covid19cz.erouska

import android.app.AlarmManager
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.os.PowerManager
import androidx.core.content.getSystemService
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.nearby.Nearby
import cz.covid19cz.erouska.db.SharedPrefsRepository
import cz.covid19cz.erouska.exposurenotifications.ExposureCryptoTools
import cz.covid19cz.erouska.exposurenotifications.ExposureNotificationsRepository
import cz.covid19cz.erouska.net.ExposureServerRepository
import cz.covid19cz.erouska.net.FirebaseFunctionsRepository
import cz.covid19cz.erouska.ui.about.AboutVM
import cz.covid19cz.erouska.ui.activation.ActivationNotificationsVM
import cz.covid19cz.erouska.ui.activation.ActivationVM
import cz.covid19cz.erouska.ui.contacts.ContactsVM
import cz.covid19cz.erouska.ui.dashboard.DashboardVM
import cz.covid19cz.erouska.ui.exposure.ExposuresVM
import cz.covid19cz.erouska.ui.exposure.MainSymptomsVM
import cz.covid19cz.erouska.ui.exposure.RecentExposuresVM
import cz.covid19cz.erouska.ui.exposure.SpreadPreventionVM
import cz.covid19cz.erouska.ui.help.HelpVM
import cz.covid19cz.erouska.ui.main.MainVM
import cz.covid19cz.erouska.ui.mydata.MyDataVM
import cz.covid19cz.erouska.ui.permissions.PermissionDisabledVM
import cz.covid19cz.erouska.ui.permissions.onboarding.PermissionsOnboardingVM
import cz.covid19cz.erouska.ui.sandbox.SandboxConfigVM
import cz.covid19cz.erouska.ui.sandbox.SandboxDataVM
import cz.covid19cz.erouska.ui.sandbox.SandboxVM
import cz.covid19cz.erouska.ui.senddata.SendDataVM
import cz.covid19cz.erouska.ui.update.legacy.LegacyUpdateVM
import cz.covid19cz.erouska.ui.update.playservices.UpdatePlayServicesVM
import cz.covid19cz.erouska.ui.welcome.WelcomeVM
import cz.covid19cz.erouska.utils.CustomTabHelper
import cz.covid19cz.erouska.utils.DeviceInfo
import cz.covid19cz.erouska.utils.Markdown
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel { MainVM() }
    viewModel { SandboxVM(get(), get(), get()) }
    viewModel { SandboxConfigVM(get()) }
    viewModel { SandboxDataVM(get(), get()) }
    viewModel { ActivationVM(get(), get()) }
    viewModel { WelcomeVM(get(), get(), get()) }
    viewModel { HelpVM() }
    viewModel { AboutVM() }
    viewModel { DashboardVM(get(), get(), get()) }
    viewModel { PermissionsOnboardingVM(get(), get()) }
    viewModel { PermissionDisabledVM(get(), get()) }
    viewModel { ContactsVM() }
    viewModel { MyDataVM(get(), get()) }
    viewModel { SendDataVM(get()) }
    viewModel { ExposuresVM(get()) }
    viewModel { RecentExposuresVM() }
    viewModel { MainSymptomsVM() }
    viewModel { SpreadPreventionVM() }
    viewModel { LegacyUpdateVM(get()) }
    viewModel { UpdatePlayServicesVM() }
    viewModel { ActivationNotificationsVM(get(), get()) }
}

val databaseModule = module {

}

val repositoryModule = module {
    single { SharedPrefsRepository(get()) }
    single { ExposureNotificationsRepository(Nearby.getExposureNotificationClient(androidContext()), get(), get(), get(), get()) }
    single { FirebaseFunctionsRepository(get(), get()) }
    single { ExposureServerRepository(get(), get()) }
}

val appModule = module {
    single { LocalBroadcastManager.getInstance(androidApplication()) }
    single { androidContext().getSystemService<PowerManager>() }
    single { androidContext().getSystemService<BluetoothManager>() }
    single { androidContext().getSystemService<AlarmManager>() }
    single { Markdown(androidContext()) }
    single { DeviceInfo(androidContext()) }
    single { CustomTabHelper(androidContext()) }
    single { BluetoothAdapter.getDefaultAdapter() }
    single { ExposureCryptoTools() }
}

val allModules = listOf(appModule, viewModelModule, databaseModule, repositoryModule)
