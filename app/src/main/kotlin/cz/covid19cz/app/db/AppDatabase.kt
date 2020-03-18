package cz.covid19cz.app.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [ExpositionEntity::class], version = 3, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract val expositionDao: ExpositionDao
}