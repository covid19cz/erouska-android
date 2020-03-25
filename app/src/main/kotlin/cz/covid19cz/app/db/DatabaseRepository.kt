package cz.covid19cz.app.db

import android.util.Log
import cz.covid19cz.app.ext.daysToMilis
import cz.covid19cz.app.utils.L
import io.reactivex.Single
import java.text.SimpleDateFormat
import java.util.*
import java.util.logging.SimpleFormatter
import kotlin.time.days

interface DatabaseRepository {

    fun getAllDesc(): Single<List<ScanDataEntity>>
    fun getAllFromTimestamp(timestamp: Long): Single<List<ScanDataEntity>>
    fun add(scanData: ScanDataEntity): Long
    fun getBuidCount(since: Long): Single<Int>
    fun getCriticalExpositions(since: Long, criticalRssi: Int, criticalMinutes: Int): Single<List<ExpositionEntity>>
    fun delete(scanData: ScanDataEntity)
    fun deleteOldData(persistDays : Int) : Int
    fun clear()
}

class ExpositionRepositoryImpl(private val dao: ScanDataDao) :
    DatabaseRepository {

    override fun getAllDesc() : Single<List<ScanDataEntity>>{
        return dao.getAllDesc()
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

    override fun getBuidCount(since: Long): Single<Int> {
        return dao.getDistinctCount(since)
    }

    override fun getCriticalExpositions(since: Long, criticalRssi: Int, criticalMinutes: Int): Single<List<ExpositionEntity>> {
        return dao.getCritical(since, criticalRssi, criticalMinutes)
    }

    override fun delete(scanData: ScanDataEntity) {
        dao.delete(scanData)
    }

    override fun deleteOldData(persistDays: Int): Int {
        return dao.deleteOldData(System.currentTimeMillis() - persistDays.daysToMilis())
    }

    override fun clear() {
        dao.clear()
    }
}