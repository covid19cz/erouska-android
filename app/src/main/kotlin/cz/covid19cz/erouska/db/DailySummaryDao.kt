package cz.covid19cz.erouska.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlin.time.days

@Dao
interface DailySummaryDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(entity : List<DailySummaryEntity>)

    @Query("SELECT * FROM daily_summaries ORDER BY days_since_epoch DESC LIMIT 1")
    suspend fun getLatest() : List<DailySummaryEntity>

    @Query("SELECT * FROM daily_summaries WHERE notified == 1 ORDER BY days_since_epoch DESC LIMIT 1")
    suspend fun getLastNotified() : List<DailySummaryEntity>

    @Query("SELECT * FROM daily_summaries ORDER BY days_since_epoch DESC")
    suspend fun getAll() : List<DailySummaryEntity>

    @Query("UPDATE daily_summaries SET notified = 1")
    suspend fun markAsNotified()

    @Query("UPDATE daily_summaries SET accepted = 1")
    suspend fun markAsAccepted()

    @Query("DELETE FROM daily_summaries WHERE days_since_epoch < :beforeDaysSinceEpoch")
    suspend fun deleteOld(beforeDaysSinceEpoch : Long = (System.currentTimeMillis()/1000/60/60/24) - 14)
}