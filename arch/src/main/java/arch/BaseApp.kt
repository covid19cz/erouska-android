package arch

import androidx.multidex.MultiDexApplication

open class BaseApp : MultiDexApplication() {

    companion object {
        lateinit var instance: BaseApp private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}