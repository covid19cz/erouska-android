package cz.covid19cz.app.di

import kotlinx.coroutines.Dispatchers
import org.kodein.di.Kodein.Module
import org.kodein.di.generic.bind
import org.kodein.di.generic.provider
import kotlin.coroutines.CoroutineContext

private const val MODULE_NAME = "CoroutineContext Module"

val coroutineContextModule = Module(MODULE_NAME, false) {
    bind<CoroutineContext>(tag = "BGCoroutineContext") with provider { Dispatchers.IO }
    bind<CoroutineContext>(tag = "UICoroutineContext") with provider { Dispatchers.Main }
}