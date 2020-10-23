package cz.covid19cz.erouska.ui.recentexposures

import androidx.databinding.ObservableArrayList
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.viewModelScope
import arch.adapter.RecyclerLayoutStrategy
import arch.viewmodel.BaseArchViewModel
import cz.covid19cz.erouska.R
import cz.covid19cz.erouska.db.DailySummariesDb
import cz.covid19cz.erouska.db.DailySummaryEntity
import cz.covid19cz.erouska.exposurenotifications.ExposureNotificationsRepository
import cz.covid19cz.erouska.ui.recentexposures.entity.RecentExposureGroupHeaderItem
import cz.covid19cz.erouska.utils.L
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.threeten.bp.LocalDate
import java.lang.IllegalArgumentException

class RecentExposuresVM @ViewModelInject constructor(
    private val exposureNotificationsRepo: ExposureNotificationsRepository,
    private val db: DailySummariesDb
) : BaseArchViewModel() {

    val layoutStrategy = object: RecyclerLayoutStrategy{
        override fun getLayoutId(item: Any): Int {
            return when(item){
                is RecentExposureGroupHeaderItem -> R.layout.item_recent_exposure_group_header
                is DailySummaryEntity -> R.layout.item_recent_exposure
                else -> throw IllegalArgumentException("Missing layout mapping")
            }
        }
    }

    val items = ObservableArrayList<Any>()

    fun loadExposures(demo: Boolean = false) {
        items.clear()
        if (!demo) {
            viewModelScope.launch(Dispatchers.IO) {
                kotlin.runCatching {
                    exposureNotificationsRepo.getDailySummariesFromDbByImportDate()
                }.onSuccess { dailySummaries ->
                    viewModelScope.launch(Dispatchers.Main) {
                        var previousTimestamp = -1L
                        dailySummaries.forEachIndexed { index, item ->
                            if (previousTimestamp != item.importTimestamp){
                                items.add(RecentExposureGroupHeaderItem(item.importTimestamp))
                                previousTimestamp = item.importTimestamp
                            }
                            items.add(item)
                        }
                    }
                }.onFailure {
                    L.e(it)
                }
            }
        } else {
            val now = LocalDate.now()
            items.addAll(
                listOf(
                    // old exposures
                    DailySummaryEntity(
                        LocalDate.of(2019, 12, 28).toEpochDay().toInt(),
                        1000.0,
                        1000.0,
                        1000.0,
                        0,
                        false,
                        false
                    ),
                    DailySummaryEntity(
                        LocalDate.of(2019, 3, 20).toEpochDay().toInt(),
                        1000.0,
                        1000.0,
                        1000.0,
                        0,
                        false,
                        false
                    ),
                    DailySummaryEntity(
                        LocalDate.of(2019, 5, 14).toEpochDay().toInt(),
                        1000.0,
                        1000.0,
                        1000.0,
                        0,
                        false,
                        false
                    ),
                    DailySummaryEntity(
                        LocalDate.of(2019, 8, 5).toEpochDay().toInt(),
                        1000.0,
                        1000.0,
                        1000.0,
                        0,
                        false,
                        false
                    ),
                    // very recent exposures
                    DailySummaryEntity(
                        now.minusDays(10).toEpochDay().toInt(),
                        1000.0,
                        1000.0,
                        1000.0,
                        0,
                        false,
                        false
                    ),
                    DailySummaryEntity(
                        now.minusDays(5).toEpochDay().toInt(),
                        1000.0,
                        1000.0,
                        1000.0,
                        0,
                        false,
                        false
                    ),
                    DailySummaryEntity(
                        now.minusDays(12).toEpochDay().toInt(),
                        1000.0,
                        1000.0,
                        1000.0,
                        0,
                        false,
                        false
                    )
                )
            )
        }
    }
}
