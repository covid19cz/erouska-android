package cz.covid19cz.app.ui.main

import android.os.Bundle
import android.view.MenuItem
import androidx.navigation.findNavController
import cz.covid19cz.app.R
import cz.covid19cz.app.databinding.ActivityMainBinding
import cz.covid19cz.app.ui.base.BaseActivity
import cz.covid19cz.app.ui.help.HelpFragment
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity :
    BaseActivity<ActivityMainBinding, MainVM>(R.layout.activity_main, MainVM::class) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setSupportActionBar(toolbar)

        findNavController(R.id.nav_host_fragment).addOnDestinationChangedListener { controller, destination, arguments ->
            if (destination.label != null){
                title = destination.label
            } else {
                setTitle(R.string.app_name)
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == R.id.menu_help) {
            findNavController(R.id.nav_host_fragment).navigate(R.id.nav_help)
            true
        } else super.onOptionsItemSelected(item)
    }
}
