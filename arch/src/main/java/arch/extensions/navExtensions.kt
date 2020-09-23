package arch.extensions

import androidx.navigation.NavController
import arch.event.NavigationEvent
import arch.event.NavigationGraphEvent
import arch.utils.safeLet
import cz.stepansonsky.mvvm.BuildConfig

fun NavController.navigate(navEvent: NavigationEvent) {
    try {
        if (navEvent.resId != null) {
            navigate(navEvent.resId!!, navEvent.navArgs, navEvent.navOptions)
        } else if (navEvent.navDirections != null) {
            navigate(navEvent.navDirections!!, navEvent.navOptions)
        }
    } catch (it: Throwable){
        if (BuildConfig.DEBUG) {
            it.printStackTrace()
        }
    }
}

fun NavController.setNavigationGraph(navEvent: NavigationGraphEvent) {
    safeLet(navEvent.navGraphId, navEvent.navStartDestinationId) { graphId, startNavId ->
        val graph = navInflater.inflate(graphId)
        graph.startDestination = startNavId
        setGraph(graph)
    }
}
