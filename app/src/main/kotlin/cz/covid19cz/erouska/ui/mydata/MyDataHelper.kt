package cz.covid19cz.erouska.ui.mydata

import arch.livedata.SafeMutableLiveData
import cz.covid19cz.erouska.ui.mydata.MyDataVM.Companion.LAST_UPDATE_API_FORMAT
import cz.covid19cz.erouska.ui.mydata.MyDataVM.Companion.LAST_UPDATE_UI_FORMAT
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Function takes 3 arguments (total rate, increase rate and date) and distributes them through LiveData.
 * Then it passes them through provided functions.
 */
inline fun <T1 : Any, T2 : Any, T3 : String, S1 : SafeMutableLiveData<T1>, S2 : SafeMutableLiveData<T2>, S3 : SafeMutableLiveData<String>> safeLetAndSet(
    responseTotal: T1?,
    responseIncrease: T2?,
    responseIncreaseDate: T3?,
    liveTotal: S1,
    liveIncrease: S2,
    liveIncreaseDate: S3,
    funTotal: (T1) -> Unit,
    funIncrease: (T2) -> Unit,
    funIncreaseDate: (Long) -> Unit
) {
    if (responseTotal != null && responseIncrease != null && responseIncreaseDate != null) {
        liveTotal.value = responseTotal
        liveIncrease.value = responseIncrease

        funTotal(responseTotal)
        funIncrease(responseIncrease)

        val lastUpdateDate = SimpleDateFormat(
            LAST_UPDATE_API_FORMAT,
            Locale.getDefault()
        ).parse(responseIncreaseDate)

        lastUpdateDate?.time?.let { lastUpdateMillis ->
            funIncreaseDate(lastUpdateMillis)

            liveIncreaseDate.value = SimpleDateFormat(
                LAST_UPDATE_UI_FORMAT,
                Locale.getDefault()
            ).format(
                Date(lastUpdateMillis)
            )
        }
    }
}

/**
 * Function takes 2 arguments (total rate, increase rate) and distributes them through LiveData.
 * Then it passes them through provided functions.
 */
inline fun <T1 : Any, T2 : Any, S1 : SafeMutableLiveData<T1>, S2 : SafeMutableLiveData<T2>> safeLetAndSet(
    responseTotal: T1?,
    responseIncrease: T2?,
    liveTotal: S1,
    liveIncrease: S2,
    prefTotal: (T1) -> Unit,
    prefIncrease: (T2) -> Unit
) {
    if (responseTotal != null && responseIncrease != null) {
        liveTotal.value = responseTotal
        liveIncrease.value = responseIncrease

        prefTotal(responseTotal)
        prefIncrease(responseIncrease)
    }
}

/**
 * Function takes 1 argument (total rate) and distributes it through LiveData.
 * Then it passes it through provided functions.
 */
inline fun <T1 : Any, S1 : SafeMutableLiveData<T1>> safeLetAndSet(
    responseTotal: T1?,
    liveTotal: S1,
    funTotal: (T1) -> Unit
) {
    if (responseTotal != null) {
        liveTotal.value = responseTotal
        funTotal(responseTotal)
    }
}

/**
 * Function takes 1 argument (last stats update date) and distributes it through LiveData.
 * Then it passes it through provided functions.
 */
inline fun safeLetAndSetStatsDate(responseDate: String?, funLastUpdateDate: (Long) -> Unit) {
    if (responseDate != null) {
        val lastStatsUpdate = SimpleDateFormat(
            LAST_UPDATE_API_FORMAT,
            Locale.getDefault()
        ).parse(responseDate)

        lastStatsUpdate?.time?.let { lastUpdateMillis ->
            funLastUpdateDate(lastUpdateMillis)
        }
    }
}

/**
 * Function takes 1 argument (last metrics update date) and distributes it through LiveData.
 * Then it passes it through provided functions.
 */
inline fun <S1 : SafeMutableLiveData<String>> safeLetAndSetMetricsDate(responseDate: String?, liveIncreaseDate: S1, funLastUpdateDate: (Long) -> Unit) {
    if (responseDate != null) {
        val lastStatsUpdate = SimpleDateFormat(
            LAST_UPDATE_API_FORMAT,
            Locale.getDefault()
        ).parse(responseDate)

        lastStatsUpdate?.time?.let { lastUpdateMillis ->
            funLastUpdateDate(lastUpdateMillis)

            val lastMetricsIncreaseMillis = lastUpdateMillis - TimeUnit.DAYS.toMillis(1) // increase day = day before day of last update

            liveIncreaseDate.value = SimpleDateFormat(
                LAST_UPDATE_UI_FORMAT,
                Locale.getDefault()
            ).format(
                Date(lastMetricsIncreaseMillis)
            )
        }
    }
}

/**
 * Function takes 1 argument (total rate) and distributes it through LiveData.
 * Then it passes it through provided function.
 */
inline fun setTotal(funTotal: () -> Int): SafeMutableLiveData<Int> {
    return SafeMutableLiveData(funTotal())
}

/**
 * Function takes 1 argument (increase rate) and distributes it through LiveData.
 * Then it passes it through provided function.
 */
inline fun setIncrease(funIncrease: () -> Int): SafeMutableLiveData<Int> {
    return SafeMutableLiveData(funIncrease())
}

/**
 * Function takes 1 argument (increase date), formats it and distributes it through LiveData.
 * Then it passes it through provided function.
 */
inline fun setIncreaseDate(funIncreaseDate: () -> Long): SafeMutableLiveData<String> {
    return if (funIncreaseDate() == 0L) {
        SafeMutableLiveData("-")
    } else {
        SafeMutableLiveData(
            SimpleDateFormat(
                LAST_UPDATE_UI_FORMAT,
                Locale.getDefault()
            ).format(Date(funIncreaseDate()))
        )
    }
}

/**
 * Function takes 1 argument (last update date), formats it and distributes it through LiveData.
 * Then it passes it through provided function.
 */
inline fun setLastMetricsUpdateDate(funLastUpdateDate: () -> Long): SafeMutableLiveData<String> {
    return if (funLastUpdateDate() == 0L) {
        SafeMutableLiveData("-")
    } else {
        SafeMutableLiveData(
            SimpleDateFormat(
                LAST_UPDATE_UI_FORMAT,
                Locale.getDefault()
            ).format(Date(funLastUpdateDate() - TimeUnit.DAYS.toMillis(1))) // increase day = day before day of last update
        )
    }
}