package cz.covid19cz.app.repository.device

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface DeviceDao {

    @Query("SELECT * FROM devices")
    fun findAll(): LiveData<List<Device>>

    @Query("SELECT * FROM devices WHERE deviceId=:id")
    fun findById(id: Int): LiveData<Device>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(devices: List<Device>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(devices: Device)

    @Query("SELECT * FROM devices WHERE deviceId IN (:deviceIds)")
    fun findAllByIds(deviceIds: IntArray): List<Device>

    @Delete
    fun delete(device: Device)
}