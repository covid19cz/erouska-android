package cz.covid19cz.app.ui.main

import android.os.Bundle
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
