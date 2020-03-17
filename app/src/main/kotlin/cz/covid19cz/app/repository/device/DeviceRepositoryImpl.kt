package cz.covid19cz.app.repository.device

import androidx.lifecycle.LiveData

class DeviceRepositoryImpl(private val deviceDao: DeviceDao) : DeviceRepository {

    override val data = deviceDao.findAll()

    override fun getDeviceById(id: Int): LiveData<Device> = deviceDao.findById(id)

    override fun addDevices(devices: List<Device>) {
        deviceDao.insertAll(devices)
    }

    override fun addDevice(device: Device) {
        deviceDao.insert(device)
    }

    override fun deleteDevice(device: Device) {
        deviceDao.delete(device)
    }
}