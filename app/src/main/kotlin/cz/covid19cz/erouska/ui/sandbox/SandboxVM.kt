package cz.covid19cz.erouska.ui.sandbox

import android.util.Base64
import androidx.databinding.ObservableArrayList
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.nearby.exposurenotification.TemporaryExposureKey
import cz.covid19cz.erouska.R
import cz.covid19cz.erouska.db.SharedPrefsRepository
import cz.covid19cz.erouska.exposurenotifications.ExposureCryptoTools
import cz.covid19cz.erouska.exposurenotifications.ExposureNotificationsRepository
import cz.covid19cz.erouska.net.ExposureServerRepository
import cz.covid19cz.erouska.net.model.ExposureRequest
import cz.covid19cz.erouska.net.model.TemporaryExposureKeyDto
import cz.covid19cz.erouska.ui.base.BaseVM
import cz.covid19cz.erouska.ui.dashboard.event.GmsApiErrorEvent
import cz.covid19cz.erouska.ui.sandbox.event.SnackbarEvent
import cz.covid19cz.erouska.utils.L
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class SandboxVM(
    val exposureNotificationsRepository: ExposureNotificationsRepository,
    private val serverRepository: ExposureServerRepository,
    private val cryptoTools: ExposureCryptoTools,
    private val prefs: SharedPrefsRepository
) : BaseVM() {

    val filesString = MutableLiveData<String>()
    val lastDownload = MutableLiveData<String>()
    val teks = ObservableArrayList<TemporaryExposureKey>()
    var files = ArrayList<File>()

    init {
        lastDownload.value = prefs.lastKeyExportFileName()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun onCreate() {
        refreshTeks()
    }

    fun getExposureWindows() {
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

    fun tekToString(tek: TemporaryExposureKey): String {
        return Base64.encodeToString(tek.keyData, Base64.NO_WRAP)
    }

    fun reportTypeToString(reportType: Int): String {
        return when (reportType) {
            0 -> "UNKNOWN"
            1 -> "CONFIRMED_TEST"
            2 -> "CONFIRMED_CLINICAL_DIAGNOSIS"
            3 -> "SELF_REPORT"
            4 -> "RECURSIVE"
            5 -> "REVOKED"
            else -> reportType.toString()
        }
    }

    fun rollingStartToString(rollingStart: Int): String {
        val formatter = SimpleDateFormat("d.M.yyyy H:mm", Locale.getDefault())
        formatter.timeZone = TimeZone.getTimeZone("UTC")
        val dateTime = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            timeInMillis = (rollingStart.toLong() * 10 * 60 * 1000)
        }
        return formatter.format(dateTime.time)
    }

    fun rollingIntervalToString(rollingInterval: Int): String {
        return "${(rollingInterval * 10) / 60}h"
    }

    fun refreshTeks() {
        teks.clear()
        viewModelScope.launch {
            kotlin.runCatching {
                exposureNotificationsRepository.getTemporaryExposureKeyHistory()
            }.onSuccess {
                teks.addAll(it.sortedByDescending { it.rollingStartIntervalNumber })
            }.onFailure {
                if (it is ApiException) {
                    publish(GmsApiErrorEvent(it.status))
                }
                L.e(it)
            }
        }
    }

    fun downloadKeyExport() {
        viewModelScope.launch {
            kotlin.runCatching {
                files.clear()
                files.addAll(serverRepository.downloadKeyExport())

                L.d("files=${files}")
                return@runCatching files.size
            }.onSuccess {
                lastDownload.value = prefs.lastKeyExportFileName()
                filesString.value = files.joinToString(separator = "\n", transform = { it.name })
                showSnackbar("Download success: $it files")
            }.onFailure {
                showSnackbar("Download failed: ${it.message}")
            }

        }
    }

    fun deleteKeys() {
        serverRepository.deleteFiles()
        lastDownload.value = null
        filesString.value = null
    }

    fun provideDiagnosisKeys() {
        viewModelScope.launch {
            runCatching {
                exposureNotificationsRepository.provideDiagnosisKeys(files)
            }.onSuccess {
                showSnackbar("Import success")
            }.onFailure {
                showSnackbar("Import error: ${it.message}")
                L.e(it)
            }
        }
    }

    fun reportExposure() {
        viewModelScope.launch {
            runCatching {
                val keys = exposureNotificationsRepository.getTemporaryExposureKeyHistory()
                val request = ExposureRequest(keys.map {
                    TemporaryExposureKeyDto(
                        Base64.encodeToString(
                            it.keyData,
                            Base64.NO_WRAP
                        ), it.rollingStartIntervalNumber, it.rollingPeriod
                    )
                }, null, null, null, null)
                serverRepository.reportExposure(request)
            }.onSuccess {
                showSnackbar("Upload success: ${it.insertedExposures ?: 0} keys")
            }.onFailure {
                if (it is ApiException) {
                    publish(GmsApiErrorEvent(it.status))
                } else {
                    showSnackbar("Upload error: ${it.message}")
                }
                L.e(it)
            }
        }
    }

    private fun showSnackbar(text : String){
        publish(SnackbarEvent(text))
    }

}