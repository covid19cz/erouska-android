package cz.covid19cz.app.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exposition")
data class ExpositionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long,
    val buid: String,
    val timestampStart: Long,
    val timestampEnd: Long,
    val rssiMin: Int,
    val rssiMax: Int,
    val rssiAvg: Int,
    val rssiMed: Int
)