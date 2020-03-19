package cz.covid19cz.app.ui.main

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.navigation.NavOptions.Builder
import cz.covid19cz.app.BuildConfig
import cz.covid19cz.app.R
import cz.covid19cz.app.databinding.ActivityMainBinding
import cz.covid19cz.app.ui.base.BaseActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity :
    BaseActivity<ActivityMainBinding, MainVM>(R.layout.activity_main, MainVM::class) {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setSupportActionBar(toolbar)
    }
}
