package cz.covid19cz.erouska.ui.traveller

import cz.covid19cz.erouska.R
import cz.covid19cz.erouska.databinding.FragmentSymptomDateBinding
import cz.covid19cz.erouska.ui.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TravellerFragment : BaseFragment<FragmentSymptomDateBinding, TravellerVM>(R.layout.fragment_traveller, TravellerVM::class)