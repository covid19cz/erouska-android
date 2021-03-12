package cz.covid19cz.erouska.ui.publishsuccess

import cz.covid19cz.erouska.ui.base.BaseVM

class PublishSuccessVM : BaseVM() {

    val enoughKeys : Boolean = true

    fun close(){
        navigate(PublishSuccessFragmentDirections.actionNavPublishSuccessToNavDashboard())
    }
}