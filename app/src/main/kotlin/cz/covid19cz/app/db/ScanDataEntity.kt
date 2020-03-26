package cz.covid19cz.app.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import cz.covid19cz.app.ext.rssiToDistance
import cz.covid19cz.app.ext.rssiToDistanceString
import kotlin.math.pow

@Entity(tableName = ScanDataDao.TABLE_NAME)
data class ScanDataEntity(
    @ColumnInfo(name = COLUMN_ID)
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    @ColumnInfo(name = COLUMN_BUID)
    val buid: String,
    @ColumnInfo(name = COLUMN_TIMESTAMP_START)
    val timestampStart: Long,
    @ColumnInfo(name = COLUMN_TIMESTAMP_END)
    val timestampEnd: Long,
    @ColumnInfo(name = COLUMN_RSSI_AVG)
    val rssiAvg: Int,
    @ColumnInfo(name = COLUMN_RSSI_MED)
    val rssiMed: Int,
    @ColumnInfo(name = COLUMN_RSSI_COUNT)
    val rssiCount: Int
){
    companion object {
        const val COLUMN_ID = "id"
        const val COLUMN_BUID = "buid"
        const val COLUMN_TIMESTAMP_START = "timestampStart"
        const val COLUMN_TIMESTAMP_END = "timestampEnd"
        const val COLUMN_RSSI_AVG = "rssiAvg"
        const val COLUMN_RSSI_MED = "rssiMed"
        const val COLUMN_RSSI_COUNT = "rssiCount"
    }

    fun getMaskedBuid() : String{
        return "...${buid.substring(14)}"
    }

    fun getMedDistance() : String{
        return rssiMed.rssiToDistanceString()
    }

    fun getAvgDistance() : String{
        return rssiAvg.rssiToDistanceString()
    }

    override fun equals(other: Any?): Boolean {
        return id == (other as? ScanDataEntity)?.id
    }
}