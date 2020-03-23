package cz.covid19cz.app.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = ScanResultsDao.TABLE_NAME)
data class ScanResultEntity(
    @PrimaryKey(autoGenerate = true) val id: Long,
    val buid: String,
    val timestampStart: Long,
    val timestampEnd: Long,
    val rssiMax: Int,
    val rssiMed: Int,
    val rssiCount: Int
)