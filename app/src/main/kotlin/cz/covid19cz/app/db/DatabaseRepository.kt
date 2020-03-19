package cz.covid19cz.app.db

import io.reactivex.Single

interface DatabaseRepository {

    val data: Single<List<ScanResultEntity>>

    fun add(scanResult: ScanResultEntity) : Long
    fun delete(scanResult: ScanResultEntity)
}

class ExpositionRepositoryImpl(private val dao: ScanResultsDao) :
    DatabaseRepository {

    override val data: Single<List<ScanResultEntity>> = dao.getAll()

    override fun add(device: ScanResultEntity) : Long {
        return dao.insert(device)
    }

    override fun delete(scanResult: ScanResultEntity) {
        dao.delete(scanResult)
    }
}