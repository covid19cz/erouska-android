package arch.viewmodel

open class BaseDialogViewModel : BaseArchViewModel() {

    var title: String? = null
    var message: String? = null
    var positiveButton: String? = null
    var negativeButton: String? = null
    var neutralButton: String? = null

}
