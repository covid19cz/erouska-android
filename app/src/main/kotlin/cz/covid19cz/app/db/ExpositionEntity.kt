package cz.covid19cz.app.db

import androidx.room.ColumnInfo

data class ExpositionEntity(
    @ColumnInfo(name = COLUMN_BUID)
    val buid: String,
    @ColumnInfo(name = COLUMN_EXPOSITION_TIME)
    val expositionTime: Long
){
    companion object{
        const val COLUMN_BUID = "buid"
        const val COLUMN_EXPOSITION_TIME = "expositionTime"
    }
}