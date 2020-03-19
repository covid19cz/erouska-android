package cz.covid19cz.app.db

import androidx.lifecycle.LiveData
import androidx.room.*
import io.reactivex.Single

@Dao
interface ScanResultsDao {

    companion object{
        const val TABLE_NAME = "scan_results"
    }

    @Query("SELECT * FROM $TABLE_NAME")
    fun findAll(): LiveData<List<ScanResultEntity>>

    @Query("SELECT * FROM $TABLE_NAME")
    fun getAll(): Single<List<ScanResultEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(devices: List<ScanResultEntity>) : List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(devices: ScanResultEntity) : Long

    @Delete
    fun delete(device: ScanResultEntity)
}