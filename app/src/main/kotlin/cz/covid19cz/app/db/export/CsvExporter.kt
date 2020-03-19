package cz.covid19cz.app.db.export

import android.content.Context
import cz.covid19cz.app.db.DatabaseRepository
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.supercsv.io.CsvListWriter
import org.supercsv.prefs.CsvPreference
import java.io.File
import java.io.FileWriter

class CsvExporter(val context: Context, val repository: DatabaseRepository) {

    companion object {
        // represents the structure of the csv file
        val HEADERS: Array<String> = arrayOf(
            "buid",
            "timestampStart",
            "timestampEnd",
            "minRssi",
            "maxRssi",
            "avgRssi",
            "medRssi"
        )
    }

    private fun filename() = "${System.currentTimeMillis()}.csv"

    fun export(): Single<String> {
        val destinationFile = File(context.cacheDir, filename())
        val csvWriter =
            CsvListWriter(FileWriter(destinationFile), CsvPreference.STANDARD_PREFERENCE)

        return repository.data
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .map { entities ->
                // write metadata
                csvWriter.writeHeader(*HEADERS)

                // write entities
                entities.forEach {
                    csvWriter.write(
                        it.buid,
                        it.timestampStart,
                        it.timestampEnd,
                        it.rssiMin,
                        it.rssiMax,
                        it.rssiAvg,
                        it.rssiMed
                    )
                }
                csvWriter.close()
            }.map { destinationFile.absolutePath }
    }
}