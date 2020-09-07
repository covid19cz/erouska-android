package arch.event

class NavigationGraphEvent() : LiveEvent() {

    var navGraphId: Int? = null
    var navStartDestinationId: Int? = null

    constructor(navGraphId: Int, navStartDestinationId: Int) : this() {
        this.navGraphId = navGraphId
        this.navStartDestinationId = navStartDestinationId
    }
}