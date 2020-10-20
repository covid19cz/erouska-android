package cz.covid19cz.erouska.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import cz.covid19cz.erouska.ext.daysSinceEpochToDateString

@Entity(tableName = "daily_summaries")
data class DailySummaryEntity(
    @ColumnInfo(name = "days_since_epoch")
    @PrimaryKey val daysSinceEpoch: Int,
    @ColumnInfo(name = "maximum_score")
    val maximumScore: Double,
    @ColumnInfo(name = "score_sum")
    val scoreSum: Double,
    @ColumnInfo(name = "weightened_duration_sum")
    val weightenedDurationSum: Double,
    @ColumnInfo(name = "import_timestamp")
    val importTimestamp: Long,
    @ColumnInfo(name = "notified")
    val notified: Boolean,
    @ColumnInfo(name = "accepted")
    val accepted: Boolean
){

    fun getDateString() : String{
        return daysSinceEpoch.daysSinceEpochToDateString()
    }
}