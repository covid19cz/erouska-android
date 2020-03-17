package cz.covid19cz.app.repository.device

class DeviceRepositoryImpl(private val deviceDao: DeviceDao) : DeviceRepository {

    override val data = deviceDao.findAll()

    override fun addDevices(devices: List<Device>) {
        deviceDao.add(devices)
    }

    override fun getDevice(): String {
        return "Device from repo"
    }
}