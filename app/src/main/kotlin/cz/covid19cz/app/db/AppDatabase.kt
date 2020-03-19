package cz.covid19cz.app.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [ScanResultEntity::class], version = 5, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    companion object{
        const val DATABASE_NAME = "database"
    }

    abstract val scanResultsDao: ScanResultsDao
}