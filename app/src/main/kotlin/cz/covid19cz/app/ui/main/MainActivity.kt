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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if (BuildConfig.DEBUG){
            menuInflater.inflate(R.menu.main_debug, menu)
        } else {
            menuInflater.inflate(R.menu.main, menu)
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.action_test -> navigate(R.id.nav_sandbox, null,
                Builder()
                    .setPopUpTo(
                        R.id.nav_graph,
                        true
                    ).build())

            R.id.action_about -> navigate(R.id.nav_about)
        }

        return super.onOptionsItemSelected(item)
    }
}
