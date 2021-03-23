package cz.covid19cz.erouska.ui.mydata

import arch.livedata.SafeMutableLiveData
import cz.covid19cz.erouska.ui.mydata.MyDataVM.Companion.LAST_UPDATE_API_FORMAT
import cz.covid19cz.erouska.ui.mydata.MyDataVM.Companion.LAST_UPDATE_UI_FORMAT
import java.text.SimpleDateFormat
import java.util.*

inline fun <T1: Int, T2: Int, T3: String, S1: SafeMutableLiveData<Int>, S2: SafeMutableLiveData<Int>, S3: SafeMutableLiveData<String>> safeLetAndSet(responseTotal: T1?, responseIncrease: T2?, responseIncreaseDate: T3?, liveTotal: S1, liveIncrease: S2, liveIncreaseDate: S3, prefTotal: (T1) -> Unit, prefIncrease: (T2) -> Unit, prefIncreaseDate: (Long) -> Unit) {
    if (responseTotal != null && responseIncrease != null && responseIncreaseDate != null) {
        liveTotal.value = responseTotal
        liveIncrease.value = responseIncrease

        prefTotal(responseTotal)
        prefIncrease(responseIncrease)

        val lastUpdateDate = SimpleDateFormat(
            LAST_UPDATE_API_FORMAT,
            Locale.getDefault()
        ).parse(responseIncreaseDate)

        lastUpdateDate?.time?.let { lastUpdateMillis ->
            prefIncreaseDate(lastUpdateMillis)

            liveIncreaseDate.value = SimpleDateFormat(
                LAST_UPDATE_UI_FORMAT,
                Locale.getDefault()
            ).format(
                Date(lastUpdateMillis)
            )
        }
    }
}
inline fun <T1: Int, T2: Int, S1: SafeMutableLiveData<Int>, S2: SafeMutableLiveData<Int>> safeLetAndSet(responseTotal: T1?, responseIncrease: T2?, liveTotal: S1, liveIncrease: S2, prefTotal: (T1) -> Unit, prefIncrease: (T2) -> Unit) {
    if (responseTotal != null && responseIncrease != null) {
        liveTotal.value = responseTotal
        liveIncrease.value = responseIncrease

        prefTotal(responseTotal)
        prefIncrease(responseIncrease)
    }
}

inline fun <T1: Int, S1: SafeMutableLiveData<Int>> safeLetAndSet(responseTotal: T1?, liveTotal: S1, prefTotal: (T1) -> Unit) {
    if (responseTotal != null) {
        liveTotal.value = responseTotal
        prefTotal(responseTotal)
    }
}
inline fun <T1: String> safeLetAndSet(responseDate: T1?, prefDate: (Long) -> Unit) {
    if (responseDate != null) {
        val lastStatsUpdate = SimpleDateFormat(
            LAST_UPDATE_API_FORMAT,
            Locale.getDefault()
        ).parse(responseDate)

        lastStatsUpdate?.time?.let { lastUpdateMillis ->
            prefDate(lastUpdateMillis)
        }
    }
}

inline fun setTotal(getPref: () -> Int) : SafeMutableLiveData<Int>{
    return SafeMutableLiveData(getPref())
}

inline fun setIncrease(getPref: () -> Int) : SafeMutableLiveData<Int>{
    return SafeMutableLiveData(getPref())
}

inline fun setIncreaseDate(getPref: () -> Long) : SafeMutableLiveData<String>{
    return if (getPref() == 0L) {
        SafeMutableLiveData("-")
    } else {
        SafeMutableLiveData(
            SimpleDateFormat(
                LAST_UPDATE_UI_FORMAT,
                Locale.getDefault()
            ).format(Date(getPref()))
        )
    }
}