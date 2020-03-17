package cz.covid19cz.app.db

import androidx.room.Database
import androidx.room.RoomDatabase
import cz.covid19cz.app.repository.device.Device
import cz.covid19cz.app.repository.device.DeviceDao

@Database(entities = [Device::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract val deviceDao: DeviceDao
}