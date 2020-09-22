package arch.extensions

import androidx.navigation.NavController
import arch.event.NavigationEvent
import arch.event.NavigationGraphEvent
import arch.utils.safeLet

fun NavController.navigate(navEvent: NavigationEvent) {
    if (navEvent.resId != null) {
        navigate(navEvent.resId!!, navEvent.navArgs, navEvent.navOptions)
    } else if (navEvent.navDirections != null) {
        navigate(navEvent.navDirections!!, navEvent.navOptions)
    }
}

fun NavController.safeNavigate(navEvent: NavigationEvent) {
    if (navEvent.currentDestination != null && currentDestination?.id == navEvent.currentDestination) {
        if (navEvent.resId != null) {
            navigate(navEvent.resId!!, navEvent.navArgs, navEvent.navOptions)
        } else if (navEvent.navDirections != null) {
            navigate(navEvent.navDirections!!, navEvent.navOptions)
        }
    }
}

fun NavController.setNavigationGraph(navEvent: NavigationGraphEvent) {
    safeLet (navEvent.navGraphId, navEvent.navStartDestinationId) { graphId, startNavId ->
        val graph = navInflater.inflate(graphId)
        graph.startDestination = startNavId
        setGraph(graph)
    }
}
