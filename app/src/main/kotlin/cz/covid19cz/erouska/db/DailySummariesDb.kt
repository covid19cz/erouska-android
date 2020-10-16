package cz.covid19cz.erouska.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(version = 1, entities = [DailySummaryEntity::class], exportSchema = false)
abstract class DailySummariesDb : RoomDatabase(){
    abstract fun dao(): DailySummaryDao
}