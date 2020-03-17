package cz.covid19cz.app.repository.device

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface DeviceDao {

    @Query("SELECT * FROM devices")
    fun findAll(): LiveData<List<Device>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun add(devices: List<Device>)
}