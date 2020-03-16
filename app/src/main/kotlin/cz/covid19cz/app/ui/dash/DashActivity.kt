package cz.covid19cz.app.ui.dash

import android.os.Bundle
import cz.covid19cz.app.R.layout
import cz.covid19cz.app.R.style
import cz.covid19cz.app.ext.withViewModel
import cz.covid19cz.app.ui.base.BaseActivity

class DashActivity : BaseActivity()  {

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(style.AppTheme)
        super.onCreate(savedInstanceState)
        setContentView(layout.activity_dash)

        withViewModel<DashViewModel>(viewModelFactory) {
            // TODO
        }
    }


}
