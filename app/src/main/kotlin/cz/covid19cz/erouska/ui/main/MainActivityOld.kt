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
import cz.covid19cz.erouska.db.SharedPrefsRepository
import cz.covid19cz.erouska.ui.base.BaseActivity
import cz.covid19cz.erouska.ui.exposurehelp.ExposureHelpFragmentArgs
import cz.covid19cz.erouska.ui.exposurehelp.entity.ExposureHelpType
import cz.covid19cz.erouska.utils.Analytics
import cz.covid19cz.erouska.utils.Analytics.KEY_CONTACTS
import cz.covid19cz.erouska.utils.Analytics.KEY_HELP
import cz.covid19cz.erouska.utils.Analytics.KEY_HOME
import cz.covid19cz.erouska.utils.Analytics.KEY_NEWS
import cz.covid19cz.erouska.utils.CustomTabHelper
import cz.covid19cz.erouska.utils.L
import cz.covid19cz.erouska.utils.showOrHide
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.search_toolbar.*
import javax.inject.Inject

@AndroidEntryPoint
class MainActivityOld :
    BaseActivity<ActivityMainBinding, MainVM>(R.layout.activity_main, MainVM::class) {

    @Inject
    internal lateinit var customTabHelper: CustomTabHelper

    @Inject
    internal lateinit var prefs : SharedPrefsRepository

    private lateinit var reviewManager: ReviewManager
    var reviewInfo: ReviewInfo? = null

    private val fragmentsWithSearch = arrayListOf(
        R.id.nav_help,
        R.id.nav_help_search,
        R.id.nav_help_category
    )

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
                val options = NavOptions.Builder().setPopUpTo(R.id.nav_graph, false).build()
                navigate(item.itemId, navOptions = options)
                logTabClickEventToAnalytics(item)
                true
            }

            it.addOnDestinationChangedListener { _, destination, arguments ->
                updateTitle(destination)
                toolbar_search_view.showOrHide(fragmentsWithSearch.contains(destination.id))
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

    private fun logTabClickEventToAnalytics(item: MenuItem) {
        val event = when (item.itemId) {
            R.id.nav_dashboard -> KEY_HOME
            R.id.nav_my_data -> KEY_NEWS
            R.id.nav_contacts -> KEY_CONTACTS
            R.id.nav_help -> KEY_HELP
            else -> throw IllegalStateException("analytics event for ${item.title} is not mapped")
        }
        Analytics.logEvent(this, event)
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
            R.id.nav_exposure_help -> {
                navigate(
                    R.id.nav_exposure_help,
                    ExposureHelpFragmentArgs(ExposureHelpType.EXPOSURE).toBundle()
                )
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
        prefs.setAppVisitedTimestamp()
    }

    override fun onStop() {
        if (connectedToCustomTabsService) {
            unbindService(customTabsConnection)
            connectedToCustomTabsService = false
        }
        // saving timestamp in onStop so that it eliminates cases when the feature in question
        // took place while being in the app
        prefs.setAppVisitedTimestamp()
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
            reviewManager.launchReviewFlow(this, reviewInfo!!).addOnFailureListener {
                L.e(it)
            }.addOnCompleteListener { L.i("Review success") }
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
