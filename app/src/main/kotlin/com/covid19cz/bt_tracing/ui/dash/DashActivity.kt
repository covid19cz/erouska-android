package com.covid19cz.bt_tracing.ui.dash

import android.os.Bundle
import com.covid19cz.bt_tracing.R.layout
import com.covid19cz.bt_tracing.R.style
import com.covid19cz.bt_tracing.ext.withViewModel
import com.covid19cz.bt_tracing.ui.base.BaseActivity

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
