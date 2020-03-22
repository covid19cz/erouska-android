package cz.covid19cz.app.ui.dbexplorer

import androidx.databinding.ObservableArrayList
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.OnLifecycleEvent
import cz.covid19cz.app.R
import cz.covid19cz.app.db.DatabaseRepository
import cz.covid19cz.app.db.ScanResultEntity
import cz.covid19cz.app.ui.base.BaseVM
import cz.covid19cz.app.utils.Log
import java.text.SimpleDateFormat
import java.util.*

class DbExplorerVM(val dbRepo: DatabaseRepository) : BaseVM() {

    val items = ObservableArrayList<ScanResultEntity>()
    val dateFormatter = SimpleDateFormat("d.M.yyyy HH:mm:ss", Locale.getDefault())
    val timeFormatter = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun onCreate() {
        subscribe(dbRepo.data.map { orig ->
            orig.sortedByDescending { it.timestampStart }
        }, { Log.e(it) }) {
            items.addAll(it)
        }

    }

    fun formatTimeStamps(start : Long, end : Long) : String{
        return "${dateFormatter.format(Date(start))} - ${timeFormatter.format(Date(end))}"
    }

    fun getAvgSeconds(start : Long, end : Long, count : Int) : String{
        return String.format("%.1fs", (end - start)/count.toFloat()/1000f)
    }

    fun getExpositionColor(rssi : Int) : Int{
        return when{
            rssi > -60 -> R.color.exposition_level_8
            rssi > -65 -> R.color.exposition_level_7
            rssi > -70 -> R.color.exposition_level_6
            rssi > -75 -> R.color.exposition_level_5
            rssi > -80 -> R.color.exposition_level_4
            rssi > -85 -> R.color.exposition_level_3
            rssi > -90 -> R.color.exposition_level_2
            else -> R.color.exposition_level_1

        }
    }
}