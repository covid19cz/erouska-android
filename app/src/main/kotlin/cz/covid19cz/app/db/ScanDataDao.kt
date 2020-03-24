package cz.covid19cz.app.db

import androidx.lifecycle.LiveData
import androidx.room.*
import io.reactivex.Single

@Dao
interface ScanDataDao {

    companion object{
        const val TABLE_NAME = "scan_data"
    }

    @Query("SELECT * FROM $TABLE_NAME")
    fun findAll(): LiveData<List<ScanDataEntity>>

    @Query("SELECT * FROM $TABLE_NAME")
    fun getAll(): Single<List<ScanDataEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(devices: List<ScanDataEntity>) : List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(devices: ScanDataEntity) : Long

    @Delete
    fun delete(device: ScanDataEntity)

    @Query("DELETE FROM $TABLE_NAME")
    fun clear()

    @Query("SELECT count(DISTINCT buid) FROM $TABLE_NAME WHERE timestampEnd > :since")
    fun getDistinctCount(since: Long) : Single<Int>

    @Query("SELECT * FROM (SELECT buid, sum(timestampEnd - timestampStart) AS expositionTime FROM $TABLE_NAME WHERE timestampEnd > :since AND rssiMed >= :criticalRssi GROUP BY buid) WHERE expositionTime > (:criticalMinutes*60*1000)")
    fun getCritical(since: Long, criticalRssi : Int, criticalMinutes : Int) : Single<List<ExpositionEntity>>
}