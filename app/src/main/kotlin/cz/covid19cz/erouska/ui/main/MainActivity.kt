package cz.covid19cz.erouska.ui.main

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.browser.customtabs.CustomTabsClient
import androidx.browser.customtabs.CustomTabsServiceConnection
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.navigation.NavDestination
import androidx.navigation.NavOptions
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import com.google.android.play.core.review.ReviewInfo
import com.google.android.play.core.review.ReviewManager
import com.google.android.play.core.review.ReviewManagerFactory
import cz.covid19cz.erouska.R
import cz.covid19cz.erouska.databinding.ActivityMainBinding
import cz.covid19cz.erouska.ext.isBtEnabled
import cz.covid19cz.erouska.ui.base.BaseActivity
import cz.covid19cz.erouska.utils.CustomTabHelper
import cz.covid19cz.erouska.utils.L
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity :
    BaseActivity<ActivityMainBinding, MainVM>(R.layout.activity_main, MainVM::class) {

    @Inject
    internal lateinit var customTabHelper: CustomTabHelper

    lateinit var reviewManager: ReviewManager
    var reviewInfo: ReviewInfo? = null

    private val customTabsConnection = object : CustomTabsServiceConnection() {
        override fun onCustomTabsServiceConnected(
            name: ComponentName,
            client: CustomTabsClient
        ) {
            connectedToCustomTabsService = true
            client.warmup(0)
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            connectedToCustomTabsService = false
        }
    }
    private var connectedToCustomTabsService = false

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        setSupportActionBar(toolbar)

        findNavController(R.id.nav_host_fragment).let {
            bottom_navigation.setOnNavigationItemSelectedListener { item ->
                navigate(
                    item.itemId,
                    navOptions = NavOptions.Builder().setPopUpTo(R.id.nav_graph, false).build()
                )
                true
            }

            it.addOnDestinationChangedListener { _, destination, arguments ->
                updateTitle(destination)
                updateBottomNavigation(destination, arguments)
            }
        }

        viewModel.serviceRunning.observe(this, Observer { isRunning ->
            ContextCompat.getColor(
                this,
                if (isRunning) R.color.green else R.color.red
            ).let {
                bottom_navigation.getOrCreateBadge(R.id.nav_dashboard).backgroundColor = it
            }
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.nav_about -> {
                false
            }
            R.id.nav_help -> {
                navigate(R.id.nav_help, Bundle().apply { putBoolean("fullscreen", true) })
                true
            }
            else -> {
                NavigationUI.onNavDestinationSelected(
                    item,
                    findNavController(R.id.nav_host_fragment)
                ) || super.onOptionsItemSelected(item)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        customTabHelper.chromePackageName?.let {
            CustomTabsClient.bindCustomTabsService(this, it, customTabsConnection)
        }
    }

    override fun onStop() {
        if (connectedToCustomTabsService) {
            unbindService(customTabsConnection)
            connectedToCustomTabsService = false
        }
        super.onStop()
    }

    /** Call this on a Fragment which might show reviews, ideally in onViewCreated **/
    fun initReviews() {
        reviewManager = ReviewManagerFactory.create(this)
        reviewManager.requestReviewFlow().addOnCompleteListener { request ->
            if (request.isSuccessful) {
                reviewInfo = request.result
            } else {
                L.e(request.exception)
            }
        }
    }

    /** Call this when asking for review **/
    fun askForReview() {
        if (reviewInfo != null) {
            reviewManager.launchReviewFlow(this, reviewInfo).addOnFailureListener {
                L.e(it)
            }.addOnCompleteListener { _ ->
               L.i("Review success")
            }
        }
    }

    private fun updateTitle(destination: NavDestination) {
        if (destination.label != null) {
            title = destination.label
        } else {
            setTitle(R.string.app_name)
        }
    }

    private fun updateBottomNavigation(
        destination: NavDestination,
        arguments: Bundle?
    ) {
        bottom_navigation.visibility =
            if (destination.arguments["fullscreen"]?.defaultValue == true
                || arguments?.getBoolean("fullscreen") == true
            ) {
                GONE
            } else {
                VISIBLE
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        L.d("$requestCode")
    }

}
