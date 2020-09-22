package arch.event

import android.os.Bundle
import androidx.annotation.IdRes
import androidx.navigation.NavDirections
import androidx.navigation.NavOptions

class NavigationEvent() : LiveEvent() {

    var resId: Int? = null
    var currentDestination: Int? = null
    var navArgs: Bundle? = null
    var navDirections: NavDirections? = null
    var navOptions: NavOptions? = null

    constructor(@IdRes resId: Int, @IdRes currentDestination: Int, navArgs: Bundle? = null, navOptions: NavOptions? = null) : this() {
        this.resId = resId
        this.currentDestination = currentDestination
        this.navArgs = navArgs
        this.navOptions = navOptions
    }
}