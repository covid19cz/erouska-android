package cz.covid19cz.app.db

import io.reactivex.Single

interface DatabaseRepository {

    val data: Single<List<ScanDataEntity>>
    fun add(scanData: ScanDataEntity): Long
    fun getBuidCount(since: Long): Single<Int>
    fun getCriticalExpositions(since: Long, criticalRssi: Int, criticalMinutes: Int): Single<List<ExpositionEntity>>
    fun delete(scanData: ScanDataEntity)
    fun clear()
}

class ExpositionRepositoryImpl(private val dao: ScanDataDao) :
    DatabaseRepository {

    override val data: Single<List<ScanDataEntity>> = dao.getAll()

    override fun add(device: ScanDataEntity): Long {
        if (device.buid.length == 20) {
            return dao.insert(device)
        }
        return 0L
    }

    override fun getBuidCount(since: Long): Single<Int> {
        return dao.getDistinctCount(since)
    }

    override fun getCriticalExpositions(since: Long, criticalRssi: Int, criticalMinutes: Int): Single<List<ExpositionEntity>> {
        return dao.getCritical(since, criticalRssi, criticalMinutes)
    }

    override fun delete(scanData: ScanDataEntity) {
        dao.delete(scanData)
    }

    override fun clear() {
        dao.clear()
    }
}