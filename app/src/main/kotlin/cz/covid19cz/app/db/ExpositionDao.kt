package cz.covid19cz.app.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ExpositionDao {

    companion object{
        const val TABLE_NAME = "exposition"
    }

    @Query("SELECT * FROM $TABLE_NAME")
    fun findAll(): LiveData<List<ExpositionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(devices: List<ExpositionEntity>) : List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(devices: ExpositionEntity) : Long

    @Delete
    fun delete(device: ExpositionEntity)
}