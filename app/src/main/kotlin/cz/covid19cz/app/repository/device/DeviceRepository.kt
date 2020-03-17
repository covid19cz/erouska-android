package cz.covid19cz.app.repository.device

import androidx.lifecycle.LiveData

interface DeviceRepository {

    val data: LiveData<List<Device>>

    fun addDevices(devices: List<Device>)
    fun getDevice(): String
}