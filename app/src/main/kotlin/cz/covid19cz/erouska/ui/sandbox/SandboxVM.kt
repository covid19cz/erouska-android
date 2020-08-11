package cz.covid19cz.erouska.ui.sandbox

import androidx.databinding.ObservableArrayList
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.viewModelScope
import arch.livedata.SafeMutableLiveData
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.nearby.exposurenotification.TemporaryExposureKey
import cz.covid19cz.erouska.db.SharedPrefsRepository
import cz.covid19cz.erouska.exposurenotifications.ExposureNotificationsRepository
import cz.covid19cz.erouska.ext.asHexLower
import cz.covid19cz.erouska.net.ExposureServerRepository
import cz.covid19cz.erouska.net.model.ExposureRequest
import cz.covid19cz.erouska.net.model.TemporaryExposureKeyDto
import cz.covid19cz.erouska.ui.base.BaseVM
import cz.covid19cz.erouska.ui.dashboard.event.GmsApiErrorEvent
import cz.covid19cz.erouska.utils.L
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class SandboxVM(
    val exposureNotificationsRepository: ExposureNotificationsRepository,
    private val serverRepository: ExposureServerRepository,
    val prefs : SharedPrefsRepository
) : BaseVM() {

    val teks = ObservableArrayList<TemporaryExposureKey>()
    val serviceRunning = SafeMutableLiveData(false)

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun onCreate(){

    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onResume(){
        viewModelScope.launch {
            kotlin.runCatching {
                exposureNotificationsRepository.isEnabled()
            }.onSuccess {
                L.d("Exposure Notifications enabled $it")
                serviceRunning.value = it
            }.onFailure {
                L.e(it)
            }
        }
    }

    fun getExposureWindows(){
        viewModelScope.launch {
            kotlin.runCatching {
                exposureNotificationsRepository.getExposureWindows()
            }.onSuccess {
                L.d("success")
            }.onFailure {
                L.e("failed")
            }
        }
    }

    fun tekToString(tek : TemporaryExposureKey) : String{
        return tek.keyData.asHexLower
    }

    fun reportTypeToString(reportType : Int) : String{
        return when(reportType){
            0 -> "UNKNOWN"
            1 -> "CONFIRMED_TEST"
            2 -> "CONFIRMED_CLINICAL_DIAGNOSIS"
            3 -> "SELF_REPORT"
            4 -> "RECURSIVE"
            5 -> "REVOKED"
            else -> reportType.toString()
        }
    }

    fun rollingStartToString(rollingStart : Int) : String{
        val formatter = SimpleDateFormat("d.M.yyyy H:mm", Locale.getDefault())
        formatter.timeZone = TimeZone.getTimeZone("UTC")
        val dateTime = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            timeInMillis = (rollingStart.toLong() * 10 * 60 * 1000)
        }
        return formatter.format(dateTime.time)
    }

    fun rollingIntervalToString(rollingInterval : Int) : String{
        return "${(rollingInterval * 10) / 60}h"
    }

    fun refreshTeks(){
        teks.clear()
        viewModelScope.launch {
            kotlin.runCatching {
                exposureNotificationsRepository.getTemporaryExposureKeyHistory()
            }.onSuccess {
                teks.addAll(it.sortedByDescending { it.rollingStartIntervalNumber })
            }.onFailure {
                if (it is ApiException){
                    publish(GmsApiErrorEvent(it.status))
                }
                L.e(it)
            }
        }
    }

    fun downloadKeyExport() {
        viewModelScope.launch {
            val files = serverRepository.downloadKeyExport()
            L.d("files=$files")
        }
    }

    fun provideDiagnosisKeys(){
        viewModelScope.launch {
            runCatching {
                val files = serverRepository.downloadKeyExport()
                exposureNotificationsRepository.provideDiagnosisKeys(files)
            }
        }
    }

    fun reportExposure(){
        viewModelScope.launch {
            runCatching {
                val keys = exposureNotificationsRepository.getTemporaryExposureKeyHistory()
                val request = ExposureRequest(keys.map { TemporaryExposureKeyDto(it.keyData.toString(), it.rollingStartIntervalNumber, it.rollingPeriod, it.transmissionRiskLevel) }, null, null, null, null)
                serverRepository.reportExposure(request)
            }.onSuccess {
                L.d("success")
            }.onFailure {
                if (it is ApiException){
                    publish(GmsApiErrorEvent(it.status))
                }
                L.e(it)
            }
        }
    }

}