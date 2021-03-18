package cz.covid19cz.erouska.ui.update.efgs

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import cz.covid19cz.erouska.AppConfig
import cz.covid19cz.erouska.R
import cz.covid19cz.erouska.databinding.FragmentEfgsUpdateBinding
import cz.covid19cz.erouska.ui.base.BaseFragment
import cz.covid19cz.erouska.utils.CustomTabHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_efgs_update.*
import javax.inject.Inject

@AndroidEntryPoint
class EfgsUpdateFragment : BaseFragment<FragmentEfgsUpdateBinding, EfgsUpdateVM>(
    R.layout.fragment_efgs_update,
    EfgsUpdateVM::class
) {

    @Inject
    internal lateinit var customTabHelper: CustomTabHelper

    private var isFullscreen: Boolean = false
    private var isOnboarding: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        isOnboarding = arguments?.let {
            EfgsUpdateFragmentArgs.fromBundle(it).onboarding
        } ?: false

        isFullscreen = arguments?.let {
            EfgsUpdateFragmentArgs.fromBundle(it).fullscreen
        } ?: false

        (activity as? AppCompatActivity)?.supportActionBar?.setDisplayShowTitleEnabled(!isFullscreen || isOnboarding)
        enableUpInToolbar(!isFullscreen || isOnboarding)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        showEFGSNews()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        (activity as? AppCompatActivity)?.supportActionBar?.setDisplayShowTitleEnabled(true)
    }

    private fun showEFGSNews() {
        viewModel.sharedPrefsRepository.setEFGSIntroduced(true)

        legacy_update_button.setOnClickListener {
            if (isOnboarding){
                navigate(R.id.action_nav_efgs_update_fragment_to_activation_fragment)
            } else {
                navController().navigateUp()
            }
        }

        legacy_update_checkbox.setOnCheckedChangeListener { _, isChecked ->
            viewModel.sharedPrefsRepository.setTraveller(isChecked)
        }
    }
}