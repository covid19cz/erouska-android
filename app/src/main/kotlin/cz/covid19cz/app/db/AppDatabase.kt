package cz.covid19cz.app.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [ScanDataEntity::class], version = 7, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    companion object{
        const val DATABASE_NAME = "database"
    }

    abstract val scanResultsDao: ScanDataDao
}