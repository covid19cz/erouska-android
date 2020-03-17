package cz.covid19cz.app.repository.device

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "devices")
data class Device(
    @PrimaryKey val deviceId: Long,
    val timestamp: Long,
    val rssi: Int
)