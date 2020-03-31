package cz.covid19cz.erouska.db.export

import cz.covid19cz.erouska.db.DatabaseRepository
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.supercsv.io.CsvListWriter
import org.supercsv.prefs.CsvPreference
import java.io.ByteArrayOutputStream
import java.io.OutputStreamWriter
import java.nio.charset.Charset

class CsvExporter(private val repository: DatabaseRepository) {

    companion object {
        // represents the structure of the csv file
        val HEADERS: Array<String> = arrayOf(
            "buid",
            "timestampStart",
            "timestampEnd",
            "avgRssi",
            "medRssi"
        )
    }

    fun export(): Single<ByteArray> {
        val stream = ByteArrayOutputStream()
        val csvWriter = CsvListWriter(
            OutputStreamWriter(stream, Charset.forName("utf-8")),
            CsvPreference.STANDARD_PREFERENCE
        )

        return repository.getAll()
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
                        it.rssiAvg,
                        it.rssiMed
                    )
                }
                csvWriter.close()
            }.map { stream.toByteArray() }
    }
}
