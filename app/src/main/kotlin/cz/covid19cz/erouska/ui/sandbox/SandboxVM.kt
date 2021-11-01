package cz.covid19cz.erouska.ui.sandbox

import android.util.Base64
import androidx.databinding.ObservableArrayList
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.viewModelScope
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.nearby.exposurenotification.TemporaryExposureKey
import cz.covid19cz.erouska.R
import cz.covid19cz.erouska.exposurenotifications.ExposureNotificationsRepository
import cz.covid19cz.erouska.ext.timestampToDateTime
import cz.covid19cz.erouska.net.ExposureServerRepository
import cz.covid19cz.erouska.net.model.DownloadedKeys
import cz.covid19cz.erouska.ui.base.BaseVM
import cz.covid19cz.erouska.ui.dashboard.event.GmsApiErrorEvent
import cz.covid19cz.erouska.ui.sandbox.event.SnackbarEvent
import cz.covid19cz.erouska.ui.verification.ReportExposureException
import cz.covid19cz.erouska.ui.verification.VerifyException
import cz.covid19cz.erouska.utils.L
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SandboxVM @Inject constructor(
    private val exposureNotificationsRepository: ExposureNotificationsRepository,
    private val serverRepository: ExposureServerRepository
) : BaseVM() {

    val filesString = MutableLiveData<String?>()
    val teks = ObservableArrayList<TemporaryExposureKey>()
    var downloadResult: List<DownloadedKeys>? = null
    val code = MutableLiveData("")

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun onCreate() {
        refreshTeks()
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
        val timeInMillis = (rollingStart.toLong() * 10 * 60 * 1000)
        return timeInMillis.timestampToDateTime()
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
                publish(GmsApiErrorEvent(it))
                L.e(it)
            }
        }
    }

    fun downloadKeyExport() {
        viewModelScope.launch {
            kotlin.runCatching {
                downloadResult = serverRepository.downloadKeyExport()
                L.d("files=${downloadResult}")
                return@runCatching downloadResult
            }.onSuccess {
                showSnackbar("Download success")
            }.onFailure {
                showSnackbar("Download failed: ${it.message}")
            }

        }
    }

    fun deleteKeys() {
        serverRepository.deleteFiles()
        filesString.value = null
    }

    fun provideDiagnosisKeys() {
        if (downloadResult != null) {
            viewModelScope.launch {
                runCatching {
                    exposureNotificationsRepository.provideDiagnosisKeys(downloadResult!!)
                }.onSuccess {
                    showSnackbar("Import success")
                }.onFailure {
                    showSnackbar("Import error: ${it.message}")
                    L.e(it)
                }
            }
        } else {
            showSnackbar("Download keys first")
        }
    }

    fun reportExposureWithVerification(code: String) {
        viewModelScope.launch {
            runCatching {
                exposureNotificationsRepository.verifyCode(code)
                exposureNotificationsRepository.publishKeys()
            }.onSuccess {
                showSnackbar("Upload success: $it keys")
            }.onFailure {
                when (it) {
                    is ApiException -> publish(GmsApiErrorEvent(it))
                    is VerifyException -> showSnackbar("Verification error: ${it.message}")
                    is ReportExposureException -> showSnackbar("Upload error: ${it.message}")
                    else -> showSnackbar("${it.message}")
                }
                L.e(it)
            }
        }
    }

    private fun showSnackbar(text: String) {
        publish(SnackbarEvent(text))
    }

    fun navigateToData() {
        navigate(R.id.nav_sandbox_data)
    }

    fun navigateToConfig() {
        navigate(R.id.nav_sandbox_config)
    }

}