package cz.covid19cz.erouska.helpers

import cz.covid19cz.erouska.db.DatabaseRepository
import cz.covid19cz.erouska.db.ScanDataEntity
import org.koin.test.KoinTest
import org.koin.test.inject

object DatabaseFiller : KoinTest {

    const val MEDIAN_RSSI = -80
    const val TUID = "01234567890123456789"
    const val TIMESTAMP_START = 0L

    private val db: DatabaseRepository by inject()

    fun addDataToDb() {
        db.clear()
        db.add(ScanDataEntity(1, TUID, TIMESTAMP_START, 1000, -70, MEDIAN_RSSI, 2))
    }
}