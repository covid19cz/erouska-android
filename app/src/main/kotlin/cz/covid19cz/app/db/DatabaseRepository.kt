package cz.covid19cz.app.db

import cz.covid19cz.app.AppConfig
import cz.covid19cz.app.ext.daysToMilis
import io.reactivex.Single

interface DatabaseRepository {

    fun getAllDesc(): Single<List<ScanDataEntity>>
    fun getCriticalDesc(): Single<List<ScanDataEntity>>
    fun getAllFromTimestamp(timestamp: Long): Single<List<ScanDataEntity>>
    fun add(scanData: ScanDataEntity): Long
    fun getBuidCount(since: Long): Single<Int>
    fun getCriticalBuidCount(since: Long): Single<Int>
    fun delete(scanData: ScanDataEntity)
    fun deleteOldData() : Int
    fun clear()
}

class ExpositionRepositoryImpl(private val dao: ScanDataDao) :
    DatabaseRepository {

    override fun getAllDesc() : Single<List<ScanDataEntity>>{
        return dao.getAllDesc()
    }

    override fun getCriticalDesc() : Single<List<ScanDataEntity>>{
        return dao.getCriticalDesc(AppConfig.criticalExpositionRssi)
    }

    override fun add(device: ScanDataEntity): Long {
        if (device.buid.length == 20) {
            return dao.insert(device)
        }
        return 0L
    }

    override fun getAllFromTimestamp(timestamp: Long): Single<List<ScanDataEntity>> {
        return dao.getAllFromTimestamp(timestamp)
    }

    override fun getCriticalBuidCount(since: Long): Single<Int> {
        return dao.getBuidCount(since, AppConfig.criticalExpositionRssi)
    }

    override fun getBuidCount(since: Long): Single<Int> {
        return dao.getBuidCount(since, -100)
    }

    override fun delete(scanData: ScanDataEntity) {
        dao.delete(scanData)
    }

    override fun deleteOldData(): Int {
        return dao.deleteOldData(System.currentTimeMillis() - AppConfig.persistDataDays.daysToMilis())
    }

    override fun clear() {
        dao.clear()
    }
}