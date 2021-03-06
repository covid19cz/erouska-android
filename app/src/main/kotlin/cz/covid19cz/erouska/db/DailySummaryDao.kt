package cz.covid19cz.erouska.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import cz.covid19cz.erouska.AppConfig
import java.util.concurrent.TimeUnit

@Dao
interface DailySummaryDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(entity : List<DailySummaryEntity>)

    @Query("SELECT * FROM daily_summaries ORDER BY days_since_epoch DESC LIMIT 1")
    suspend fun getLatest() : List<DailySummaryEntity>

    @Query("SELECT * FROM daily_summaries WHERE notified == 1 ORDER BY days_since_epoch DESC LIMIT 1")
    suspend fun getLastNotified() : List<DailySummaryEntity>

    @Query("SELECT * FROM daily_summaries ORDER BY days_since_epoch DESC")
    suspend fun getAllByExposureDate() : List<DailySummaryEntity>

    @Query("SELECT * FROM daily_summaries ORDER BY import_timestamp DESC, days_since_epoch DESC")
    suspend fun getAllByImportDate() : List<DailySummaryEntity>

    @Query("UPDATE daily_summaries SET notified = 1")
    suspend fun markAsNotified()

    @Query("UPDATE daily_summaries SET accepted = 1")
    suspend fun markAsAccepted()

    @Query("DELETE FROM daily_summaries WHERE days_since_epoch < :beforeDaysSinceEpoch")
    suspend fun deleteOld(beforeDaysSinceEpoch : Long = TimeUnit.MILLISECONDS.toDays (System.currentTimeMillis()) - AppConfig.dbCleanupDays)
}