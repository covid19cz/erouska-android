package cz.covid19cz.erouska.db

import androidx.room.ColumnInfo

data class ExpositionEntity(
    @ColumnInfo(name = COLUMN_TUID)
    val tuid: String,
    @ColumnInfo(name = COLUMN_EXPOSITION_TIME)
    val expositionTime: Long
){
    companion object{
        const val COLUMN_TUID = "tuid"
        const val COLUMN_EXPOSITION_TIME = "expositionTime"
    }
}