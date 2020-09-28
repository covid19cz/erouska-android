package arch.extensions

import android.os.Bundle
import androidx.annotation.IdRes
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import androidx.navigation.NavOptions
import arch.event.NavigationEvent
import arch.event.NavigationGraphEvent
import arch.utils.safeLet
import cz.stepansonsky.mvvm.BuildConfig

fun NavController.safeNavigate(navEvent: NavigationEvent) {
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

fun NavController.safeNavigate(@IdRes resId: Int, args: Bundle? = null, navOptions: NavOptions? = null) {
    try {
        navigate(resId, args, navOptions)
    } catch (it: Throwable){
        if (BuildConfig.DEBUG) {
            it.printStackTrace()
        }
    }
}
fun NavController.safeNavigate(directions: NavDirections, navOptions: NavOptions? = null) {
    try {
        navigate(directions, navOptions)
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
