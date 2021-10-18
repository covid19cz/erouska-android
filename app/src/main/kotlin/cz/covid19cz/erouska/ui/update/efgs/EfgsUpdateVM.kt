package cz.covid19cz.erouska.ui.update.efgs

import arch.viewmodel.BaseArchViewModel
import cz.covid19cz.erouska.AppConfig
import cz.covid19cz.erouska.db.SharedPrefsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class EfgsUpdateVM @Inject constructor(
     val sharedPrefsRepository: SharedPrefsRepository
) : BaseArchViewModel() {

     fun efgsDays() = AppConfig.efgsDays
     fun efgsSupportedCountries() = AppConfig.efgsSupportedCountries
}