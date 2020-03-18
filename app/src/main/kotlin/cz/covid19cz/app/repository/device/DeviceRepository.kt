package cz.covid19cz.app.repository.device

import androidx.lifecycle.LiveData

interface DeviceRepository {

    val data: LiveData<List<Device>>

    fun addDevices(devices: List<Device>)
    fun getDeviceById(id: Int): LiveData<Device>
    fun addDevice(device: Device)
    fun deleteDevice(device: Device)
}